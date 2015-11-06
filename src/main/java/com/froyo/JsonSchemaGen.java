package com.froyo;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.JsonSchemaGenerator;
import com.github.reinert.jjschema.SchemaGeneratorBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.reflections.Reflections;

import java.util.Set;

@Mojo(name = "generateschema")
public class JsonSchemaGen extends AbstractMojo {

    public JsonSchemaGen() {

    }

    public void execute() throws MojoExecutionException {

        getLog().info("Execute JSON Schema Generator");

        // Find all annotated classes
        Reflections reflections = new Reflections("com.froyo");
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Attributes.class);

        for (Class c: annotatedClasses) {

            JsonSchemaGenerator v4generator = SchemaGeneratorBuilder.draftV4Schema().build();
            JsonNode productSchema = v4generator.generateSchema(c);
            System.out.println(productSchema);
            getLog().info("Generated " + productSchema);
        }
    }
}
