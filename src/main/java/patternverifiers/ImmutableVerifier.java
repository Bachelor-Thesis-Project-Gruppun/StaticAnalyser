package patternverifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

/**
 * A verifier for the immutable pattern.
 */
public class ImmutableVerifier implements IPatternVerifier {

    public ImmutableVerifier() {
    }

    /**
     * Verifies if every class in the given compilationalUnit is immutable.
     */
    @Override
    public Feedback verify(CompilationUnit compUnit) {
        List<Feedback> childFeedbacks = new ArrayList<>();

        compUnit.findAll(ClassOrInterfaceDeclaration.class).stream().forEach(c -> {
            childFeedbacks.add(verifyClass(c));
        });

        return Feedback.getFeedbackWithChildren(new FeedbackImplementations(compUnit),
                                                childFeedbacks);
    }

    /**
     * Verifies if the given class is immutable.
     *
     * @param classOrI the class to verify.
     *
     * @return whether or not the class is immutable.
     */
    private Feedback verifyClass(ClassOrInterfaceDeclaration classOrI) {
        List<FieldDeclaration> fields = classOrI.getFields();
        List<Feedback> childFeedbacks = new ArrayList<>();

        fields.forEach(field -> {
            field.getVariables().forEach(var -> {
                childFeedbacks.add(verifyField(field, classOrI));
            });
        });

        return Feedback.getFeedbackWithChildren(
            new FeedbackImplementations(classOrI), childFeedbacks);
    }

    /**
     * Verifies if the given field is immutable.
     *
     * @param field    the field to verify.
     * @param classOrI the class the field is contained in.
     *
     * @return whether or not the class is immutable.
     */
    private Feedback verifyField(FieldDeclaration field, ClassOrInterfaceDeclaration classOrI) {
        if (field.hasModifier(Modifier.Keyword.STATIC) || field.hasModifier(
            Modifier.Keyword.FINAL)) {
            return Feedback.getNoErrorFeedback();
        }

        if (field.hasModifier(Modifier.Keyword.PUBLIC)) {
            return Feedback.getNoChildFeedback("public field.", new FeedbackImplementations(field));
        }

        List<Feedback> childFeedbacks = new ArrayList<>();

        if (field.hasModifier(Modifier.Keyword.PRIVATE)) {
            List<MethodDeclaration> methods = classOrI.getMethods();
            field.getVariables().forEach(var -> {
                methods.forEach(method -> {
                    childFeedbacks.add(isAssignedIn(var, method));
                });
            });
        }

        return Feedback.getFeedbackWithChildren(new FeedbackImplementations(field), childFeedbacks);
    }

    /**
     * Returns whether the given variable is ever assigned in the given method.
     *
     * @param variable the variable to check.
     * @param method   the method to check in.
     *
     * @return if the variable was assigned in the method.
     */
    @SuppressWarnings("PMD.LinguisticNaming")
    private Feedback isAssignedIn(VariableDeclarator variable, MethodDeclaration method) {
        BlockStmt methodBody;
        if (method.getBody().isPresent()) {
            methodBody = method.getBody().get();
        } else {
            // An empty method does not mutate the class...? 0.o
            return Feedback.getNoErrorFeedback();
        }

        NodeList<Statement> statements = methodBody.getStatements();
        // A list of variables that have been declared locally in the method this far.
        List<String> localVars = new ArrayList<>();

        List<Feedback> childFeedbacks = new ArrayList<>();

        for (Statement statement : statements) {
            if (statement.isExpressionStmt()) {
                Expression expr = statement.asExpressionStmt().getExpression();

                // Check if the expression is the declaration of a local variable.
                if (expr.isVariableDeclarationExpr()) {
                    VariableDeclarationExpr varDecExpr = expr.asVariableDeclarationExpr();
                    varDecExpr.getVariables().forEach(var -> {
                        // Add the variable to the list of declared variables.
                        localVars.add(var.getNameAsString());
                    });
                }

                childFeedbacks.add(isVariableAssignment(expr, variable, localVars));
            }
        }

        return Feedback.getFeedbackWithChildren(new FeedbackImplementations(method),
                                                childFeedbacks);
    }

    /**
     * Is the given expression a variable assignment for the given variableDeclarator.
     *
     * @param expr      The expression to check.
     * @param variable  The variable declaration to check against.
     * @param localVars A list of variables that are in a local scope and therefore not the same
     *                  variable.
     */
    @SuppressWarnings({"PMD.LinguisticNaming", "PMD.AvoidDeeplyNestedIfStmts"})
    private Feedback isVariableAssignment(
        Expression expr, VariableDeclarator variable, List<String> localVars) {
        if (expr.isAssignExpr()) {
            AssignExpr assignExpr = expr.asAssignExpr();
            Expression target = assignExpr.getTarget();

            if (target.isNameExpr()) {
                NameExpr accessedVar = target.asNameExpr();
                String name = accessedVar.getNameAsString();

                // Compare the variables by name.
                if (variable.getNameAsString().equals(name) && !localVars.contains(name)) {
                    // The variable being assigned has the same name as the variable
                    // we're checking for.
                    String line = "";
                    Optional<Range> lines = accessedVar.getRange();
                    if (lines.isPresent()) {
                        line = lines.get().toString();
                    }

                    return Feedback.getNoChildFeedback("Variable '" + name + "' is assigned.",
                                                       new FeedbackImplementations(assignExpr));

                }
            }
        }

        return Feedback.getNoErrorFeedback();
    }
}
