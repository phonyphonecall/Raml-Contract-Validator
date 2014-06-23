package ramlContractValidator.core.comparator;

import org.raml.model.Resource;

/**
 * Created by shendrickson1 on 6/23/14.
 *
 */
public class RamlDiscrepancy {

    private String message;
    private Resource expected,
                     observed;

    public RamlDiscrepancy(Resource expected, Resource observed, String message) {
        this.expected = expected;
        this.observed = observed;
        this.message = message;
    }

    @Override
    public String toString() {
        // TODO this is sloppy printing
        String expectedPrint = "";
        String observedPrint = "";
        if(expected != null)
            expectedPrint = expected.getUri();
        if(observed != null)
            observedPrint = observed.getUri();

        return message + " | expected resource: " + expectedPrint + " | observed resource: " + observedPrint;

    }
}
