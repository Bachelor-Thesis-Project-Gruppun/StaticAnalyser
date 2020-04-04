package tool.designpatterns.verifiers.multiclassverifiers.proxy.visitors;

import java.util.Optional;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * A visitor that visits all MethodCall Expressions. Whilst visiting also verifies if the method
 * calls upon a third method on a variable both given in the constructor.
 */
public class MethodCallVisitor extends GenericVisitorAdapter<Boolean, Void> {

    private final VariableDeclarator other;
    private final MethodDeclaration otherMethod;

    public MethodCallVisitor(VariableDeclarator other, MethodDeclaration otherMethod) {
        this.other = other;
        this.otherMethod = otherMethod;
    }

    @Override
    public Boolean visit(MethodCallExpr n, Void arg) {
        // Compare if the method is called on the correct variable (type)
        Optional<Expression> optExpr = n.getScope();
        System.out.println("checking out " + n.toString());
        if (optExpr.isPresent()) {
            ResolvedType resolvedVarType = optExpr.get().calculateResolvedType();
            ResolvedType otherType = other.resolve().getType();
            if (resolvedVarType.equals(otherType)) {
                return true;
            }
        }

        // Check if the method called is the same as the one we're looking for.
        ResolvedMethodDeclaration resolvedMethod = n.resolve();
        ResolvedMethodDeclaration resolvedOtherMethod = otherMethod.resolve();

        if (resolvedMethod.equals(resolvedOtherMethod)) {
            return true;
        }

        // Check if any other method call is valid, if there are no more it will return null.
        return super.visit(n, arg);
    }

}
