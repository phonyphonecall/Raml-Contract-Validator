package ramlContractValidator.core.comparator;

import org.apache.maven.plugin.logging.Log;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Resource;
import org.raml.model.parameter.QueryParameter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by shendrickson1 on 6/24/14.
 *
 * @author Scott Hendrickson
 *
 */
public class ResourceComparator {

    Log logger;

    public ResourceComparator(Log logger) {
        this.logger = logger;
    }

    public List<RamlDiscrepancy> compare(Resource expected, Resource observed) {
        List<RamlDiscrepancy> discrepancies = new LinkedList<RamlDiscrepancy>();

        if (observed == null) {
            discrepancies.add(new RamlDiscrepancy(expected, null, "Missing resource", logger));
            return discrepancies;
        }
        if (expected == null) {
            discrepancies.add(new RamlDiscrepancy(null, observed, "Non-specified resource observed", logger));
            return discrepancies;
        }

        if (!expected.getRelativeUri().equals(observed.getRelativeUri()))
            discrepancies.add(new RamlDiscrepancy(expected, observed, "Relative URI's do not match", logger));
        if (!expected.getParentUri().equals(observed.getParentUri())) {
            logger.debug("Expected Parent: " + expected.getParentUri());
            logger.debug("Observed Parent: " + observed.getParentUri());
            discrepancies.add(new RamlDiscrepancy(expected, observed, "Parent URI's do not match", logger));
        }

         discrepancies.addAll(compareActions(expected.getActions(), observed.getActions()));

        return discrepancies;
    }

    private List<RamlDiscrepancy> compareActions(Map<ActionType, Action> expected, Map<ActionType, Action> observed) {
        Map<ActionType, Action> observedQueryParamBuffer = new LinkedHashMap<ActionType, Action>();
        observedQueryParamBuffer.putAll(observed);
        List<RamlDiscrepancy> discrepancies = new LinkedList<RamlDiscrepancy>();

        for (Entry<ActionType, Action> entry : expected.entrySet()) {
            Action action = observed.get(entry.getKey());
            if(action == null) {
                discrepancies.add(new RamlDiscrepancy(expected, observed, "Could not find expected action", logger));
            } else {
                observedQueryParamBuffer.remove(entry.getKey());
            }
            discrepancies.addAll(compareQueryParms(entry.getValue().getQueryParameters(), action.getQueryParameters()));
        }

        for(Entry<ActionType, Action> entry : observedQueryParamBuffer.entrySet()) {
            discrepancies.add(new RamlDiscrepancy(null, observed, "Non-Specified query-parameter observed", logger));
        }

        return discrepancies;
    }

    private List<RamlDiscrepancy> compareQueryParms(Map<String, QueryParameter> expected, Map<String, QueryParameter> observed) {
        Map<String, QueryParameter> observedQueryParamBuffer = new LinkedHashMap<String, QueryParameter>();
        observedQueryParamBuffer.putAll(observed);
        List<RamlDiscrepancy> discrepancies = new LinkedList<RamlDiscrepancy>();

        for (Entry<String, QueryParameter> entry : expected.entrySet()) {
            QueryParameter queryParam = observed.get(entry.getKey());
            if(queryParam == null) {
                discrepancies.add(new RamlDiscrepancy(entry.getKey(), null, "Could not find expected query-parameter", logger));
            } else {
                observedQueryParamBuffer.remove(entry.getKey());
            }
        }

        for(Entry<String, QueryParameter> entry : observedQueryParamBuffer.entrySet()) {
            discrepancies.add(new RamlDiscrepancy(null, entry.getKey(), "Non-Specified query-parameter observed", logger));
        }

        return discrepancies;
    }
}
