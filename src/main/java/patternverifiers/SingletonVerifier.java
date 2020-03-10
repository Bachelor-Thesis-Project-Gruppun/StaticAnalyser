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

    private List<ConstructorDeclaration> constructorDeclarations = new ArrayList<>();
    private boolean isInstantiated;
    private FieldDeclaration instanceVar;

    public SingletonVerifier() {
    }

    @Override
    public boolean verify(CompilationUnit compUnit) {
        return callsConstructor(compUnit) && hasAStaticInstance(compUnit) && onlyInstantiatedIfNull(
            compUnit) && hasPrivateConstructor(compUnit);
    }

    /**
     * Method for checking if all the constructors in a Java class are private.
     *
     * @param compUnit The CompilationUnit representing the Java class to look at
     *
     * @return True iff all constructors are private
     */
    public boolean hasPrivateConstructor(CompilationUnit compUnit) {
        List<Boolean> isPrivate = new ArrayList<>();
        boolean isPrivateConstructor = true;

        // Finds and checks if the constructors are private or not.
        compUnit.findAll(ConstructorDeclaration.class).forEach(constructor -> {
            if (constructor.isPrivate()) {
                isPrivate.add(true);
            } else {
                isPrivate.add(false);
            }
        });

        // Takes all boolean values and ANDs them to one value.
        for (Boolean bool : isPrivate) {
            isPrivateConstructor &= bool;
        }

        return isPrivateConstructor;
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

        for (FieldDeclaration fieldDeclaration : VariableReader.readVariables(
            compUnit)) {      // For each FieldDeclaration in the java file
            if (fieldDeclaration.getVariables().get(0).getType().toString().equals(
                compUnit.getType(0).getNameAsString())) {    // If there is a field of the
                // same type as the file itself, probably needs to check for
                // several different classes in the same file, can have inner
                // classes etc not sure how javaparser handles that.
                if (isStatic && isPrivate) {  // If an instance variable has already been found,
                    // return false since a singleton should only have one instance.
                    isStatic = false;
                    isPrivate = false;
                    return false;
                }
                if (!fieldDeclaration.getVariables().get(0).getInitializer().isEmpty()) {
                    isInstantiated = true;
                }
                for (Modifier md : fieldDeclaration
                    .getModifiers()) {     // For each modifier on that field
                    if (md.getKeyword().asString().equals(
                        "static")) {  // If that modifier is static
                        isStatic = true;
                    } else if (md.getKeyword().asString().equals(
                        "private")) { // Else if that modifier is private
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
        compUnit.findAll(MethodDeclaration.class).forEach(
            methodDeclaration -> {  //Make a list of all
                // methods in the class.
                methods.add(methodDeclaration);
            });
        for (MethodDeclaration declaration : methods) { // For each method
            if (declaration.getTypeAsString().equals(
                compUnit.getPrimaryTypeName().get())) {   // If the method
                // returns an instance of the Singleton
                if (declaration.isStatic()) {  // If the method is static
                    if (declaration.isPrivate()) {  // If the method is private
                        compUnit.findAll(ConstructorDeclaration.class).forEach(
                            constructor -> constructorDeclarations.add(constructor));
                        instanceMethod &= isMethodCalledFromPublic(methods, declaration);   //
                        // Check if the method is called from a public method
                        // (isMethodCalledFromPublic calls itself recursively to find nested
                        // method calls)
                        // return findConstructorCall(declaration, constructorDeclaration);
                    } else {
                        instanceMethod &= true; // AND with true
                    }
                } else {
                    instanceMethod &= false;    // AND with false
                }
            }
        }
        return instanceMethod;  // Return result
    }

    /**
     * Method to look for a constructor call, might be useful to differentiate between different
     * types of singletons in the future, currently unused
     *
     * @param declaration A method to look inside to see if there is a ConstructorCall
     * @param constructor A ConstructorDeclaration for the class
     *
     * @return If a method is calling a specific constructor
     */
    public boolean findConstructorCall(
        MethodDeclaration declaration, ConstructorDeclaration constructor) {
        List<ObjectCreationExpr> calledMethods = new ArrayList<>();
        declaration.findAll(ObjectCreationExpr.class).forEach(methodDeclaration -> {    // Find all
            // ObjectCreations in the method
            if (methodDeclaration.getTypeAsString().equals(
                constructor.getNameAsString())) {    // If the object created is an instance
                // of the singleton
                calledMethods.add(methodDeclaration);   // Add the ObjectCreationExpr to
                // the list of ObjectCreationExpr
            }
        });
        return !calledMethods.isEmpty(); // If the list of ObjectCreationExpr is empty -> return
        // true
    }

    /**
     * Method that returns whether a method is called from another public method in the same class
     * or not. Calls itself recursively until no more calls are found (not 100% sure it doesn't end
     * up in an infinite loop if it never finds a public call, might need some help with that)
     * Method does currently not differentiate between overloaded methods since it compares names of
     * methods at this time.
     *
     * @param allMethods      A list of MethodDeclarations of all methods in a class
     * @param methodToLookFor The method to look for if it is called from a public method in the
     *                        same class
     *
     * @return True if the method is called from another public method in the same class
     */
    public boolean isMethodCalledFromPublic(
        List<MethodDeclaration> allMethods, MethodDeclaration methodToLookFor) {
        List<MethodCallExpr> publicCalls = new ArrayList<>();
        List<MethodCallExpr> privateCalls = new ArrayList<>();
        boolean result = false;
        for (MethodDeclaration current : allMethods) {  // For each method in a class
            current.findAll(MethodCallExpr.class).forEach(methodCallExpr -> {   // Find all
                // MethodCalls
                if (methodCallExpr.getChildNodes().get(0).toString().equals(
                    methodToLookFor.getNameAsString())) {   // If the Method called has the same
                    // name as the one we are looking for (so not differentiating between
                    // overloaded methods) AND the method calling the method is not private
                    if (!current.isPrivate()) {
                        publicCalls.add(methodCallExpr);  // Add the call to a list of viable calls
                    } else {
                        privateCalls.add(methodCallExpr);   // Add the call to a list of calls
                        // from other private methods
                    }
                }
            });
        }
        result = !publicCalls.isEmpty();    // If a public call was found, set result to true
        if (!result) {   // If result is false
            for (MethodCallExpr currentExpr : privateCalls) {   // For each private call found
                Node node = currentExpr;   // Assign the current private call to node
                while (!(node instanceof CompilationUnit) && !result) {   // While node is not a
                    // CompilationUnit
                    // (Since CompilationUnits are high up the the hierarchy, node being a
                    // CompilationUnit would most likely mean it has gone past all
                    // MethodDeclarations and can therefor go to the next MethodCallExpr
                    if (node instanceof MethodDeclaration) {   // If node is a MethodDeclaration
                        result = isMethodCalledFromPublic(allMethods, (MethodDeclaration) node);
                        // Call the same method recursively on that method to see if that method
                        // is called from a public method
                    } else {    // If node is not a MethodDeclaration
                        node = node.getParentNode()
                                   .get();    // Find the parent node of node and check if
                        // that is a MethodDeclaration
                    }
                }
            }
        }
        return result;    // Return result
    }

    /**
     * Method for checking that an object of the Singleton class is only instantiated after checking
     * that the instance variable is null, should be extended to make sure that the returned
     * instance is assigned to the instance variable and not returned directly
     *
     * @param compUnit The CompilationUnit representing the java class to look at
     *
     * @return true, if a check that the instance variable is null before the constructor is called
     *     is performed.
     */
    public boolean onlyInstantiatedIfNull(CompilationUnit compUnit) {
        AtomicBoolean onlyIfNull = new AtomicBoolean(true);
        compUnit.findAll(ObjectCreationExpr.class).forEach(objectCreationExpr -> {    // Find all
            // Object creations in the class
            if (objectCreationExpr.getTypeAsString().equals(compUnit.getPrimaryTypeName().get())) {
                // If the created object is of the Singletons type
                Node node = objectCreationExpr.getParentNodeForChildren();
                while (true) {  // Iterate over parent nodes until you hit either the compilation
                    // unit OR an if statement
                    if (node instanceof IfStmt) {
                        IfStmt nodeAsIfStmt = (IfStmt) node;
                        if (nodeAsIfStmt.getCondition().isBinaryExpr()) {   // If null is one of
                            // the parts of the binary expression (in the If statement) and the
                            // name of the instance variable is the other part of the binary
                            // expression
                            if ((
                                    ((BinaryExpr) (nodeAsIfStmt.getCondition())).getLeft()
                                                                                .isNullLiteralExpr() ||
                                    ((BinaryExpr) (nodeAsIfStmt.getCondition())).getRight()
                                                                                .isNullLiteralExpr()) &&
                                (
                                    ((BinaryExpr) (nodeAsIfStmt.getCondition())).getLeft()
                                                                                .asNameExpr()
                                                                                .getName()
                                                                                .toString().equals(
                                        instanceVar.getVariable(0).getNameAsString()) ||
                                    ((BinaryExpr) (nodeAsIfStmt.getCondition())).getRight()
                                                                                .asNameExpr()
                                                                                .getName()
                                                                                .toString().equals(
                                        instanceVar.getVariable(0).getNameAsString()))) {
                                if (nodeAsIfStmt.hasElseBlock()) {      // DOES NOT WORK CURRENTLY
                                    nodeAsIfStmt.getElseStmt().get().findAll(
                                        ObjectCreationExpr.class).forEach(objectCreationExpr1 -> {
                                        if (objectCreationExpr1.getTypeAsString().equals(
                                            instanceVar.getVariable(0).getTypeAsString())) {
                                            onlyIfNull.set(onlyIfNull.get() && false);
                                        }
                                    });
                                }
                                onlyIfNull.set(onlyIfNull.get() && true);   // Predicate verified
                                System.out.println("Object is only instantiated after a check if " +
                                                   "the instance variable is null: " +
                                                   onlyIfNull.get());
                            } else {
                                onlyIfNull.set(onlyIfNull.get() && false);
                            }
                        } else {
                            onlyIfNull.set(onlyIfNull.get() && false);
                        }
                        break;  // break the while loop
                    } else if (node instanceof CompilationUnit) {
                        onlyIfNull.set(onlyIfNull.get() && false);
                        break;  // break the while loop if we reach a CompilationUnit
                    }
                    node = node.getParentNode().get();    // Go to the Parent node,
                }

            }
        });
        return onlyIfNull.get();    // Return result
    }
}


