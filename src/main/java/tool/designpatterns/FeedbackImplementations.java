package tool.designpatterns;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;

import org.apache.commons.lang.NotImplementedException;

public final class FeedbackImplementations {

    private String message;

    public FeedbackImplementations(Statement statement) {
        message = getStringStart(statement);
    }

    public FeedbackImplementations(Expression expression) {
        message = getStringStart(expression);
    }

    public FeedbackImplementations(VariableDeclarator variable) {
        String msg = getStringStart(variable);
        msg += " for variable " + variable.getNameAsString();
    }

    public FeedbackImplementations(CallableDeclaration callable) {
        String msg = getStringStart(callable);
        msg += " in method " + callable.getNameAsString();
    }

    public FeedbackImplementations(TypeDeclaration type) {
        String msg = getStringStart(type);
        msg += " in method " + type.getNameAsString();
    }

    public FeedbackImplementations(FieldDeclaration field) {
        StringBuilder msg = new StringBuilder(getStringStart(field));
        msg.append(" in the field declerations for variables: \n");
        for (VariableDeclarator variable : field.getVariables()) {
            msg.append(" - " + variable.getNameAsString() + "\n");
        }
    }

    private String getStringStart(Node node) {
        if (!node.getRange().isPresent()) {
            return "";
        }

        Range range = node.getRange().get();
        return "On lines " + range.begin.line + " - " + range.end.line;
    }

    public FeedbackImplementations(Node node) {
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
