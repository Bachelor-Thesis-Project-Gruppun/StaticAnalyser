package patternverifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class ImmutableVerifier implements IPatternVerifier {

    public ImmutableVerifier() {
    }

    /**
     * Verifies if every class in the given compilationalunit is immutable.
     */
    @Override
    public boolean verify(CompilationUnit cu) {
        System.out.println();
        Map<ClassOrInterfaceDeclaration, Boolean> classImmutableMap = new HashMap();
        cu.findAll(ClassOrInterfaceDeclaration.class).stream().forEach(c -> {
            classImmutableMap.put(c, verifyClass(c));
        });

        return false;
    }

    /**
     * Verifies if the given class is immutable.
     *
     * @param c the class to verify.
     *
     * @return whether or not the class is immutable.
     */
    private boolean verifyClass(ClassOrInterfaceDeclaration c) {
        List<FieldDeclaration> fields = c.getFields();
        Map<FieldDeclaration, Boolean> varImmutableMap = new HashMap<>();
        fields.stream().forEach(field -> {
            field.getVariables().stream().forEach(var -> {
                varImmutableMap.put(field, verifyField(field, c));
            });
        });

        return false;
    }

    /**
     * Verifies if the given field is immutable.
     *
     * @param field the field to verify.
     * @param c     the class the field is contained in.
     *
     * @return whether or not the class is immutable.
     */
    private boolean verifyField(FieldDeclaration field, ClassOrInterfaceDeclaration c) {
        if (field.hasModifier(Modifier.Keyword.STATIC) || field.hasModifier(
            Modifier.Keyword.FINAL)) {
            return true;
        }

        if (field.hasModifier(Modifier.Keyword.PRIVATE)) {
            Map<MethodDeclaration, Boolean> methodMutatesField = new HashMap();
            List<MethodDeclaration> methods = c.getMethods();
            field.getVariables().stream().forEach(var -> {
                methods.stream().forEach(method -> {
                    methodMutatesField.put(method, isAssignedIn(var, method));
                });
            });
        }
        return false;
    }

    /**
     * Returns whether the given variable is ever assigned in the given method
     *
     * @param variable the variable to check.
     * @param method   the method to check in.
     *
     * @return if the variable was assigned in the method.
     */
    private boolean isAssignedIn(VariableDeclarator variable, MethodDeclaration method) {
        BlockStmt methodBody = method.getBody().get();

        NodeList<Statement> statements = methodBody.getStatements();
        Map<VariableDeclarator, Boolean> varImmutableMap = new HashMap<>();
        // A list of variables that have been declared locally in the method this far.
        List<String> locallyDeclaredVariables = new ArrayList<>();

        for (Statement statement : statements) {
            if (statement.isExpressionStmt()) {
                Expression expr = statement.asExpressionStmt().getExpression();

                // Check if the expression is the declaration of a local variable.
                if (expr.isVariableDeclarationExpr()) {
                    VariableDeclarationExpr varDecExpr = expr.asVariableDeclarationExpr();
                    varDecExpr.getVariables().stream().forEach(var -> {
                        // Add the variable to the list of declared variables.
                        locallyDeclaredVariables.add(var.getNameAsString());
                    });
                }

                if (expr.isAssignExpr()) {
                    AssignExpr aExpr = expr.asAssignExpr();
                    Expression target = aExpr.getTarget();

                    if (target.isNameExpr()) {
                        NameExpr accessedVar = target.asNameExpr();
                        String name = accessedVar.getNameAsString();

                        // Compare the variables by name.
                        if (variable.getNameAsString().equals(name)) {
                            // The variable being assigned has the same name as the variable
                            // we're checking for.
                            if (locallyDeclaredVariables.contains(name) == false) {
                                // The variable being assigned is not a local variable with the
                                // same name as the one we're looking for.
                                return true;
                            }

                        }
                    }
                }
            }
        }
        return false;
    }
}
