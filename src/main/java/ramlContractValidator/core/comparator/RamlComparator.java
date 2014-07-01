package ramlContractValidator.core.comparator;

import org.apache.maven.plugin.logging.Log;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.parameter.QueryParameter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by shendrickson1 on 6/23/14.
 *
 * @author Scott Hendrickson
 *
 */
public class RamlComparator {
    private Log logger;

    List<RamlDiscrepancy> discrepancies = new LinkedList<RamlDiscrepancy>();

    public RamlComparator(Log logger) {
        this.logger = logger;
    }


    public List<RamlDiscrepancy> compare(Raml expectedRaml, Raml observedRaml) {
        logger.info("Beginning RAML Comparison");

        if(expectedRaml == null) {
            logger.error("Compare on null expected resource disallowed");
            throw new NullPointerException("Compare on null expected resource disallowed");
        }
        if(observedRaml == null) {
            logger.error("Compare on null observed resource disallowed");
            throw new NullPointerException("Compare on null observed resource disallowed");
        }

        compareResources(getAllResources(expectedRaml), getAllResources(observedRaml));


        if (discrepancies.isEmpty())
            return null;
        else
            return discrepancies;
    }

    private void compareResources(Map<String, Resource> expectedResources, Map<String, Resource> observedResources) {
        // First we compare resources
        MapComparator<String, Resource> resourceMapComparator = new MapComparator<String, Resource>(logger);

        discrepancies.addAll(resourceMapComparator.compare(expectedResources, observedResources, "resource"));

        Map<String, Resource> validExpectedResources = resourceMapComparator.getValidExpected();
        Map<String, Resource> validObservedResources = resourceMapComparator.getValidObserved();

        for(Map.Entry<String, Resource> entry : validExpectedResources.entrySet()) {
            Resource expectedResource = entry.getValue();
            Resource observedResource = validObservedResources.get(entry.getKey());

            logger.debug("comparing expected resource: " + expectedResource + " to " + observedResource);

            compareActions(expectedResource.getActions(), observedResource.getActions());
        }
    }


    private void compareActions(Map<ActionType, Action> expectedActions, Map<ActionType, Action> observedActions) {
        MapComparator<ActionType, Action> actionMapComparator = new MapComparator<ActionType, Action>(logger);

        discrepancies.addAll(actionMapComparator.compare(expectedActions, observedActions, "action"));

        Map<ActionType, Action> validExpectedActions = actionMapComparator.getValidExpected();
        Map<ActionType, Action> validObservedActions = actionMapComparator.getValidObserved();

        for(Map.Entry<ActionType, Action> entry : validExpectedActions.entrySet()) {
            Action expectedAction = entry.getValue();
            Action observedAction = validObservedActions.get(entry.getKey());

            compareQueryParams(expectedAction, observedAction);
        }
    }

    private void compareQueryParams(Action expectedAction, Action observedAction) {
        MapComparator<String, QueryParameter> actionMapComparator = new MapComparator<String, QueryParameter>(logger);
        discrepancies.addAll(actionMapComparator.compare(expectedAction.getQueryParameters(),
                                                         observedAction.getQueryParameters(),
                                                         "query parameter"));
    }


    private Map<String, Resource> getAllResources(Raml raml) {
        Map<String, Resource> resources = getAllResources(raml.getResources());
        return resources;
    }

    private Map<String, Resource> getAllResources(Map<String, Resource> resources) {
        Map<String, Resource> found = new LinkedHashMap<String, Resource>();
        if(resources != null)
            found.putAll(resources);

        Map<String, Resource> buffer = new LinkedHashMap<String, Resource>();
        for(Resource resource : found.values()) {
            if(resource.getResources() != null && !resource.getResources().isEmpty())
                buffer.putAll(getAllResources(resource.getResources()));
        }

        found.putAll(buffer);

        return found;
    }
}
