package tool.designpatterns.verifiers.multiclassverifiers.proxy.visitors;

import java.util.Optional;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * A visitor that visits all MethodCall Expressions. Whilst visiting also verifies if the method
 * calls upon a third method on a variable both given in the constructor.
 */
public class MethodCallVisitor extends GenericVisitorAdapter<Boolean, Void> {

    private final ResolvedReferenceTypeDeclaration otherType;
    private final MethodDeclaration otherMethod;

    /**
     * Creates a new MethodCallVisitor.
     *
     * @param otherType   the type to look for.
     * @param otherMethod the other method that should be called.
     */
    public MethodCallVisitor(
        ResolvedReferenceTypeDeclaration otherType, MethodDeclaration otherMethod) {
        super();

        this.otherType = otherType;
        this.otherMethod = otherMethod;
    }

    @Override
    public Boolean visit(MethodCallExpr methodCall, Void arg) {

        // Check if the method called is the same as the one we're looking for.
        String calledMethod = methodCall.resolve().getQualifiedName();
        String correctMethod = otherMethod.resolve().getQualifiedName();

        if (calledMethod.equals(correctMethod) && calledOnCorrectType(methodCall)) {
            return true;
        }

        // Check if any other method call is valid, if there are no more it will return null.
        return super.visit(methodCall, arg);
    }

    /**
     * Method that checks if the given methodCall occurs on the type stored in the variable other.
     *
     * @param methodCall the method call expression to verify.
     *
     * @return if the method call occurs on the specified type.
     */
    private boolean calledOnCorrectType(MethodCallExpr methodCall) {
        Optional<Expression> optExpr = methodCall.getScope();
        if (optExpr.isPresent()) {
            ResolvedType resolvedVarType = optExpr.get().calculateResolvedType();
            if (resolvedVarType.isReferenceType()) {
                ResolvedReferenceTypeDeclaration varTypeDec =
                    resolvedVarType.asReferenceType().getTypeDeclaration();

                return varTypeDec.equals(otherType);
            }
        }
        return false;
    }
}


