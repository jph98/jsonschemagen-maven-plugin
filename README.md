# maven-jsonschema-gen

Maven Plugin that generates JSON Schemas from Java objects using JJSchema.

To use this add the following to your project with the package name to scan:

    <plugin>
        <groupId>com.froyo</groupId>
        <artifactId>jsonschemagen-maven-plugin</artifactId>
        <version>1.0</version>

        <configuration>
            <packageNameToScan>com.brightpearl</packageNameToScan>
            <skipErrorNoDescriptorsFound>true</skipErrorNoDescriptorsFound>
        </configuration>

        <executions>
            <execution>
                <phase>validate</phase>
                <goals>
                    <goal>jsonschemagen</goal>
                </goals>
            </execution>
        </executions>
    </plugin>

You should annotate each of your classes with the JJSchema Attributes annotation, for more info
on this see:

https://github.com/reinert/JJSchema