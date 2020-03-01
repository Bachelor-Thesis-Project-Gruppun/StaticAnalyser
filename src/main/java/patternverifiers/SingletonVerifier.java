package patternverifiers;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

import base.VariableReader;

public class SingletonVerifier implements IPatternVerifier {

    private ConstructorDeclaration constructorDeclaration;

    public SingletonVerifier() {
    }

    @Override
    public boolean verify(CompilationUnit cu) {
        return callsConstructor(cu) &&
               hasStaticInstance(cu) &&
               hasPrivateConstructor(cu);
    }

    /**
     * Method for checking if all the constructors in a Java class are
     * private.
     *
     * @param cu The CompilationUnit representing the Java class to look at
     * @return True iff all constructors are private
     */
    public boolean hasPrivateConstructor(CompilationUnit cu){
        List<Boolean> isPrivate = new ArrayList<>();
        boolean isPrivateConstructor = true;

        // Finds and checks if the constructors are private or not.
        cu.findAll(ConstructorDeclaration.class).forEach(constructor -> {
            if(constructor.isPrivate()){
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
     * @param cu The CompilationUnit representing the java class to look at
     *
     * @return True iff the java class holds a field variable with a static modifier of the same
     *     type as the class itself (eg. static SingletonVerifier sv;)
     */
    public boolean hasStaticInstance(CompilationUnit cu) {
        boolean stat = false;
        boolean priv = false;
        for (FieldDeclaration bd : VariableReader.readVariables(
            cu)) {      // For each FieldDeclaration in the java file
            if (bd.getVariables().get(0).getType().toString().equals(
                cu.getType(0).getNameAsString())) {    // If there is a field of the
                // same type as the file itself, probably needs to check for
                // several different classes in the same file, can have inner
                // classes etc not sure how javaparser handles that.
                for (Modifier md : bd.getModifiers()) {     // For each modifier on that field
                    if (md.getKeyword().asString().equals(
                        "static")) {  // If that modifier is static
                        stat = true;
                    } else if (md.getKeyword().asString().equals(
                        "private")) { // Else if that modifier is private
                        priv = true;
                    }
                }
            }
        }
        return stat && priv;
    }

    /**
     * Method for declaring if a java class has a getInstance() method which calls the constructor
     * of the Singleton class, does not support a Singleton that is initialized at variable
     * declaration in the Instance field.
     *
     * @param cu The CompilationUnit representing the java class to look at
     *
     * @return
     */
    public boolean callsConstructor(CompilationUnit cu) {
        boolean instanceMethod = false;
        List<MethodDeclaration> methods = new ArrayList<>();
        cu.findAll(MethodDeclaration.class).forEach(methodDeclaration -> {  //Make a list of all
            // methods in the class.
            methods.add(methodDeclaration);
        });
        for (MethodDeclaration declaration : methods) { // For each method
            if (declaration.isStatic()) {   // If the method is static
                if (declaration.getTypeAsString().equals(cu.getPrimaryTypeName().get())) {  // If
                    // the method returns an instance of the Singleton
                    if (declaration.isPrivate()) {  // If the method is private
                        cu.findAll(ConstructorDeclaration.class).forEach(
                            constructor -> constructorDeclaration = constructor);
                        return isMethodCalledFromPublic(methods, declaration);
                        // return findConstructorCall(declaration, constructorDeclaration);
                    } else {
                        instanceMethod = true;
                    }
                }
            }
        }
        return instanceMethod;
    }

    /**
     * Method to look for a (specific, does not look for different constructors if multiple exist in
     * a class) constructor call, might be useful to differentiate between different types of
     * singletons in the future, currently unused
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
                Node n = currentExpr;   // Assign the current private call to n
                while (!(n instanceof CompilationUnit)) {   // While n is not a CompilationUnit
                    // (Since CompilationUnits are high up the the hierarchy, n being a
                    // CompilationUnit would most likely mean it has gone past all
                    // MethodDeclarations and can therefor go to the next MethodCallExpr
                    if (n instanceof MethodDeclaration) {   // If n is a MethodDeclaration
                        result = isMethodCalledFromPublic(allMethods, (MethodDeclaration) n);
                        // Call the same method recursively on that method to see if that method
                        // is called from a public method
                    } else {    // If n is not a MethodDeclaration
                        n = n.getParentNode().get();    // Find the parent node of n and check if
                        // that is a MethodDeclaration
                    }
                }
            }
        }
        return result;    // Return result
    }

}


