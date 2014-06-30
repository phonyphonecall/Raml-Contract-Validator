package ramlContractValidator.core.visitor;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import org.apache.maven.plugin.logging.Log;
import org.raml.emitter.RamlEmitter;
import org.raml.model.Raml;
import ramlContractValidator.core.comparator.RamlComparator;
import ramlContractValidator.core.comparator.RamlDiscrepancy;
import ramlContractValidator.writer.RamlWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by shendrickson1 on 6/23/14.
 *
 * @author Scott Hendrickson
 *
 */
public class VisitorHandler {

    private final Log logger;
    private final RamlComparator ramlComparator;
    private String templateLocation;
    private boolean getTemplateRaml;

    private Raml observedRaml = new Raml();

    public VisitorHandler(RamlComparator ramlComparator, boolean getTemplateRaml, String templateFileLocation, Log logger) {
        this.ramlComparator  = ramlComparator;
        this.getTemplateRaml = getTemplateRaml;
        this.templateLocation = templateFileLocation;
        this.logger = logger;
    }

    public void validateResource( final File resourceFile, Raml expectedRaml ) {
        CompilationUnit cu;
        FileInputStream in;
        try {
            in = new FileInputStream(resourceFile);
            cu = JavaParser.parse(in);
            // These will visit all class and method definitions respectively, calling the visit methods in visitor
            new ResourceValidatorClassVisitor(observedRaml, logger).visit(cu, null);
            new ResourceValidatorMethodVisitor(observedRaml, logger).visit(cu, null);
        } catch (ParseException e) {
            logger.error("Parse Exception in reflection util");
            throw new RuntimeException("Parse Exception in reflection util");
        } catch (FileNotFoundException e) {
            logger.error("File Not Found Exception in reflection util");
            throw new RuntimeException("File Not Found Exception in reflection util");
        }

        logger.debug(new RamlEmitter().dump(observedRaml));

        if (!getTemplateRaml || (expectedRaml != null)) {
            logger.debug(new RamlEmitter().dump(expectedRaml));

            List<RamlDiscrepancy> issues = ramlComparator.compare(expectedRaml, observedRaml);

            if (issues != null) {
                logger.error("RAML Contract Violations:");
                for (RamlDiscrepancy discrepancy : issues) {
                    logger.error(discrepancy.toString());
                }
                throw new RuntimeException("RAML CONTRACT VIOLATIONS DETECTED.");
            } else {
                logger.info("RAML Contract Validation Successful");
            }
        } else {
            new RamlWriter(templateLocation, logger).write(new RamlEmitter().dump(observedRaml));

            logger.warn("-------------------------------------------------------------------------------------------------------------------------");
            logger.warn("------------- SEVERE WARNING --------------------------------------------------------------------------------------------");
            logger.warn("  No validation will take place.");
            logger.warn("  Generated template RAML file from resource layer.");
            logger.warn("  File can be found at: " + templateLocation);
            logger.warn("  It is recommended this file be renamed, and filled out to become your resource's RAML contract.");
            logger.warn("  Don't forget to change the RAML location in POM.xml to reflect the name and location of your filled out RAML contract.");
            logger.warn("  RAML Contract validator exiting.");
            logger.warn("-------------------------------------------------------------------------------------------------------------------------");
        }
    }
}
