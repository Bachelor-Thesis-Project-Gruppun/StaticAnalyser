package tool.feedback;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;

import org.apache.commons.lang.NotImplementedException;
import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

/**
 * A ''stacktrace'' class made to track where errors occur when traversing the javaparser AST.
 */
@DesignPattern(pattern = {Pattern.IMMUTABLE})
public final class FeedbackTrace {

    private String message;

    /**
     * Feedback implementations for statements.
     *
     * @param statement the statement.
     */
    public FeedbackTrace(Statement statement) {
        message = getStringStart(statement);
    }

    /**
     * Feedback implementations for expressions.
     *
     * @param expression the expression.
     */
    public FeedbackTrace(Expression expression) {
        message = getStringStart(expression);
    }

    /**
     * Feedback implementations for variables.
     *
     * @param variable the variable.
     */
    public FeedbackTrace(VariableDeclarator variable) {
        StringBuilder msg = new StringBuilder(getStringStart(variable));
        msg.append(" for variable ").append(variable.getNameAsString());
        message = msg.toString();
    }

    /**
     * Feedback implementations for constructors.
     *
     * @param constructor the constructor.
     */
    public FeedbackTrace(ConstructorDeclaration constructor) {
        StringBuilder msg = new StringBuilder(getStringStart(constructor));
        ResolvedConstructorDeclaration resolvedConstructor = constructor.resolve();
        msg.append(" in constructor ").append(resolvedConstructor.getQualifiedName());
        message = msg.toString();
    }

    /**
     * Feedback implementations for methods.
     *
     * @param method the method.
     */
    public FeedbackTrace(MethodDeclaration method) {
        StringBuilder msg = new StringBuilder(getStringStart(method));
        ResolvedMethodDeclaration resolvedMethod = method.resolve();
        msg.append(" in method ").append(resolvedMethod.getQualifiedName());
        message = msg.toString();
    }

    /**
     * Feedback implementations for types (classes etc).
     *
     * @param type the type.
     */
    public FeedbackTrace(TypeDeclaration type) {
        StringBuilder msg = new StringBuilder(getStringStart(type));
        ResolvedTypeDeclaration resolvedType = type.resolve();
        msg.append(" in class ").append(resolvedType.getQualifiedName());
        message = msg.toString();
    }

    /**
     * Feedback implementations for fields.
     *
     * @param field the field.
     */
    public FeedbackTrace(FieldDeclaration field) {
        StringBuilder msg = new StringBuilder(getStringStart(field));
        msg.append(" in the field declarations for variables: ");
        boolean comma = false;
        for (VariableDeclarator variable : field.getVariables()) {
            if (comma) {
                msg.append(", ");
            }
            msg.append(variable.getNameAsString());
            comma = true;
        }
        message = msg.toString();
    }

    private String getStringStart(Node node) {
        if (!node.getRange().isPresent()) {
            return "";
        }

        Range range = node.getRange().get();
        return "On lines " + range.begin.line + " - " + range.end.line;
    }

    /**
     * Feedback implementations for nodes, this should not be used! Will always throw an
     * NotImplementedException as more specific node subtypes should be used instead. If they do not
     * currently exist, please implement a constructor for them.
     *
     * @param node the node.
     *
     * @exception NotImplementedException always thrown, read method description.
     */
    public FeedbackTrace(Node node) {
        // If this happens, look over why the given object was casted as a node and possible add
        // a constructor for it / one of its super classes.
        throw new NotImplementedException(
            "The given type is not yet supported, here follows the entire node: \n\n" +
            node.toString());
    }

    @Override
    public String toString() {
        return message;
    }
}
