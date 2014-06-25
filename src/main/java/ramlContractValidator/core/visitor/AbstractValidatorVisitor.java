package ramlContractValidator.core.visitor;

import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.apache.maven.plugin.logging.Log;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.parameter.QueryParameter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by shendrickson1 on 6/23/14.
 *
 * @author Scott Hendrickson
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

    protected void addPath(String value, ActionType actionType, Map<String, QueryParameter> queryParams) {
        Action action = new Action();
        action.setType(actionType);
        action.setQueryParameters(queryParams);

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
                logger.debug("Adding resource \'/" + path + "\' to parent: \'" + parentPath + "\'");
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
            parentPath = sb.toString();
        }

        // Make sure to put action once we find resources
        Resource resource = allResources.get(sb.toString());
        action.setResource(resource);

        for(Action existingAction : resource.getActions().values()) {
            if(existingAction.getType().equals(actionType)) {
                logger.debug("Multiple " + actionType.name() + "\'s detected at path: " + resource.getUri());
                throw new RuntimeException("Multiple " + actionType.name() + "\'s detected at path: " + resource.getUri());
            }
        }

        resource.getActions().put(actionType, action);
    }

    /**
     * String twiddling to get annotation values
     */
    protected String getValue(AnnotationExpr annotation) {
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

    protected void addBaseResourcePathAction(ActionType actionType, Map<String, QueryParameter> queryParams) {
        logger.debug("Adding base path action: " + actionType.name());
        Action action = new Action();
        action.setType(actionType);
        action.setResource(baseResource);
        action.setQueryParameters(queryParams);

        for(Action existingAction : baseResource.getActions().values()) {
            if(existingAction.getType().equals(actionType)) {
                logger.debug("Multiple " + actionType.name() + "\'s detected at path: " + baseResource.getUri());
                throw new RuntimeException("Multiple " + actionType.name() + "\'s detected at path: " + baseResource.getUri());
            }
        }

        baseResource.getActions().put(actionType, action);
    }
}
