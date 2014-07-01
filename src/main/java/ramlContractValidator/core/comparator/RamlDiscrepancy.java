package ramlContractValidator.core.comparator;

import org.apache.maven.plugin.logging.Log;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.parameter.QueryParameter;

import java.util.Map;

/**
 * Created by shendrickson1 on 6/23/14.
 *
 * @author Scott Hendrickson
 *
 */
public class RamlDiscrepancy<T> {

    private String message;

    public RamlDiscrepancy( String message, Log logger ) {
        this.message = message;
        logThis(logger);
    }

    public RamlDiscrepancy(T expected, T observed, String name, String message, Log logger) {
        String expectedPrint;
        String observedPrint;

        expectedPrint = (expected != null) ? (isQueryParam(expected) ? ((QueryParameter) expected).getDisplayName() : expected.toString()) : "";
        observedPrint = (observed != null) ? (isQueryParam(observed) ? ((QueryParameter) observed).getDisplayName() : observed.toString()) : "";

        String finalMessage = String.format(message, name);

        this.message =  finalMessage + " | expected " + name + ": " + expectedPrint + " | observed " + name + ": " + observedPrint;
        logThis(logger);
    }

    public RamlDiscrepancy(Map<ActionType, Action> expected, Map<ActionType, Action> observed, String message, Log logger) {
        String expectedPrint = (expected != null) ? expected.toString() : "";
        String observedPrint = (observed != null) ? observed.toString() : "";

        this.message =  message + " | expected action(s): " + expectedPrint + " | observed action: " + observedPrint;
        logThis(logger);
    }


    public RamlDiscrepancy(String expectedQueryParamName, String observedQueryParamName, String message, Log logger) {

        this.message =  message + " | expected query-parameter: " + expectedQueryParamName + " | observed query-parameter: " + observedQueryParamName;
        logThis(logger);
    }

    private void logThis(Log logger) {
        logger.debug("Discrepancy Added: " + this.message);
    }


    @Override
    public String toString() {
        return message;
    }


    private boolean isQueryParam(T param) {
       return param instanceof QueryParameter;
     }
}
