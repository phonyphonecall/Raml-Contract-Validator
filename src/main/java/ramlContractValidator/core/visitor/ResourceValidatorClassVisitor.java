package ramlContractValidator.core.visitor;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import org.apache.maven.plugin.logging.Log;
import org.raml.model.Raml;


/**
 * Created by shendrickson1 on 6/23/14.
 *
 */
public class ResourceValidatorClassVisitor extends AbstractValidatorVisitor {


    public ResourceValidatorClassVisitor(Raml resourceRaml, Log logger) {
        super(resourceRaml, logger);
    }

    // FIXME only supports Resources with class level path
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
        if (n.getAnnotations() != null) {
            for (AnnotationExpr annotation : n.getAnnotations()) {
                if(annotation.getName().getName().equals("Path")) {
                    String path = getPathValue(annotation);
                    addBaseResourcePath(path);
                }
            }
        } else {
            logger.error("Failed to find path annotations in specified resource");
            throw new RuntimeException("Failed to find path annotations in specified resource");
        }
    }

}
