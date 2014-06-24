package ramlContractValidator.core.comparator;

import org.apache.maven.plugin.logging.Log;
import org.raml.model.Raml;
import org.raml.model.Resource;

import java.util.LinkedHashMap;
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
            recurseResources(expectedRamlResources, observedRamlResources);
        }
    }

    private void recurseResources(Map<String, Resource> expectedRamlResources, Map<String, Resource> observedRamlResources) {
        if (observedRamlResources == null) {
            logger.debug("No resource level found in expected RAML");
            discrepancies.add(new RamlDiscrepancy("No resource level found in observed RAML", logger));
            return;
        }

        Map<String, Resource> observedResourceBuffer = new LinkedHashMap<String, Resource>();
        observedResourceBuffer.putAll(observedRamlResources);

        for(Entry<String, Resource> entry : expectedRamlResources.entrySet()) {
            Resource expected = entry.getValue();
            Resource observed = observedRamlResources.get(entry.getKey());
            if(observed != null)
                observedResourceBuffer.remove(entry.getKey());

            discrepancies.addAll(new ResourceComparator(logger).compare(expected, observed));

            Map<String, Resource> expectedChildResources = expected.getResources();
            Map<String, Resource> observedChildResources = (observed != null) ? observed.getResources() : null;
            if (expectedChildResources != null) {
                recurseResources(expectedChildResources, observedChildResources);
            }
        }

        for(Entry<String, Resource> entry : observedResourceBuffer.entrySet()) {
            discrepancies.addAll(new ResourceComparator(logger).compare(null, entry.getValue()));
        }
    }


    public void setCompareResources(boolean compareResources) {
        this.compareResources = compareResources;
    }

}
