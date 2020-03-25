package tool.feedback;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

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
     * Feedback implementations for callables (methods & constructors).
     *
     * @param callable the callable.
     */
    public FeedbackTrace(CallableDeclaration callable) {
        StringBuilder msg = new StringBuilder(getStringStart(callable));
        msg.append(" in method ").append(callable.getNameAsString());
        message = msg.toString();
    }

    /**
     * Feedback implementations for types (classes).
     *
     * @param type the type.
     */
    public FeedbackTrace(TypeDeclaration type) {
        StringBuilder msg = new StringBuilder(getStringStart(type));
        msg.append(" in class ").append(type.getNameAsString());
        message = msg.toString();
    }

    /**
     * Feedback implementations for fields.
     *
     * @param field the field.
     */
    public FeedbackTrace(FieldDeclaration field) {
        StringBuilder msg = new StringBuilder(getStringStart(field));
        msg.append(" in the field declerations for variables: ");
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

    public FeedbackTrace(ClassOrInterfaceType type) {
        message = getStringStart(type);
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
