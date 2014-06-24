package ramlContractValidator.core.visitor;

import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import org.apache.maven.plugin.logging.Log;
import org.raml.model.ActionType;
import org.raml.model.Raml;


/**
 * Created by shendrickson1 on 6/23/14.
 *
 */
public class ResourceValidatorMethodVisitor extends AbstractValidatorVisitor {

    public ResourceValidatorMethodVisitor(Raml resourceRaml, Log logger) {
        super(resourceRaml, logger);
    }


    @Override
    public void visit(MethodDeclaration n, Object arg) {
        AnnotationExpr path = null;
        ActionType action = null;
        if (n.getAnnotations() != null) {
            for (AnnotationExpr annotation : n.getAnnotations()) {
                if(annotation.getName().getName().equals("Path")) {
                    path = annotation;
                }
                if(annotation.getName().getName().equals("GET")) {
                    action = ActionType.GET;
                }
                if(annotation.getName().getName().equals("PUT")) {
                    action = ActionType.PUT;
                }
                if(annotation.getName().getName().equals("POST")) {
                    action = ActionType.POST;
                }
                if(annotation.getName().getName().equals("DELETE")) {
                    action = ActionType.DELETE;
                }
                if(annotation.getName().getName().equals("PATCH")) {
                    action = ActionType.PATCH;
                }
                if(annotation.getName().getName().equals("OPTIONS")) {
                    action = ActionType.OPTIONS;
                }
                if(annotation.getName().getName().equals("HEAD")) {
                    action = ActionType.HEAD;
                }
                if(annotation.getName().getName().equals("TRACE")) {
                    action = ActionType.TRACE;
                }
            }

            /*
             * Four cases:
             *  0: Method is not a path method
             *  1: Method has path but no action: Disallowed
             *  2: Method has path and action: add to RAML
             *  3: Method has action but no path: base path action assumed
             */
            if (path == null && action == null) {
                return;
            } else if (path != null && action == null) {
                throw new RuntimeException("Path missing action type at: " + getPathValue(path));
            } else if (path == null && action != null) {
                addBaseResourcePathAction(action);
            } else {
                addPath(getPathValue(path), action);
            }
        }
    }

}
