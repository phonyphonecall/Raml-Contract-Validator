package ramlContractValidator.core.comparator;

import org.apache.maven.plugin.logging.Log;
import org.raml.model.Raml;
import org.raml.model.Resource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by shendrickson1 on 6/23/14.
 *
 */
public class RamlComparator {
    private Log logger;

    // --- Configuration Variables ---//
    private boolean compareResources;


    public RamlComparator(Log logger) {
        this.logger = logger;
    }

    List<RamlDiscrepancy> discrepancies = new LinkedList<RamlDiscrepancy>();


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

        compareResources(expectedRaml.getResources(), observedRaml.getResources());

        if (discrepancies.isEmpty())
            return null;
        else
            return discrepancies;
    }

    private void compareResources(Map<String, Resource> expectedRamlResources, Map<String, Resource> observedRamlResources) {
        if (compareResources) {
            logger.info("Beginning Resource Comparison");
            recurseResources(expectedRamlResources, observedRamlResources);
        }
    }

    private void recurseResources(Map<String, Resource> expectedRamlResources, Map<String, Resource> observedRamlResources) {
        if (expectedRamlResources == null) {
            logger.debug("No resources found in expected RAML");
            discrepancies.add(new RamlDiscrepancy("No resources found in expected RAML", logger));
            return;
        }
        if (observedRamlResources == null) {
            logger.debug("No resources found in expected RAML");
            discrepancies.add(new RamlDiscrepancy("No resources found in observed RAML", logger));
            return;
        }

        for(Entry<String, Resource> entry : expectedRamlResources.entrySet()) {
            Resource expected = entry.getValue();
            Resource observed = observedRamlResources.get(entry.getKey());

            logger.debug("Expected Resource: " + expected.toString());
            logger.debug("Observed Resource: " + observed.toString());

//            if(expected == null) {
//                logger.debug("Null expected resource found in resource map");
//                discrepancies.add(new RamlDiscrepancy("Null expected resource found in resource map", logger));
//                continue;
//            }
//            if(observed == null) {
//                logger.debug("Could not find expected resource" + expected.toString());
//                discrepancies.add(new RamlDiscrepancy(expected, null, "Could not find expected resource", logger));
//                continue;
//            }

            List<RamlDiscrepancy> resourceDiscrepancies = new ResourceComparator(logger).compare(expected, observed);
            if (resourceDiscrepancies != null) {
                discrepancies.addAll(resourceDiscrepancies);
            }

            // Recurse
            Map<String, Resource> expectedChildResources = expected.getResources();
            Map<String, Resource> observedChildResources = observed.getResources();
            if (expectedChildResources != null) {
                recurseResources(expectedChildResources, observedChildResources);
            }
        }
    }


    public void setCompareResources(boolean compareResources) {
        this.compareResources = compareResources;
    }

}
