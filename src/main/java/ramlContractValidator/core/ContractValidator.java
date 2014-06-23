package ramlContractValidator.core;

import org.apache.maven.plugin.logging.Log;
import org.raml.model.Raml;

import javax.ws.rs.Path;
import java.lang.annotation.Annotation;

/**
 * Created by Scott on 6/21/14.
 */
public class ContractValidator {

    public void compare(Raml raml, Object resourceKlass, Log log) {
        log.info("Beginning Resource Comparison");

        getKlassResources(resourceKlass);
    }

    private void getKlassResources(Object resourceKlass) {
        System.out.println(resourceKlass.getClass().getComponentType().toString());
        String basePath = getBasePath(resourceKlass);

        System.out.println("BasePath: " + basePath);
    }

    private String getBasePath(Object resourceKlass) {
        for(Annotation annotation : resourceKlass.getClass().getAnnotations()) {
            System.out.println(annotation.toString());
            if(annotation instanceof Path) {
                System.out.println("Found path annotation");
                try {
                    return (String) annotation.getClass().getField("value").get(resourceKlass);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

}
