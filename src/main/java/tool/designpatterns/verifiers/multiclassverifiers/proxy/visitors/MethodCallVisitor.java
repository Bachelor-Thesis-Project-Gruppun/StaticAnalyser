package tool.designpatterns.verifiers.multiclassverifiers.proxy.visitors;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import org.apache.commons.lang.NotImplementedException;
import tool.feedback.Feedback;

/**
 * A visitor that visits all MethodCall Expressions. Whilst visiting also verifies if the method
 * calls upon a third method on a variable both given in the constructor.
 */
public class MethodCallVisitor extends GenericVisitorAdapter<Feedback, Void> {

    private final VariableDeclarator other;
    private final MethodDeclaration otherMethod;

    public MethodCallVisitor(VariableDeclarator other, MethodDeclaration otherMethod) {
        this.other = other;
        this.otherMethod = otherMethod;
    }

    @Override
    public Feedback visit(MethodCallExpr n, Void arg) {
        // Found a method call
        System.out.println(n.getScope() + " - " + n.getName());
        // Don't forget to call super, it may find more method calls inside the arguments of this
        // method call, for example.
        super.visit(n, arg);

        throw new NotImplementedException();
    }
}
