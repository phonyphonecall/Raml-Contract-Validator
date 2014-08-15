Raml-Contract-Validator
=======================
A project by Scott Hendrickson for Intuit's ProTax Services team

The Raml-Contract-Validator is meant to help developers use raml to validate that their jax-rs annotated resource classes describle the API that their raml actually describes. The Raml-Contract-Validator is implemented as a maven plugin that runs at build time. It takes as parameters a RAML file, a resource class, and some optional configuration. It parses the given raml file, and compares it to the jax-rs annotated resource class. It attempts to buffer up all discrepancies found, and prints them back to the user. The plugin will only allow the build to succeed if the raml and the resource match entirely.

Currently Validates:
--------------------
- Path's (petstore.com/pets/{petId} is compared with @Path("/pets/{petId}"))
- Actions (GET: compared with @GET)
- QueryParameters (queryParameters: owner: is compared with @QueryParam("owner"))

Additional Features:
--------------------
If `<generateTemplateRaml>true</generateTemplateRaml>` is included in plugin configuration found in the POM.xml, the project will not fail if the raml file is not found. Instead it generates a skeleton raml for the user to fill in. This is meant to ease the RAML onboarding process for jax-rs users.

How to use:
-----------
Clone this repo locally, and run a maven clean install to get it into your local maven repositories.
(You may push it to your nexus if desired)

Add the following dependancy to your POM:
```        
<dependency>
  <groupId>com.intuit</groupId>
  <artifactId>raml-contract-validator</artifactId>
  <version>0.8</version>
</dependency>
```

Add the following plugin configuration to your POM:
```
<build>
    <plugins>
        <plugin>
            <groupId>com.intuit</groupId>
            <artifactId>raml-contract-validator</artifactId>
            <version>0.8</version>
            <executions>
                <execution>
                    <id>validation</id>
                    <phase>process-classes</phase>
                    <goals>
                        <goal>validateRamlContract</goal>
                    </goals>
                    <configuration>
                        <ramlLocation>${project.basedir}/POINT_TO_YOUR_RAML</ramlLocation>
                        <resourceClassPath>${project.basedir}/POINT_TO_YOUR_REASOURCE.java</resourceClassPath>
                        <!-- Allows first time users to generate a template RAML from their resource -->
                        <!-- HIGHLY reccommended that you disable this after you have a working raml -->
                        <generateTemplateRaml>true</generateTemplateRaml>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Features We Are Interested In:
------------------------------
- Transition to use the [mulesoft RAML parser](https://github.com/mulesoft/jaxrs-to-raml/tree/master/com.mulesoft.jaxrs.raml.generator), instead of the current custom built one
- Auto-generation of swagger docs, for those still stuck on swagger
- Incorperation of auto-generating raml html documentation
- In resource class linking of html documentation


