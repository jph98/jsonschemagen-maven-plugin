package com.froyo;

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.JsonSchemaGenerator;
import com.github.reinert.jjschema.SchemaGeneratorBuilder;
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
import java.util.Map;
import java.util.Set;

/**
 * See: https://maven.apache.org/guides/plugin/guide-java-plugin-development.html
 * See: http://stackoverflow.com/questions/2659048/add-maven-build-classpath-to-plugin-execution-classpath
 */
@Mojo(name = "jsonschemagen")
public class JsonSchemaGen extends AbstractMojo {

    private static final boolean DEBUG = false;

    @Parameter
    private String packageNameToScan;

    @Parameter
    private String schemaOutputDirectory;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.compileSourceRoots}", required = true, readonly = true)
    private List compileSourceRoots;

    /**
     * @parameter default-value="${descriptor}"
     */
    @Component
    private PluginDescriptor descriptor;

    public void execute() throws MojoExecutionException {

        displayModules();

        loadDependenciesIntoPluginClasspath();

        System.out.println("-- CLASSES --");
        System.out.println("Using package: " + packageNameToScan);
        Set<Class<?>> classes = getClassesFromPackage(packageNameToScan);

        if (classes.size() == 0) {
            getLog().warn("Could not find any classes to generate schemas from.");
        } else {
            generateJsonSchemaForClassSet(classes);
        }
    }

    private void generateJsonSchemaForClassSet(Set<Class<?>> classes) {

        JsonSchemaGenerator generator = SchemaGeneratorBuilder.draftV4Schema().build();
        for (Class c : classes) {
            System.out.println("\tJJSchemaGen - processing: " + c.getName());
            ObjectNode node = generator.generateSchema(c);

            if (DEBUG) {
                System.out.println(node);
            }

            try {
                writeFileToOutput(c, node);
            } catch (IOException e) {
                System.err.println("Could not write json schema for " + c.getName() + e);
            }

        }
    }

    private void writeFileToOutput(Class c, ObjectNode node) throws IOException {

        File outputDirectory = new File(project.getBasedir() + File.separator + schemaOutputDirectory);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        String outputFileName = c.getSimpleName() + ".json";
        File outputFile = new File(outputDirectory + File.separator, outputFileName);

        String formattedJson = JsonWriter.formatJson(node.toString());
        Files.write(formattedJson, outputFile, Charsets.UTF_8);

        System.out.println("Wrote: " + outputFile.getAbsolutePath());
    }

    private void loadDependenciesIntoPluginClasspath() {

        ClassRealm realm = descriptor.getClassRealm();

        System.out.println("-- PROJECTS --");
        List<MavenProject> projects = project.getCollectedProjects();
        for (MavenProject p: projects) {
            System.out.println("\tHandling " + p);
            addFilestoRealm(p, realm);
        }
    }

    private void addFilestoRealm(MavenProject p, ClassRealm realm) {

        try {

            List<String> classElements = p.getCompileClasspathElements();

            for (String element : classElements) {
                File elementFile = new File(element);
                realm.addURL(elementFile.toURI().toURL());
                System.out.println("Added " + elementFile.toURI().toURL().toString());
            }
        } catch (DependencyResolutionRequiredException e) {
            System.out.println("Could not resolve dependency " + e);
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException " + e);
        }
    }

    private void displayModules() {

        System.out.println("-- MODULES --");
        List<String> modules = project.getModules();
        for (String mod: modules) {
            System.out.println(mod);
        }
    }

    public static Set<Class<?>> getClassesFromPackage(String packageName) {

        Reflections ref = new Reflections(packageName);
        return ref.getTypesAnnotatedWith(Attributes.class);
    }

}
