package ramlContractValidator.core.visitor;

import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import org.apache.maven.plugin.logging.Log;
import org.raml.model.Raml;


/**
 * Created by shendrickson1 on 6/23/14.
 */
public class ResourceValidatorMethodVisitor extends AbstractValidatorVisitor {

    public ResourceValidatorMethodVisitor(Raml resourceRaml, Log logger) {
        super(resourceRaml, logger);
    }


    @Override
    public void visit(MethodDeclaration n, Object arg) {
        if (n.getAnnotations() != null) {
            for (AnnotationExpr annotation : n.getAnnotations()) {
                if(annotation.getName().getName().equals("Path")) {
                    String path = getPathValue(annotation);
                    addPath(path);
                }
            }
        }
    }
}
