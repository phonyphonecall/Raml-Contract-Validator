package ramlContractValidator.core.visitor;

import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.apache.maven.plugin.logging.Log;
import org.raml.model.Raml;
import org.raml.model.Resource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by shendrickson1 on 6/23/14.
 *
 */
public class AbstractValidatorVisitor extends VoidVisitorAdapter {

    Log logger;

    private Raml resourceRaml;

    private Resource baseResource;
    // For convenience we will buffer up all resources in project here
    private Map<String, Resource> allResources = new HashMap<String, Resource>();

    public AbstractValidatorVisitor(Raml resourceRaml, Log logger) {
        this.resourceRaml = resourceRaml;
        this.logger = logger;

        // Fill baseResource if possible
        if (resourceRaml.getResources() != null) {
            if (!resourceRaml.getResources().isEmpty()) {
                for (Entry<String, Resource> entry : resourceRaml.getResources().entrySet()) {
                    baseResource = entry.getValue();
                    allResources.put(entry.getKey(), entry.getValue());
                }
            }
        }

        if(baseResource != null) {
            fillAllResources(baseResource);
        }
    }

    // Recurse and fill
    private void fillAllResources(Resource resource) {
        if(resource.getResources() != null) {
            for(Entry<String, Resource> entry : resource.getResources().entrySet()) {
                if(entry != null) {
                    allResources.put(entry.getKey(), entry.getValue());
                    fillAllResources(entry.getValue());
                }
            }
        }
    }

    protected void addPath(String value) {
        String[] paths = value.split("/");
        StringBuilder sb = new StringBuilder();
        String parentPath = "";

        if(baseResource != null) {
            sb.append(baseResource.getRelativeUri());
            parentPath = sb.toString();
        }

        for (String path : paths) {
            if(path == null || path.isEmpty())
                continue;
            String fullPath = sb.toString() + "/" + path;
            if (!allResources.containsKey(fullPath)) {
                logger.debug("Working on full path: " + fullPath);
                logger.debug("RCV: Adding resource \'/" + path + "\' to parent: \'" + parentPath + "\'");
                Resource resource = new Resource();
                resource.setParentUri(parentPath);
                resource.setRelativeUri("/" + path);
                logger.debug("Setting parent Resource: " + allResources.get(sb.toString()));
                resource.setParentResource(allResources.get(sb.toString()));
                resource.getParentResource().getResources().put("/" + path, resource);
                // Continue to buffer new resources
                allResources.put(fullPath, resource);
            }
            sb.append("/");
            sb.append(path);
            parentPath = path;
        }
    }

    /**
     * String twiddling to get path values
     */
    protected String getPathValue(AnnotationExpr annotation) {
        String dump = annotation.toString();
        int start = dump.indexOf("\"");
        int end = dump.indexOf("\"", start + 1);
        return dump.substring(start + 1, end);
    }


    protected void addBaseResourcePath(String path) {
        baseResource = new Resource();
        baseResource.setRelativeUri(path);
        baseResource.setParentUri("");
        Map<String, Resource> baseResources = new LinkedHashMap<String, Resource>();
        baseResources.put(path, baseResource);
        resourceRaml.setResources(baseResources);
        allResources.put(path, baseResource);
    }
}
