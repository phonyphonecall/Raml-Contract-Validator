package ramlContractValidator.reader;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.raml.model.Raml;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by shendrickson1 on 6/25/14.
 *
 * @author Scott Hendrickson
 *
 */
public class RamlReader {

    private Log logger;

    public RamlReader(Log logger) {
        this.logger = logger;
    }

    public Raml getRaml(String ramlLocation) throws MojoFailureException {
        validateRaml(ramlLocation);
        return buildRaml(ramlLocation);
    }

    private Raml buildRaml(String ramlLocation) throws MojoFailureException {
        InputStream stream;
        try {
            stream = new FileInputStream(ramlLocation);
        } catch (FileNotFoundException e) {
            logger.error(String.format("Cannot find RAML contract at: %s", ramlLocation));
            throw new MojoFailureException("Cannot read find contract file");
        }

        return new RamlDocumentBuilder().build(stream, ramlLocation);
    }

    private void validateRaml(String ramlLocation) throws MojoFailureException {
        InputStream stream;
        try {
            stream = new FileInputStream(ramlLocation);
        } catch (FileNotFoundException e) {
            logger.error(String.format("Cannot find RAML contract at: %s", ramlLocation));
            throw new MojoFailureException("Cannot read find contract file");
        }

        List<ValidationResult> results = RamlValidationService.createDefault().validate(stream, ramlLocation);
        if(results != null) {
            if(!results.isEmpty()) {
                logger.error(String.format("Invalid RAML contract at: %s", ramlLocation));
                logger.error(String.format("Validation Results: "));
                for (ValidationResult result : results) {
                    logger.error(result.toString());
                }

                throw new MojoFailureException("Invalid RAML Contract at: " + ramlLocation);
            }
        }
    }
}
