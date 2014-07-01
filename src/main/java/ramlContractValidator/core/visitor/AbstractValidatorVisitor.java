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

    private String currentBaseResourcePath;
    // For convenience we will buffer up all resources in project here
    private Map<String, Resource> allResources = new HashMap<String, Resource>();

    public AbstractValidatorVisitor(Raml resourceRaml, Log logger) {
        this.resourceRaml = resourceRaml;
        this.logger = logger;

        // Fill currentBaseResourcePath if possible
        if (resourceRaml.getResources() != null) {
            if (!resourceRaml.getResources().isEmpty()) {
                currentBaseResourcePath = resourceRaml.getMediaType();
                for (Entry<String, Resource> entry : resourceRaml.getResources().entrySet()) {
                    allResources.put(entry.getKey(), entry.getValue());
                    fillAllResources(entry.getValue());
                }
            }
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
        Resource baseResource = resourceRaml.getResource(currentBaseResourcePath);

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
                logger.error("This is illegal use of annotations");
                logger.error("Must be fixed before building");
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
        currentBaseResourcePath = path;
        resourceRaml.setMediaType(path);
        Resource baseResource = new Resource();
        baseResource.setRelativeUri(path);
        baseResource.setParentUri("");
        resourceRaml.getResources().put(path, baseResource);
        allResources.put(path, baseResource);
    }

    protected void addBaseResourcePathAction(ActionType actionType, Map<String, QueryParameter> queryParams) {
        logger.debug("Adding base path action: " + actionType.name());
        Action action = new Action();
        action.setType(actionType);
        Resource baseResource = resourceRaml.getResource(currentBaseResourcePath);
        action.setResource(baseResource);
        action.setQueryParameters(queryParams);

        for(Action existingAction : baseResource.getActions().values()) {
            if(existingAction.getType().equals(actionType)) {
                logger.debug("Multiple " + actionType.name() + "\'s detected at path: " + baseResource.getUri());
                logger.error("This is illegal use of annotations");
                logger.error("Must be fixed before building");
                throw new RuntimeException("Multiple " + actionType.name() + "\'s detected at path: " + baseResource.getUri());
            }
        }

        baseResource.getActions().put(actionType, action);
    }
}
