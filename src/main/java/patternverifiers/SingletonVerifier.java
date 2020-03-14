package patternverifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.IfStmt;

import base.VariableReader;

/**
 * A verifier for the singleton pattern.
 */
public class SingletonVerifier implements IPatternVerifier {

    private final transient List<ConstructorDeclaration> constDeclList = new ArrayList<>();
    private transient FieldDeclaration instanceVar;
    private transient boolean isInstantiated;       // Tbh idk what transient means but PMD would
    // not let it work without it.

    public SingletonVerifier() {
    }

    @Override
    public Feedback verify(CompilationUnit compUnit) {
        boolean isValid = callsConstructor(compUnit) && hasAStaticInstance(compUnit) &&
                          onlyInstantiatedIfNull(compUnit) && hasPrivateConstructor(compUnit) &&
                          isInstantiated;

        return new Feedback(isValid);
    }

    /**
     * Method for checking if all the constructors in a Java class are private.
     *
     * @param compUnit The CompilationUnit representing the Java class to look at
     *
     * @return True iff all constructors are private
     */
    public boolean hasPrivateConstructor(CompilationUnit compUnit) {
        List<Boolean> listOfIsPrivate = new ArrayList<>();
        boolean isPrivConstr = true;

        // Finds and checks if the constructors are private or not.
        compUnit.findAll(ConstructorDeclaration.class).forEach(constructor -> {
            listOfIsPrivate.add(constructor.isPrivate());
        });

        // Takes all boolean values and ANDs them to one value.
        for (Boolean bool : listOfIsPrivate) {
            isPrivConstr &= bool;
        }

        return isPrivConstr;
    }

    /**
     * Method for declaring if a java class holds a field variable of a static instance
     * https://stackoverflow .com/questions/53300710/how-to-parse-inner-class-from-java-source-code
     * might help solving a check for inner classes
     *
     * @param compUnit The CompilationUnit representing the java class to look at
     *
     * @return True iff the java class holds a field variable with a static modifier of the same
     *     type as the class itself (eg. static SingletonVerifier sv;)
     */
    public boolean hasAStaticInstance(CompilationUnit compUnit) {
        boolean isStatic = false;
        boolean isPrivate = false;

        for (FieldDeclaration fieldDeclaration : VariableReader.readVariables(compUnit)) {
            if (fieldDeclaration.getVariables().get(0).getType().toString().equals(
                compUnit.getType(0).getNameAsString())) {
                if (isStatic && isPrivate) {
                    return false;
                }
                if (!fieldDeclaration.getVariables().get(0).getInitializer().isEmpty()) {
                    isInstantiated = true;
                }
                for (Modifier md : fieldDeclaration.getModifiers()) {
                    if (md.getKeyword().asString().equals("static")) {
                        isStatic = true;
                    } else if (md.getKeyword().asString().equals("private")) {
                        isPrivate = true;
                    }
                }
                instanceVar = fieldDeclaration;
            }
        }
        return isStatic && isPrivate;
    }

    /**
     * Method for declaring if a java class has a getInstance() method which calls the constructor
     * of the Singleton class, does not support a Singleton that is initialized at variable
     * declaration in the Instance field.
     *
     * @param compUnit The CompilationUnit representing the java class to look at
     *
     * @return
     */
    public boolean callsConstructor(CompilationUnit compUnit) {
        boolean instanceMethod = true;
        List<MethodDeclaration> methods = new ArrayList<>();
        compUnit.findAll(MethodDeclaration.class).forEach(methodDeclaration -> {
            methods.add(methodDeclaration);
        });
        for (MethodDeclaration declaration : methods) {
            if (declaration.getTypeAsString().equals(compUnit.getPrimaryTypeName().get())) {
                if (declaration.isStatic()) {
                    if (declaration.isPrivate()) {
                        compUnit.findAll(ConstructorDeclaration.class).forEach(
                            constructor -> constDeclList.add(constructor));
                        instanceMethod &= isMethodCalledFromPublic(methods, declaration);   //
                    } else {
                        instanceMethod &= true;
                    }
                } else {
                    instanceMethod = false;
                }
            }
        }
        return instanceMethod;
    }

    /**
     * Method to look for a constructor call, might be useful to differentiate between different
     * types of singletons in the future, currently unused.
     *
     * @param declaration A method to look inside to see if there is a ConstructorCall
     * @param constructor A ConstructorDeclaration for the class
     *
     * @return If a method is calling a specific constructor
     */
    public boolean findConstructorCall(
        MethodDeclaration declaration, ConstructorDeclaration constructor) {
        List<ObjectCreationExpr> calledMethods = new ArrayList<>();
        declaration.findAll(ObjectCreationExpr.class).forEach(methodDeclaration -> {
            if (methodDeclaration.getTypeAsString().equals(constructor.getNameAsString())) {
                calledMethods.add(methodDeclaration);
            }
        });
        return !calledMethods.isEmpty();
    }

    /**
     * Method returns whether a method is called from a public method in the same class or not.
     * Calls itself recursively until no more calls are found. Method does currently not
     * differentiate between overloaded methods since it compares names of methods at this time.
     *
     * @param allMethods      A list of MethodDeclarations of all methods in a class
     * @param methodToLookFor Method checks if it is called from a public method in the same class
     *
     * @return True if the method is called from another public method in the same class
     */
    public boolean isMethodCalledFromPublic(
        List<MethodDeclaration> allMethods, MethodDeclaration methodToLookFor) {
        List<MethodCallExpr> publicCalls = new ArrayList<>();
        List<MethodCallExpr> privateCalls = new ArrayList<>();
        boolean result = false;
        for (MethodDeclaration current : allMethods) {
            current.findAll(MethodCallExpr.class).forEach(methodCallExpr -> {
                if (methodCallExpr.getChildNodes().get(0).toString().equals(
                    methodToLookFor.getNameAsString())) {
                    if (current.isPrivate()) {
                        privateCalls.add(methodCallExpr);
                    } else {
                        publicCalls.add(methodCallExpr);
                    }
                }
            });
        }
        result = !publicCalls.isEmpty();
        if (!result) {
            for (MethodCallExpr currentExpr : privateCalls) {
                Node node = currentExpr;
                while (!(node instanceof CompilationUnit) && !result) {
                    if (node instanceof MethodDeclaration) {
                        result = isMethodCalledFromPublic(allMethods, (MethodDeclaration) node);
                        break;
                    } else {
                        node = node.getParentNode().get();
                    }
                }
            }
        }
        return result;
    }

    /**
     * Method for checking that an object of the Singleton class is only instantiated after checking
     * that the instance variable is null, should be extended to make sure that the returned
     * instance is assigned to the instance variable and not returned directly.
     *
     * @param compUnit The CompilationUnit representing the java class to look at
     *
     * @return true, if a check that the instance variable is null before the constructor is called
     *     is performed.
     */
    public boolean onlyInstantiatedIfNull(CompilationUnit compUnit) {
        AtomicBoolean onlyIfNull = new AtomicBoolean(true);
        compUnit.findAll(ObjectCreationExpr.class).forEach(objCreateExpr -> {
            if (objCreateExpr.getTypeAsString().equals(compUnit.getPrimaryTypeName().get())) {
                Node node = objCreateExpr.getParentNodeForChildren();
                while (true && !isInstantiated) {
                    if (node instanceof IfStmt) {
                        IfStmt ifStmtNode = (IfStmt) node;
                        if (ifStmtNode.getCondition().isBinaryExpr()) {
                            if (checkForNull(ifStmtNode) && checkForType(ifStmtNode, instanceVar)) {
                                if (ifStmtNode.hasElseBlock()) {
                                    ifStmtNode.getElseStmt().get().findAll(ObjectCreationExpr.class)
                                              .forEach(objCreateExpr1 -> {
                                                  if (objCreateExpr1.getTypeAsString().equals(
                                                      instanceVar.getVariable(0)
                                                                 .getTypeAsString())) {
                                                      onlyIfNull.set(false);
                                                  }
                                              });
                                }
                                onlyIfNull.compareAndSet(true, true);
                            } else {
                                onlyIfNull.set(false);
                            }
                        } else {
                            onlyIfNull.set(false);
                        }
                        break;
                    } else if (node instanceof CompilationUnit) {
                        onlyIfNull.set(false);
                        break;
                    }
                    node = node.getParentNode().get();
                }

            }
        });
        if (onlyIfNull.get()) {
            isInstantiated = true;
        }
        return onlyIfNull.get();
    }

    /**
     * Checks if an IfStmt contains a NullLiteralExpr.
     *
     * @param ifStmt The IfStmt to check.
     *
     * @return true iff the IfStmt contains a NullLiteralExpr.
     */
    private boolean checkForNull(IfStmt ifStmt) {
        boolean isNull = false;
        BinaryExpr ifAsBinary = ifStmt.getCondition().asBinaryExpr();
        isNull |= ifAsBinary.getLeft().isNullLiteralExpr();
        isNull |= ifAsBinary.getRight().isNullLiteralExpr();
        return isNull;
    }

    /**
     * Method for checking if an IfStmt has a partial expression of the same type as a variable in a
     * class.
     *
     * @param ifStmt          If statement to check.
     * @param compareVariable The variable to compare with.
     *
     * @return true iff the IfStmt contains an expression with the same type as a variable in a
     *     class.
     */
    private boolean checkForType(IfStmt ifStmt, FieldDeclaration compareVariable) {
        boolean isOfType = false;
        BinaryExpr ifAsBinary = ifStmt.getCondition().asBinaryExpr();
        String comparedTypeName = compareVariable.getVariables().get(0).getNameAsString();
        if (!ifAsBinary.getLeft().isNullLiteralExpr()) {
            isOfType |= ifAsBinary.getLeft().asNameExpr().getNameAsString().equals(
                comparedTypeName);
        }
        if (!ifAsBinary.getRight().isNullLiteralExpr()) {
            isOfType |= ifAsBinary.getRight().asNameExpr().getNameAsString().equals(
                comparedTypeName);
        }
        return isOfType;
    }
}
