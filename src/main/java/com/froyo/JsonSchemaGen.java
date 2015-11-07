package com.froyo;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.JsonSchemaGenerator;
import com.github.reinert.jjschema.SchemaGeneratorBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.reflections.Reflections;

import java.util.Set;

/**
 * https://maven.apache.org/guides/plugin/guide-java-plugin-development.html
 */
@Mojo(name = "jsonschemagen")
public class JsonSchemaGen extends AbstractMojo {

    @Parameter
    private String packageNameToScan;

    public void execute() throws MojoExecutionException {

        getLog().info("Execute JSON Schema Generator for package name: " + packageNameToScan);

        // Find all annotated classes
        Reflections reflections = new Reflections(packageNameToScan);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Attributes.class);

        getLog().info("Found " + annotatedClasses.size() + " classes with annotation " + Attributes.class.getCanonicalName());

        for (Class c: annotatedClasses) {

            JsonSchemaGenerator v4generator = SchemaGeneratorBuilder.draftV4Schema().build();
            JsonNode productSchema = v4generator.generateSchema(c);
            System.out.println(productSchema);
            getLog().info("Generated " + productSchema);
        }
    }
}
