package tool.designpatterns.verifiers.singleclassverifiers.immutable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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

import tool.designpatterns.verifiers.IPatternVerifier;
import tool.util.Feedback;

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
        Map<ClassOrInterfaceDeclaration, Feedback> classImmutableMap = new ConcurrentHashMap<>();
        compUnit.findAll(ClassOrInterfaceDeclaration.class).stream().forEach(c -> {
            classImmutableMap.put(c, verifyClass(c));
        });

        boolean verifySuccessful = true;
        StringBuilder message = new StringBuilder();
        for (Feedback feedback : classImmutableMap.values()) {
            if (!feedback.getValue()) {
                verifySuccessful = false;
                message.append('\n');
                message.append(feedback.getMessage());
            }
        }

        return new Feedback(verifySuccessful, message.toString());
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
        Map<FieldDeclaration, Feedback> varImmutableMap = new ConcurrentHashMap<>();
        fields.stream().forEach(field -> {
            field.getVariables().stream().forEach(var -> {
                varImmutableMap.put(field, verifyField(field, classOrI));
            });
        });

        boolean verifySuccessful = true;
        String message = "";
        Iterator<Map.Entry<FieldDeclaration, Feedback>> iterator =
            varImmutableMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<FieldDeclaration, Feedback> entry = iterator.next();
            Feedback feedback = entry.getValue();
            if (!feedback.getValue()) {
                verifySuccessful = false;
                message =
                    "Verification failed for class '" + classOrI.getNameAsString() + "' due to \n" +
                    feedback.getMessage();
            }
        }
        return new Feedback(verifySuccessful, message);
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
            return new Feedback(true);
        }

        if (field.hasModifier(Modifier.Keyword.PUBLIC)) {
            return new Feedback(false, "Field " + field.toString() + " is public");
        }

        Map<MethodDeclaration, Feedback> methodMutates = new ConcurrentHashMap();
        if (field.hasModifier(Modifier.Keyword.PRIVATE)) {
            List<MethodDeclaration> methods = classOrI.getMethods();
            field.getVariables().stream().forEach(var -> {
                methods.stream().forEach(method -> {
                    methodMutates.put(method, isAssignedIn(var, method));
                });
            });
        }

        boolean verifySuccessful = true;
        String message = "";

        Iterator<Map.Entry<MethodDeclaration, Feedback>> iterator =
            methodMutates.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<MethodDeclaration, Feedback> entry = iterator.next();
            Feedback feedback = entry.getValue();
            if (feedback.getValue()) {
                verifySuccessful = false;
                message = "Verification failed for field '" + field.toString() + "' due to \n" +
                          feedback.getMessage();
            }
        }
        return new Feedback(verifySuccessful, message);
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
        BlockStmt methodBody = method.getBody().get();

        NodeList<Statement> statements = methodBody.getStatements();
        // A list of variables that have been declared locally in the method this far.
        List<String> localVars = new ArrayList<>();

        for (Statement statement : statements) {
            if (statement.isExpressionStmt()) {
                Expression expr = statement.asExpressionStmt().getExpression();

                // Check if the expression is the declaration of a local variable.
                if (expr.isVariableDeclarationExpr()) {
                    VariableDeclarationExpr varDecExpr = expr.asVariableDeclarationExpr();
                    varDecExpr.getVariables().stream().forEach(var -> {
                        // Add the variable to the list of declared variables.
                        localVars.add(var.getNameAsString());
                    });
                }

                Feedback feedback = isVariableAssignment(expr, variable, localVars);
                if (feedback.getValue()) {
                    return new Feedback(
                        true, feedback.getMessage() + " in method '" + method.getNameAsString() +
                              "'\n");
                }
            }
        }

        return new Feedback(false,
                            "Variable " + variable.getNameAsString() + " is not assigned " + "in " +
                            method.getNameAsString());
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

                    return new Feedback(true,
                                        "Variable '" + name + "' is assigned on '" + line + "'\n");

                }
            }
        }
        return new Feedback(
            false, "Variable '" + variable.getNameAsString() + "' is not assigned in expression '" +
                   expr.toString() + "'");
    }
}
