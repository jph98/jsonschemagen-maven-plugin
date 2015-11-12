package com.froyo;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

/**
 * See: https://maven.apache.org/guides/plugin/guide-java-plugin-development.html
 * See: http://stackoverflow.com/questions/2659048/add-maven-build-classpath-to-plugin-execution-classpath
 */
@Mojo(name = "jsonschemagen")
@SuppressWarnings("UnusedDeclaration")
public class JsonSchemaGen extends AbstractMojo {

    @Parameter
    @SuppressWarnings("UnusedDeclaration")
    private String packageNameToScan;

    @Parameter
    @SuppressWarnings("UnusedDeclaration")
    private String schemaOutputDirectory;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    @SuppressWarnings("UnusedDeclaration")
    private MavenProject project;

    @Parameter(defaultValue = "${project.compileSourceRoots}", required = true, readonly = true)
    @SuppressWarnings("UnusedDeclaration")
    private List compileSourceRoots;

    /**
     * @parameter default-value="${descriptor}"
     */
    @Component
    @SuppressWarnings("UnusedDeclaration")
    private PluginDescriptor descriptor;

    /**
     * Execute the mojo.
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException {

        loadDependenciesIntoPluginClasspath();

        getLog().info("-- CLASSES --");
        getLog().info("Using package: " + packageNameToScan);
        Set<Class<?>> classes = getClassesFromPackage(packageNameToScan);

        if (classes.size() == 0) {
            getLog().warn("Could not find any classes to generate schemas from.");
        } else {
            generateJsonSchemaForClassSet(classes);
        }
    }

    /**
     * Generate JSON schema for class set
     * @param classes
     */
    private void generateJsonSchemaForClassSet(Set<Class<?>> classes) {

        getLog().info("\n Handling " + classes.size() + " classes \n");

        ObjectMapper m = new ObjectMapper();
        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();

        for (Class c : classes) {

            try {

                m.acceptJsonFormatVisitor(m.constructType(c), visitor);
                com.fasterxml.jackson.module.jsonSchema.JsonSchema jsonSchema = visitor.finalSchema();

                String output = m.writerWithDefaultPrettyPrinter().writeValueAsString(jsonSchema);
                writeFileToOutput(c, output);

            } catch (JsonMappingException e) {
                getLog().error("JSONMappingException: exception " + e);
            } catch (IOException e) {
                getLog().error("IOException: Could not write json schema for " + c.getName() + e);
            }

        }
    }

    private void writeFileToOutput(Class c, String json) throws IOException {

        File outputDirectory = new File(project.getBasedir() + File.separator + schemaOutputDirectory);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        String outputFileName = c.getPackage() + "." + c.getSimpleName() + ".json";
        File outputFile = new File(outputDirectory + File.separator, outputFileName);

        Files.write(json, outputFile, Charsets.UTF_8);

        getLog().info("Wrote: " + outputFile.getAbsolutePath());
    }

    private void loadDependenciesIntoPluginClasspath() {

        ClassRealm realm = descriptor.getClassRealm();

        getLog().info("-- PROJECTS --");
        List<MavenProject> projects = project.getCollectedProjects();
        for (MavenProject p: projects) {
            getLog().info("\tHandling " + p);
            addFilesToRealm(p, realm);
        }
    }

    private void addFilesToRealm(MavenProject p, ClassRealm realm) {

        try {

            List<String> classElements = p.getCompileClasspathElements();

            for (String element : classElements) {
                File elementFile = new File(element);
                realm.addURL(elementFile.toURI().toURL());
                System.out.println("Added " + elementFile.toURI().toURL().toString());
            }
        } catch (DependencyResolutionRequiredException e) {
            getLog().info("DependencyResolutionRequiredException: Could not add file " + e);
        } catch (MalformedURLException e) {
            getLog().info("MalformedURLException: Could not add file " + e);
        }
    }

    @SuppressWarnings("unused")
    private void displayModules() {

        System.out.println("-- MODULES --");
        List<String> modules = project.getModules();
        for (String mod: modules) {
            System.out.println(mod);
        }
    }

    public static Set<Class<?>> getClassesFromPackage(String packageName) {

        Reflections ref = new Reflections(packageName);
        return ref.getTypesAnnotatedWith(JsonSerializableSchema.class);
    }

}
