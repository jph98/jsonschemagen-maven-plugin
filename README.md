# maven-jsonschema-gen

Maven Plugin that generates JSON Schemas from Java objects using JJSchema.

To use this add the following to your project with the package name to scan:

```json
   <plugin>
       <groupId>com.froyo</groupId>
       <artifactId>jsonschemagen-maven-plugin</artifactId>
       <version>1.0</version>
       <executions>
           <execution>
               <phase>compile</phase>
               <goals>
                   <goal>jsonschemagen</goal>
               </goals>
           </execution>
       </executions>
       <configuration>
           <packageNameToScan>com.froyo</packageNameToScan>
           <schemaOutputDirectory>jsonschemas</schemaOutputDirectory>
       </configuration>
   </plugin>
```

You should annotate each of your classes with the JJSchema Attributes annotation, for more info
on this see:

    https://github.com/reinert/JJSchema
