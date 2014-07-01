package ramlContractValidator.core.comparator;

import org.apache.maven.plugin.logging.Log;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by shendrickson1 on 7/1/14.
 *
 */
public class MapComparator<S, T> {

    // Left public to allow tree like traversal of valid maps post-compare
    private Map<S, T> validExpected = new LinkedHashMap<S, T>();
    private Map<S, T> validObserved = new LinkedHashMap<S, T>();
    private final Log logger;

    public MapComparator(Log logger) {
        this.logger = logger;
    }

    public List<RamlDiscrepancy> compare(Map<S, T> expected, Map<S, T> observed, String name) {
        List<RamlDiscrepancy> discrepancies = new LinkedList<RamlDiscrepancy>();

        if(expected != null && observed != null) {
            validExpected.putAll(expected);
            validObserved.putAll(observed);

            Map<S, T> observedBuffer = new LinkedHashMap<S, T>();
            observedBuffer.putAll(observed);


            for (Map.Entry<S, T> entry : expected.entrySet()) {
                T value = observed.get(entry.getKey());
                if (value == null) {
                    discrepancies.add(new RamlDiscrepancy<T>(entry.getValue(), null, name, "Could not find expected %s", logger));
                    validExpected.remove(entry.getKey());
                } else {
                    // TODO Do further compare
                    observedBuffer.remove(entry.getKey());
                }
            }

            for (Map.Entry<S, T> entry : observedBuffer.entrySet()) {
                discrepancies.add(new RamlDiscrepancy<T>(null, entry.getValue(), name, "Non-Specified %s observed", logger));
                validObserved.remove(entry.getKey());
            }
        } else if(expected != null && observed == null) {
            for (T value : expected.values()) {
                discrepancies.add(new RamlDiscrepancy<T>(value, null, name, "Could not find expected %s", logger));
            }
        } else if(expected == null && observed != null) {
            for (T value : observed.values()) {
                discrepancies.add(new RamlDiscrepancy<T>(null, value, name, "Non-Specified %s observed", logger));
            }
        } else {
            // Both null... OK
        }

        return discrepancies;
    }

    public Map<S, T> getValidExpected() {
        return validExpected;
    }

    public Map<S, T> getValidObserved() {
        return validObserved;
    }
}
