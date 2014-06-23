package ramlContractValidator.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.raml.model.Raml;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;
import ramlContractValidator.core.comparator.RamlComparator;
import ramlContractValidator.core.visitor.VisitorHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@Mojo( name = "validateRamlContract" )
public class ValidateMojo extends AbstractMojo {

    @Parameter( property = "ramlContractValidator.ramlLocation", required = true )
    private String ramlLocation;


    @Parameter( property = "ramlContractValidator.resourceClassPath", required = true )
    private String resourceClassPath;

    private RamlComparator ramlComparator;


    private VisitorHandler visitorHandler;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        checkLocation(ramlLocation, " provided RAML");
        checkLocation(resourceClassPath, "resource class");

        try {
            getLog().info("Beginning RAML Contract Validation");
            validateRaml(ramlLocation);
            Raml raml = buildRaml(ramlLocation);
            File resourceFile = new File(resourceClassPath);

            initializeRamlComparator();
            visitorHandler = new VisitorHandler(ramlComparator, getLog());
            visitorHandler.validateResource(resourceFile, raml);
        } catch (Exception e) {
            getLog().error(e.getMessage());
            throw new MojoFailureException(e.getMessage());
        }
    }

    private void initializeRamlComparator() {
        ramlComparator = new RamlComparator(getLog());
        ramlComparator.setCompareResources(true);
    }

    private Raml buildRaml(String ramlLocation) throws MojoFailureException {
        InputStream stream = null;
        try {
            stream = new FileInputStream(ramlLocation);
        } catch (FileNotFoundException e) {
            getLog().error(String.format("Cannot find RAML contract at: %s", ramlLocation));
            throw new MojoFailureException("Cannot read find contract file");
        }

        return new RamlDocumentBuilder().build(stream, ramlLocation);
    }



    private void validateRaml(String location) throws MojoFailureException {
        InputStream stream = null;
        try {
            stream = new FileInputStream(location);
        } catch (FileNotFoundException e) {
            getLog().error(String.format("Cannot find RAML contract at: %s", location));
            throw new MojoFailureException("Cannot read find contract file");
        }

        List<ValidationResult> results = RamlValidationService.createDefault().validate(stream, location);
        if(results != null) {
            if(!results.isEmpty()) {
                getLog().error(String.format("Invalid RAML contract at: %s", location));
                getLog().error(String.format("Validation Results: "));
                for (ValidationResult result : results) {
                    getLog().error(result.toString());
                }

                throw new MojoFailureException("Invalid RAML Contract at: " + location);
            }
        }
    }

    private void checkLocation(String path, String name) throws MojoFailureException {
        File file = new File(path);
        if( !file.canRead() ) {
            getLog().info(String.format("Cannot read " + name + " at: %s", path));
            throw new MojoFailureException("Unable to read " + name);
        }
    }
}
