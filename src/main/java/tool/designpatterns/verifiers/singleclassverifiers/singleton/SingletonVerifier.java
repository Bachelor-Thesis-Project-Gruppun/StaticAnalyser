package tool.designpatterns.verifiers.singleclassverifiers.singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.IfStmt;

import tool.designpatterns.verifiers.IPatternVerifier;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.util.VariableReader;

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
    public Feedback verify(ClassOrInterfaceDeclaration classToVerify) {
        // boolean isValid = callsConstructor(classToVerify) && hasAStaticInstance(classToVerify) &&
        //                   onlyInstantiatedIfNull(classToVerify) && hasPrivateConstructor(
        //     classToVerify) && isInstantiated;
        List<Feedback> childFeedbacks = new ArrayList<>();
        childFeedbacks.add(hasPrivateConstructor(classToVerify));
        childFeedbacks.add(findMultipleInstances(classToVerify));
        for (FieldDeclaration fieldDeclaration : VariableReader.readVariables(classToVerify)) {
            childFeedbacks.add(staticInstance(fieldDeclaration, classToVerify.getNameAsString()));
        }
        childFeedbacks.add(onlyInstantiatedIfNull(classToVerify));
        if (!isInstantiated) {
            childFeedbacks.add(Feedback.getNoChildFeedback(
                "There is no way to instantiate the " + "class", new FeedbackTrace(classToVerify)));
        }
        return Feedback.getFeedbackWithChildren(new FeedbackTrace(classToVerify), childFeedbacks);

    }

    /**
     * Method for checking if all the constructors in a Java class are private.
     *
     * @param classToTest The ClassOrInterfaceDeclaration representing the Java class to look at
     *
     * @return True iff all constructors are private
     */
    @SuppressWarnings("PMD.LinguisticNaming")
    public Feedback hasPrivateConstructor(ClassOrInterfaceDeclaration classToTest) {
        List<ConstructorDeclaration> publicConstructors = new ArrayList<>();
        // Finds and checks if the constructors are private or not.
        classToTest.findAll(ConstructorDeclaration.class).forEach(constructor -> {
            if (!constructor.isPrivate()) {
                publicConstructors.add(constructor);
            }

        });
        List<Feedback> childFeedbacks = new ArrayList<>();
        if (publicConstructors.isEmpty()) {
            return Feedback.getSuccessfulFeedback();
        } else {
            publicConstructors.forEach(constr -> {
                childFeedbacks.add(Feedback.getNoChildFeedback(
                    "Non-private constructor found, " + "class can be instantiated " + "elsewhere",
                    new FeedbackTrace(constr)));
            });
        }
        return Feedback.getFeedbackWithChildren(new FeedbackTrace(classToTest), childFeedbacks);
    }

    /**
     * Method for declaring if a FieldDeclaration is a private static field. https://stackoverflow
     * .com/questions/53300710/how-to-parse-inner-class-from-java-source-code might help solving a
     * check for inner classes
     *
     * @param field     The field to look at.
     * @param className the name of the class to check in.
     *
     * @return A Feedback holding the result
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Feedback staticInstance(FieldDeclaration field, String className) {
        boolean isStatic = false;
        List<Feedback> childFeedbacks = new ArrayList<>();
        if (field.getVariables().get(0).getType().toString().equals(className)) {
            if (!field.getVariables().get(0).getInitializer().isEmpty()) {
                isInstantiated = true;
            }
            for (Modifier modifier : field.getModifiers()) {
                String mdName = modifier.getKeyword().asString();
                String pub = "public";
                String prot = "protected";
                if (pub.equals(mdName) || prot.equals(mdName)) {
                    childFeedbacks.add(Feedback.getNoChildFeedback(
                        "Found a non-private instance of the class, could be set " +
                        "multiple times", new FeedbackTrace(field)));
                } else if (modifier.getKeyword().asString().equals("static")) {
                    isStatic = true;
                }
            }
            if (!isStatic) {
                childFeedbacks.add(Feedback.getNoChildFeedback(
                    "Field should be static",
                    new FeedbackTrace(field)));
            }
            instanceVar = field;
        }
        if (childFeedbacks.isEmpty()) {
            return Feedback.getSuccessfulFeedback();
        }
        return Feedback.getFeedbackWithChildren(new FeedbackTrace(field), childFeedbacks);
    }

    /**
     * Checks if a java class contains multiple fields of the Singleton type.
     *
     * @param classToTest The Java class to check.
     *
     * @return a Feedback containing the result.
     */
    public Feedback findMultipleInstances(ClassOrInterfaceDeclaration classToTest) {
        AtomicBoolean firstInstanceFound = new AtomicBoolean(false);
        List<Feedback> childFeedbacks = new ArrayList<>();
        classToTest.findAll(FieldDeclaration.class).forEach(fieldDeclaration -> {
            if (firstInstanceFound.get()) {
                childFeedbacks.add(Feedback.getNoChildFeedback("Found multiple instance fields",
                                                               new FeedbackTrace(
                                                                   fieldDeclaration)));
            }
            firstInstanceFound.set(true);
        });
        if (childFeedbacks.isEmpty()) {
            return Feedback.getSuccessfulFeedback();
        } else {
            return Feedback.getFeedbackWithChildren(new FeedbackTrace(classToTest), childFeedbacks);
        }
    }

    /**
     * Method for declaring if a java class has a getInstance() method which calls the constructor
     * of the Singleton class, does not support a Singleton that is initialized at variable
     * declaration in the Instance field.
     *
     * @param classToVerify The ClassOrInterfaceDeclaration representing the java class to look at
     *
     * @return
     */
    public boolean callsConstructor(ClassOrInterfaceDeclaration classToVerify) {
        boolean instanceMethod = true;
        List<MethodDeclaration> methods = new ArrayList<>();
        classToVerify.findAll(MethodDeclaration.class).forEach(methodDeclaration -> {
            methods.add(methodDeclaration);
        });
        for (MethodDeclaration declaration : methods) {
            if (declaration.getTypeAsString().equals(classToVerify.getNameAsString())) {
                if (declaration.isStatic()) {
                    if (declaration.isPrivate()) {
                        classToVerify.findAll(ConstructorDeclaration.class).forEach(
                            constructor -> constDeclList.add(constructor));
                        instanceMethod &= !isMethodCalledFromPublic(methods, declaration)
                            .getIsError();   //
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
    @SuppressWarnings("PMD.LinguisticNaming")
    public Feedback isMethodCalledFromPublic(
        List<MethodDeclaration> allMethods, MethodDeclaration methodToLookFor) {
        List<MethodCallExpr> publicCalls = new ArrayList<>();
        List<MethodCallExpr> privateCalls = new ArrayList<>();
        List<Feedback> childFeedbacks = new ArrayList<>();
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
                while (!(node instanceof ClassOrInterfaceDeclaration) && !result) {
                    if (node instanceof MethodDeclaration) {
                        result = !isMethodCalledFromPublic(allMethods, (MethodDeclaration) node)
                            .getIsError();
                        break;
                    } else {
                        node = node.getParentNode().get();
                    }
                }
            }
        }
        if (result) {
            return Feedback.getSuccessfulFeedback();
        } else {
            return Feedback.getFeedbackWithChildren(new FeedbackTrace(methodToLookFor),
                                                    childFeedbacks);
        }
    }

    /**
     * Method for checking that an object of the Singleton class is only instantiated after checking
     * that the instance variable is null, should be extended to make sure that the returned
     * instance is assigned to the instance variable and not returned directly.
     *
     * @param classToVerify The ClassOrInterfaceDeclaration representing the java class to look at
     *
     * @return true, if a check that the instance variable is null before the constructor is called
     *     is performed.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Feedback onlyInstantiatedIfNull(ClassOrInterfaceDeclaration classToVerify) {
        List<Feedback> childFeedbacks = new ArrayList<>();
        String feedbackString =
            "Object can be instatiated even if there is another instace created";
        AtomicBoolean onlyIfNull = new AtomicBoolean(true);
        classToVerify.findAll(ObjectCreationExpr.class).forEach(objInstExpr -> {
            if (objInstExpr.getTypeAsString().equals(classToVerify.getNameAsString())) {
                Node node = objInstExpr.getParentNodeForChildren();
                while (true && !isInstantiated) {
                    if (node instanceof IfStmt) {
                        IfStmt ifStmtNode = (IfStmt) node;
                        if (ifStmtNode.getCondition().isBinaryExpr()) {
                            if (checkForNull(ifStmtNode) && checkForType(ifStmtNode, instanceVar)) {
                                if (ifStmtNode.hasElseBlock()) {
                                    ifStmtNode.getElseStmt().get().findAll(ObjectCreationExpr.class)
                                              .forEach(objCreateExpr -> {
                                                  if (objCreateExpr.getTypeAsString().equals(
                                                      instanceVar.getVariable(0)
                                                                 .getTypeAsString())) {
                                                      childFeedbacks.add(Feedback
                                                                             .getNoChildFeedback(
                                                                                 feedbackString,
                                                                                 new FeedbackTrace(
                                                                                     objInstExpr)));
                                                  }
                                              });
                                }
                                onlyIfNull.compareAndSet(true, true);
                            } else {
                                childFeedbacks.add(Feedback.getNoChildFeedback(
                                    "Object can be instantiated even if there is " +
                                    "another instance created", new FeedbackTrace(objInstExpr)));
                            }
                        } else {
                            childFeedbacks.add(Feedback.getNoChildFeedback(
                                "Object can be instantiated even if there is " +
                                "another instance created", new FeedbackTrace(objInstExpr)));
                        }
                        break;
                    } else if (node instanceof ClassOrInterfaceDeclaration) {
                        childFeedbacks.add(Feedback.getNoChildFeedback(
                            "Object can be instantiated even if there is " +
                            "another instance created", new FeedbackTrace(objInstExpr)));
                        break;
                    }
                    node = node.getParentNode().get();
                }

            }
        });
        if (onlyIfNull.get()) {
            isInstantiated = true;
        }
        if (childFeedbacks.isEmpty()) {
            return Feedback.getSuccessfulFeedback();
        }
        return Feedback.getFeedbackWithChildren(new FeedbackTrace(classToVerify), childFeedbacks);
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
