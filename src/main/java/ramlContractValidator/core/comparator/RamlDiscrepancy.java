package ramlContractValidator.core.comparator;

import org.apache.maven.plugin.logging.Log;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Resource;

import java.util.Map;

/**
 * Created by shendrickson1 on 6/23/14.
 *
 */
public class RamlDiscrepancy {

    private String message;

    public RamlDiscrepancy( String message, Log logger ) {
        this.message = message;
        logger.debug("Discrepancy Added: " + this.message);
    }

    public RamlDiscrepancy(Resource expected, Resource observed, String message, Log logger) {
        String expectedPrint = (expected != null) ? expected.getUri() : "";
        String observedPrint = (observed != null) ? observed.getUri() : "";

        this.message =  message + " | expected resource: " + expectedPrint + " | observed resource: " + observedPrint;
        logger.debug("Discrepancy Added: " + this.message);
    }

    public RamlDiscrepancy(Map<ActionType, Action> expected, Map<ActionType, Action> observed, String message, Log logger) {
        String expectedPrint = (expected != null) ? expected.toString() : "";
        String observedPrint = (observed != null) ? observed.toString() : "";

        this.message =  message + " | expected action(s): " + expectedPrint + " | observed action: " + observedPrint;
        logger.debug("Discrepancy Added: " + this.message);
    }

    @Override
    public String toString() {
        return message;
    }
}
