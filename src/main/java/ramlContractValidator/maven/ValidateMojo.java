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

    @Parameter( property = "ramlContractValidator.resourceClassPaths", required = true )
    private String[] resourceClassPaths;

    @Parameter( property = "generateTemplateRaml", required = false )
    private String generateTemplateRaml = "false";

    //@Parameter( property = "generateRamlHtmlDocs", required = false )
    private String generateRamlHtmlDocs = "true";


    private String templateRamlLocation;


    private RamlComparator ramlComparator;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            for(String resource : resourceClassPaths) {
                checkLocation(resource, "resource class");
            }

            initializeTemplateLocation(ramlLocation);

            RamlReader ramlReader = new RamlReader(getBoolean(generateTemplateRaml), getLog());

            getLog().info("Beginning RAML Contract Validation");
            Raml raml = ramlReader.getRaml(ramlLocation);

            if(raml == null) {
                getLog().warn("No RAML Contract found at: " + ramlLocation);
            }

            File[] resourceFiles = new File[resourceClassPaths.length];
            for(int i = 0; i < resourceClassPaths.length; i++) {
                resourceFiles[i] = new File(resourceClassPaths[i]);
            }

            initializeRamlComparator();
            VisitorHandler visitorHandler = new VisitorHandler(ramlComparator, getBoolean(generateTemplateRaml), templateRamlLocation, getLog());
            visitorHandler.validateResource(resourceFiles, raml);

            if(getBoolean(generateRamlHtmlDocs)) {
                // TODO
            }
        } catch (Exception e) {
            getLog().error(e.getMessage());
            throw new MojoFailureException(e.getMessage());
        }
    }

    private boolean getBoolean(String generateTemplateRaml) throws MojoFailureException {
        if (generateTemplateRaml.equals("false")) {
            return false;
        } else if (generateTemplateRaml.equals("true")) {
            return true;
        } else {
            throw new MojoFailureException("Invalid generateTemplateRaml parameter specified in pom: " + generateTemplateRaml + " | Requires either \'true\' or \'false\'" );
        }
    }


    private void initializeRamlComparator() {
        ramlComparator = new RamlComparator(getLog());
    }


    private void checkLocation(String path, String name) throws MojoFailureException {
        File file = new File(path);
        if( !file.canRead() ) {
            getLog().info(String.format("Cannot read " + name + " at: %s", path));
            throw new MojoFailureException("Unable to read " + name);
        }
    }


    private void initializeTemplateLocation(String ramlLocation) {
        int dir = ramlLocation.lastIndexOf("/");
        templateRamlLocation = ramlLocation.substring(0, dir);
    }
}
