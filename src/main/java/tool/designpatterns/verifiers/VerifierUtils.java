package tool.designpatterns.verifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;

import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;

/**
 * A static class containing methods used to verify design patterns.
 */
public final class VerifierUtils {

    /**
     * Private constructor preventing instantiation of utility class.
     */
    private VerifierUtils() {
    }

    /**
     * Checks if all constructors in a given class initialize a certain type.
     *
     * @param toTest The class to verify
     * @param type   The type to look for in the constructors
     *
     * @return A {@link Feedback} object containing the result
     */
    @SuppressWarnings("PMD.LinguisticNaming")
    public static Feedback initializesTypeInAllConstructors(
        ClassOrInterfaceDeclaration toTest, ClassOrInterfaceDeclaration type) {
        Feedback result;
        AtomicBoolean isInitialized = new AtomicBoolean(true);
        List<FieldDeclaration> fieldsInClass = toTest.findAll(FieldDeclaration.class);
        String nameOfType = type.getNameAsString();
        List<String> constructorParams = new ArrayList<>();
        for (FieldDeclaration currentField : fieldsInClass) {
            if (currentField.getCommonType().toString().equals(nameOfType)) {
                isInitialized.compareAndSet(true, !currentField.isPublic());
                if (isInitialized.get()) {
                    currentField.findAll(InitializerDeclaration.class).forEach(fieldInitializer -> {
                        isInitialized.set(false);
                    });
                    toTest.findAll(ConstructorDeclaration.class).forEach(declaration -> {
                        Optional<Parameter> parameter = declaration.getParameterByType(nameOfType);
                        if (parameter.isPresent()) {
                            constructorParams.add(parameter.get().getNameAsString());
                        } else {
                            isInitialized.set(false);
                        }
                    });
                    toTest.findAll(VariableDeclarator.class).forEach(variableDeclarator -> {
                        if (variableDeclarator.getNameAsString().equals(
                            currentField.getVariable(0).getNameAsString()) &&
                            variableDeclarator.getInitializer().isPresent() &&
                            !constructorParams.contains(
                                variableDeclarator.getInitializer().get().toString())) {
                            isInitialized.set(false);

                        }
                    });
                }
            }
            constructorParams.clear();
        }
        if (isInitialized.get()) {
            result = Feedback.getSuccessfulFeedback();
        } else {
            result = Feedback.getNoChildFeedback(
                "All constructors did not initialize the Component field",
                new FeedbackTrace(toTest));
        }
        return result;
    }

    /**
     * Checks that a class or interface contains at least one public method.
     *
     * @param toTest The class or interface to verify
     *
     * @return A {@link Feedback} object containing the result
     */
    @SuppressWarnings("PMD.LinguisticNaming")
    public static Feedback hasAtLeastOnePublicMethod(
        ClassOrInterfaceDeclaration toTest) {
        Feedback result;

        AtomicBoolean resultBool = new AtomicBoolean(false);
        for (MethodDeclaration methodDeclaration : toTest.findAll(
            MethodDeclaration.class)) {
            if (!methodDeclaration.isPrivate()) {
                resultBool.set(true);
            }
        }

        if (resultBool.get()) {
            result = Feedback.getSuccessfulFeedback();
        } else {
            String msg = "Interface/Class does not contain any methods!";
            result = Feedback.getPatternInstanceNoChildFeedback(msg);
        }

        return result;
    }

    /**
     * Method for checking that a class contains a field of a certain type.
     *
     * @param toTest The class to check
     * @param type   The class or interface that toTest should contain
     *
     * @return A {@link Feedback} object containing the result
     */
    @SuppressWarnings("PMD.LinguisticNaming")
    public static Feedback hasFieldOfType(
        ClassOrInterfaceDeclaration toTest, ClassOrInterfaceDeclaration type) {
        Feedback result;
        AtomicBoolean hasAComponent = new AtomicBoolean(false);
        toTest.findAll(VariableDeclarator.class).forEach(fieldDeclaration -> {
            if (fieldDeclaration.getTypeAsString().equals(
                type.getNameAsString())) { //Check that this works
                hasAComponent.set(true);
            }
        });
        if (hasAComponent.get()) {
            result = Feedback.getSuccessfulFeedback();
        } else {
            result = Feedback.getNoChildFeedback(
                "There was no field with type '" + type.getNameAsString() + "'",
                new FeedbackTrace(toTest));
        }
        return result;
    }

    /**
     * Since all containers and leaves implement the component we now that they must implement all
     * methods of the compent. So if a method has the {@link Override} Annotation we know that it is
     * implemented. And if it then has the same name and argumnents, it has the same method head and
     * is therefore the same method.
     *
     * @param method methods to check if it can come from the component
     * @param component the class or interface with implementable methods
     *
     * @return a boolean specifying if the method can come from the component
     */
    public static boolean methodBelongsToComponent(
        MethodDeclaration method, ClassOrInterfaceDeclaration component) {
        for (MethodDeclaration methodInContainer : component.getMethods()) {
            Optional<AnnotationExpr> overrideAnn = method.getAnnotationByClass(Override.class);
            if (isSameMethod(methodInContainer, method) && overrideAnn.isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * As getQualifiedName returns the package path to the method, it cant be used to determine
     * if two merhods are the same. Instead, we check that they have the same name and
     * idendentical argments.
     *
     * @param method1 a method
     * @param method2 a method
     *
     * @return a boolean
     */
    public static boolean isSameMethod(MethodDeclaration method1, MethodDeclaration method2) {
        boolean hasSameName = method1.getName().equals(method2.getName());
        boolean hasSameParameters = method1.getParameters().equals(method2.getParameters());
        return hasSameName && hasSameParameters;
    }


}
