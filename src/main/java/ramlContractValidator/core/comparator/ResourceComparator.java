package ramlContractValidator.core.comparator;

import org.apache.maven.plugin.logging.Log;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Resource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by shendrickson1 on 6/24/14.
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
            discrepancies.add(new RamlDiscrepancy(expected, null, "Missing matching observed resource", logger));
            return discrepancies;
        }
        if (expected == null) {
            discrepancies.add(new RamlDiscrepancy(null, observed, "Missing matching expected resource", logger));
            return discrepancies;
        }

        if (!expected.getRelativeUri().equals(observed.getRelativeUri()))
            discrepancies.add(new RamlDiscrepancy(expected, observed, "Relative URI's do not match", logger));
        if (!expected.getParentUri().equals(observed.getParentUri()))
            discrepancies.add(new RamlDiscrepancy(expected, observed, "Parent URI's do not match", logger));

//        if (!expected.getType().equals(observed.getType()))
//            discrepancies.add(new RamlDiscrepancy(expected, observed, "Types do not match", logger));

         compareActions(expected.getActions(), observed.getActions());

        // expected.getBaseUriParameters();
        // expected.getUriParameters();
        // expected.getUri();

        return discrepancies;
    }

    private List<RamlDiscrepancy> compareActions(Map<ActionType, Action> expected, Map<ActionType, Action> observed) {
        List<RamlDiscrepancy> discrepancies = new LinkedList<RamlDiscrepancy>();

        for (Entry<ActionType, Action> entry : expected.entrySet()) {
            Action action = observed.get(entry.getKey());
            if(action == null) {
                discrepancies.add(new RamlDiscrepancy(expected, observed, "Could not find expected action", logger));
                continue;
            }
            // TODO could compare more... But we just care for types now
        }

        return discrepancies;
    }
}
