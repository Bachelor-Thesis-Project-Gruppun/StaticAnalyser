package tool.designpatterns.verifiers.multiclassverifiers.decorator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;

import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.PatternGroupFeedback;

/**
 * A verifier for the decorator pattern.
 */
@SuppressWarnings("PMD.CommentSize")
public class DecoratorVerifier implements IPatternGrouper {

    /**
     * Constructor.
     */
    public DecoratorVerifier() {
    }

    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {
        List<DecoratorPatternInstance> patternInstances =
            DecoratorPatternInstance.getPatternInstances(map);
        List<Feedback> results = new ArrayList<>();
        patternInstances.forEach(patternInstance -> {
            results.add(verify(patternInstance));
        });

        return new PatternGroupFeedback(PatternGroup.DECORATOR, results);
    }

    /**
     * <p>Verifies that a correct implementation of the decorator interface has
     * been found.</p>
     * <p>Any valid implementation must fulfill the following requirements:
     *      <ol>
     *          <li>Any given pattern instance must contain:
     *              <ul>
     *             <li>Exactly one component interface</li>
     *             <li>At least one concrete component</li>
     *             <li>At least one abstract decorator</li>
     *             <li>At least one concrete decorator</li>
     *              </ul>
     *          </li>
     *          <li>The component interface must contain at least one
     *          method</li>
     *
     *         <li>Every abstract decorator must fulfill the following:
     *             <ul>
     *             <li>It must house a component field</li>
     *             <li>The component field needs to be initialized during
     *             construction</li>
     *             </ul>
     *         </li>
     *         <li>Every concrete decorator must extend an existing abstract
     *         decorator in the
     *         current the pattern instance</li>
     *      </ol>
     * </p>
     *
     * @param patternInstance An instance of the decorator pattern to be verified
     *
     * @return A {@link Feedback} object that contains the result and information regarding whether
     *     ther or not the instance of the pattern was valid
     */
    public Feedback verify(DecoratorPatternInstance patternInstance) {
        Feedback allElementsChild = patternInstance.hasAllElements(patternInstance);
        List<Feedback> childFeedbacks = new ArrayList<>();
        childFeedbacks.add(allElementsChild);
        if (!allElementsChild.getIsError()) {
            childFeedbacks.add(interfaceContainsMethod(patternInstance.interfaceComponent));
            patternInstance.abstractDecorators.forEach(decorator -> {
                childFeedbacks.add(hasFieldOfType(decorator, patternInstance.interfaceComponent));
                childFeedbacks.add(
                    fieldInitializedInConstr(decorator, patternInstance.interfaceComponent));
            });
            patternInstance.concreteDecorators.forEach(decorator -> {
                childFeedbacks.add(
                    fieldInitializedInConstr(decorator, patternInstance.interfaceComponent));
            });
        }

        return Feedback.getPatternInstanceFeedback(childFeedbacks);
    }

    /**
     * Checks that an interfaceComponent contains at least one public method.
     *
     * @param interfaceComponent The interface of the interfaceComponent
     *
     * @return A {@link Feedback} object containing true iff it contains at least one public method
     */
    public Feedback interfaceContainsMethod(
        ClassOrInterfaceDeclaration interfaceComponent) {
        Feedback result;
        MethodDeclaration erroringMethod = null;

        AtomicBoolean resultBool = new AtomicBoolean(false);
        for (MethodDeclaration methodDeclaration : interfaceComponent.findAll(
            MethodDeclaration.class)) {
            if (!methodDeclaration.isPrivate()) {
                resultBool.set(true);
                erroringMethod = methodDeclaration;
            }
        }

        if (resultBool.get()) {
            result = Feedback.getSuccessfulFeedback();
        } else {
            String msg = "Interface does not contain any methods!";
            if (erroringMethod == null) {
                result = Feedback.getPatternInstanceNoChildFeedback(msg);
            } else {
                result = Feedback.getNoChildFeedback(msg, new FeedbackTrace(erroringMethod));
            }
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
    private Feedback hasFieldOfType(
        ClassOrInterfaceDeclaration toTest, ClassOrInterfaceDeclaration type) {
        Feedback result;
        AtomicBoolean hasAComponent = new AtomicBoolean(false);
        toTest.findAll(VariableDeclarator.class).forEach(fieldDeclaration -> {
            if (fieldDeclaration.getTypeAsString().contains(
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
     * Checks if all constructors in a given class initialize the Component field in the class.
     *
     * @param toTest        The class to check.
     * @param interfaceName
     *
     * @return A {@link Feedback} object containing the result
     */
    private Feedback fieldInitializedInConstr(
        ClassOrInterfaceDeclaration toTest, ClassOrInterfaceDeclaration interfaceName) {
        Feedback result;
        AtomicBoolean isInitialized = new AtomicBoolean(true);
        List<FieldDeclaration> fieldsInClass = new ArrayList<>();
        toTest.findAll(FieldDeclaration.class).forEach(fieldDeclaration -> {
            fieldsInClass.add(fieldDeclaration);
        });
        String nameOfInterface = interfaceName.getNameAsString();
        List<String> constructorParams = new ArrayList<>();
        for (FieldDeclaration currentField : fieldsInClass) {
            if (currentField.getCommonType().toString().equals(nameOfInterface)) {
                isInitialized.compareAndSet(true, !currentField.isPublic());
                if (isInitialized.get()) {
                    currentField.findAll(InitializerDeclaration.class).forEach(fieldInitializer -> {
                        isInitialized.set(false);
                    });
                    toTest.findAll(ConstructorDeclaration.class).forEach(declaration -> {
                        Optional<Parameter> parameter = declaration.getParameterByType(
                            nameOfInterface);
                        if (parameter.isPresent()) {
                            constructorParams.add(parameter.get().getNameAsString());
                        } else {
                            isInitialized.set(false);
                        }
                    });
                    toTest.findAll(VariableDeclarator.class).forEach(variableDeclarator -> {
                        if (variableDeclarator.getNameAsString().equals(
                            currentField.getVariable(0).getNameAsString()) &&
                            !variableDeclarator.getInitializer().isEmpty() &&
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
                "All constructors did not initialize the " + "Component field",
                new FeedbackTrace(toTest));
        }
        return result;
    }

}

