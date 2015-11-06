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

    @Parameter( property = "package")
    private String packageName;

    public JsonSchemaGen() {

    }

    public void execute() throws MojoExecutionException {

        getLog().info("Execute JSON Schema Generator");

        // Find all annotated classes
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Attributes.class);

        for (Class c: annotatedClasses) {

            JsonSchemaGenerator v4generator = SchemaGeneratorBuilder.draftV4Schema().build();
            JsonNode productSchema = v4generator.generateSchema(c);
            System.out.println(productSchema);
            getLog().info("Generated " + productSchema);
        }
    }
}
