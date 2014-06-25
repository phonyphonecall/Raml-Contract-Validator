package ramlContractValidator.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.raml.model.Raml;
import ramlContractValidator.core.comparator.RamlComparator;
import ramlContractValidator.core.visitor.VisitorHandler;
import ramlContractValidator.reader.RamlReader;

import java.io.File;

/**
 *
 * @author Scott Hendrickson
 *
 */
@Mojo( name = "validateRamlContract" )
public class ValidateMojo extends AbstractMojo {

    @Parameter( property = "ramlContractValidator.ramlLocation", required = true )
    private String ramlLocation;

    @Parameter( property = "ramlContractValidator.resourceClassPath", required = true )
    private String resourceClassPath;

    private RamlComparator ramlComparator;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        checkLocation(ramlLocation, "RAML");
        checkLocation(resourceClassPath, "resource class");

        RamlReader ramlReader = new RamlReader(getLog());

        try {
            getLog().info("Beginning RAML Contract Validation");
            Raml raml = ramlReader.getRaml(ramlLocation);
            File resourceFile = new File(resourceClassPath);

            initializeRamlComparator();
            VisitorHandler visitorHandler = new VisitorHandler(ramlComparator, getLog());
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


    private void checkLocation(String path, String name) throws MojoFailureException {
        File file = new File(path);
        if( !file.canRead() ) {
            getLog().info(String.format("Cannot read " + name + " at: %s", path));
            throw new MojoFailureException("Unable to read " + name);
        }
    }
}
