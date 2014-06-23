package ramlContractValidator.maven;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.raml.model.Raml;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;
import ramlContractValidator.core.ContractValidator;

import java.io.*;
import java.util.List;

@Mojo( name = "validateRamlContract" )
public class ValidateMojo extends AbstractMojo {

    @Parameter( property = "ramlContractValidator.ramlLocation", required = true )
    private String ramlLocation;


    @Parameter( property = "ramlContractValidator.resourceKlassPath", required = true)
    private String resourceKlassPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if( !canReadLocation(ramlLocation) ) {
            getLog().info(String.format("Cannot read RAML contract at: %s", ramlLocation));

            throw new MojoFailureException("Unable to read raml contract");
        }

        try {
            getLog().info("Beginning RAML Contract Validation");
            getLog().info("Validating RAML at: " + ramlLocation);
            validateRaml(ramlLocation);
            getLog().info("RAML Contract Valid");
            getLog().info("Building RAML document");
            Raml raml = buildRaml(ramlLocation);
            getLog().info("RAML document built");

            ContractValidator cv = new ContractValidator();

            cv.compare(raml, getResourceInstance(resourceKlassPath), getLog());
        } catch (Exception e) {
            getLog().error(e.getMessage());
            throw new MojoFailureException(e.getMessage());
        }
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

    private CompilationUnit getResourceInstance(String resourceKlassPath){
        FileInputStream in = null;
        CompilationUnit cu = null;
        try {
            // creates an input stream for the file to be parsed
            in = new FileInputStream(resourceKlassPath);

            try {
                // parse the file
                cu = JavaParser.parse(in);
            } catch (ParseException e) {
                getLog().error("Could not parse resource");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return cu;
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

    private boolean canReadLocation(String path) {
        File file = new File(path);
        return file.canRead();
    }
}
