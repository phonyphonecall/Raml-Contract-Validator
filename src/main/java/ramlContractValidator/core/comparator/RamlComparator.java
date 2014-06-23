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

    List<RamlDiscrepancy> discrepencies = new LinkedList<RamlDiscrepancy>();


    public List<RamlDiscrepancy> compare(Raml expectedRaml, Raml observedRaml) {
        compareResources(expectedRaml, observedRaml);


        if (discrepencies.isEmpty())
            return null;
        else
            return discrepencies;
    }

    private void compareResources(Raml expectedRaml, Raml observedRaml) {
        if (compareResources) {
            recurseResources(expectedRaml.getResources(), observedRaml.getResources());
        }
    }

    private void recurseResources(Map<String, Resource> expectedRamlResources, Map<String, Resource> observedRamlResources) {
        for(Entry<String, Resource> entry : expectedRamlResources.entrySet()) {
            Resource expected = entry.getValue();
            Resource observed = observedRamlResources.get(entry.getKey());

            if(observed == null) {
                discrepencies.add(new RamlDiscrepancy(expected, null, "Could not find expected resource"));
                continue;
            }

            compareResource(expected, observed);

            // Recurse
            Map<String, Resource> expectedChildResources = expected.getResources();
            Map<String, Resource> observedChildResources = observed.getResources();
            if (expectedChildResources != null) {
                recurseResources(expectedChildResources, observedChildResources);
            }
        }
    }

    private void compareResource(Resource expected, Resource observed) {
        // TODO finish this
        if(observed == null)
        if(!expected.getRelativeUri().equals(observed.getRelativeUri()))
            discrepencies.add(new RamlDiscrepancy(expected, observed, "Relative URI's do not match"));
        if(!expected.getParentUri().equals(observed.getParentUri()))
            discrepencies.add(new RamlDiscrepancy(expected, observed, "Parent URI's do not match"));
    }


    public boolean getCompareResources() {
        return compareResources;
    }

    public void setCompareResources(boolean compareResources) {
        this.compareResources = compareResources;
    }

}
