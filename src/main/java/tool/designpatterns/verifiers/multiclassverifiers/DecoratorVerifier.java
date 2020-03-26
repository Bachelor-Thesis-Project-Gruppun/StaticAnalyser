package tool.designpatterns.verifiers.multiclassverifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
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
public class DecoratorVerifier implements IPatternGrouper {

    /**
     * Constructor.
     */
    public DecoratorVerifier() {
    }

    @Override
    public PatternGroupFeedback verifyGroup(Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {
        List<PatternInstance> patternInstances = getPatternInstances(map);
        List<Feedback> results = new ArrayList<>();
        patternInstances.forEach(pi -> {
            results.add(verify(pi));
        });

        return new PatternGroupFeedback(PatternGroup.DECORATOR, results);
    }

    /**
     * <p>Verifies that a correct implementation of the decorator interface has been found.</p>
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
     *          <li>The component interface must contain at least one method</li>
     *
     *         <li>Every abstract decorator must fulfill the following:
     *              <ul>
     *             <li>It must house a component field</li>
     *             <li>The component field needs to be initialized during construction</li>
     *             </ul>
     *         </li>
     *         <li>Every concrete decorator must extend an existing abstract decorator in the
     *         current the pattern instance</li>
     *      </ol>
     * </p>
     *
     * @param pi An instance of the decorator pattern to be verified
     *
     * @return A {@link Feedback} object that contains the result and information regarding whether
     *     or not the instance of the pattern was valid
     */
    public Feedback verify(PatternInstance pi) {
        Feedback allElementsChild = hasAllElements(pi);
        List<Feedback> childFeedbacks = new ArrayList<>();
        childFeedbacks.add(allElementsChild);
        if (!allElementsChild.getIsError()) {
            // Do shit here.
            childFeedbacks.add(interfaceContainsMethod(pi.interfaceComponent));
            pi.abstractDecorators.forEach(decorator -> {
                childFeedbacks.add(hasFieldOfType(decorator, pi.interfaceComponent));
                childFeedbacks.add(
                    componentInitializedInConstructor(decorator, pi.interfaceComponent));
            });
        }

        return Feedback.getPatternInstanceFeedback(childFeedbacks);
    }

    /**
     * Used to group parts of the same pattern instance (an implementation of the pattern) in one
     * place
     */
    private class PatternInstance {

        ClassOrInterfaceDeclaration interfaceComponent;
        List<ClassOrInterfaceDeclaration> concreteComponents = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> abstractDecorators = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> concreteDecorators = new ArrayList<>();

        public PatternInstance() {
        }
    }

    /**
     * Checks that an interfaceComponent contains at least one public method
     *
     * @param interfaceComponent The interface of the interfaceComponent
     *
     * @return A {@link Feedback} object containing true iff it contains at least one public method
     */
    public Feedback interfaceContainsMethod(ClassOrInterfaceDeclaration interfaceComponent) {
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
            if (erroringMethod != null) {
                result = Feedback.getNoChildFeedback(msg, new FeedbackTrace(erroringMethod));
            } else {
                result = Feedback.getPatternInstanceNoChildFeedback(msg);
            }
        }

        return result;
    }

    /**
     * Method for identifying which classes are part of the same decorator pattern instance
     *
     * @param map A map where every element of the decorator pattern (e.g. concrete decorator) is
     *            mapped to all the classes of said element type
     *
     * @return A list of all identified instances of the pattern
     */
    public List<PatternInstance> getPatternInstances(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {
        List<ClassOrInterfaceDeclaration> interfaceComponents = map.get(
            Pattern.DECORATOR_INTERFACE_COMPONENT);
        List<ClassOrInterfaceDeclaration> concreteComponents = map.get(
            Pattern.DECORATOR_CONCRETE_COMPONENT);
        List<ClassOrInterfaceDeclaration> abstractDecorators = map.get(
            Pattern.DECORATOR_ABSTRACT_DECORATOR);
        List<ClassOrInterfaceDeclaration> concreteDecorators = map.get(
            Pattern.DECORATOR_CONCRETE_DECORATOR);
        HashMap<ClassOrInterfaceDeclaration, PatternInstance> componentToPatternInstance =
            new HashMap();

        //Create incomplete PIs to be populated below, easier to use isDescendantOf below by
        // doing this
        interfaceComponents.forEach((interfaceComponent) -> {
            PatternInstance pi = new PatternInstance();
            pi.interfaceComponent = interfaceComponent;
            componentToPatternInstance.putIfAbsent(interfaceComponent, pi);
        });

        //Used at the bottom for detecting leftovers/invalid patterns
        ArrayList<ClassOrInterfaceDeclaration> identifiedElements = new ArrayList<>();

        //Found no better way to populate PIs than looping through everything multiple times
        interfaceComponents.forEach((interfaceComponent) -> {
            if (interfaceComponent.isInterface()) {
                String interfaceName = interfaceComponent.getNameAsString();

                concreteComponents.forEach(cc -> {
                    cc.getImplementedTypes().forEach(implementedInterface -> {
                        if (implementedInterface.getNameAsString().equals(interfaceName)) {
                            PatternInstance pi = componentToPatternInstance.get(interfaceComponent);
                            pi.concreteComponents.add(cc);
                            identifiedElements.add(cc);
                        }
                    });
                });

                abstractDecorators.forEach(ad -> {
                    ad.getImplementedTypes().forEach(implementedInterface -> {
                        if (implementedInterface.getNameAsString().equals(interfaceName)) {
                            PatternInstance pi = componentToPatternInstance.get(interfaceComponent);
                            pi.abstractDecorators.add(ad);
                            identifiedElements.add(ad);

                            //Check which concreteComponents extend this abstractDecorator
                            //And update PI accordingly
                            concreteDecorators.forEach(cd -> {
                                cd.getExtendedTypes().forEach(extendedClass -> {
                                    if (extendedClass.getNameAsString().equals(
                                        ad.getName().asString())) {
                                        pi.concreteDecorators.add(cd);
                                        identifiedElements.add(cd);
                                    }
                                });
                            });
                        }
                    });
                });
                //If there are any elements left (that we have not discovered) in any of the
                // following, then they are
                //part of an invalid instance of the pattern and will be handled below
                concreteComponents.removeAll(identifiedElements);
                abstractDecorators.removeAll(identifiedElements);
                concreteDecorators.removeAll(identifiedElements);
            } else {
                throw new UnsupportedOperationException(
                    "Was not an interface or something wrong happened");
            }
        });

        //If there are elements that do not relate to any of the previous interface components,
        //since they are invalid, put them in an invalid pattern instance object for verify() to
        // handle
        if (!(
            concreteComponents.isEmpty() && abstractDecorators.isEmpty() &&
            concreteDecorators.isEmpty())) {
            var pi = new PatternInstance();
            pi.interfaceComponent = null;
            pi.concreteComponents = concreteComponents;
            pi.abstractDecorators = abstractDecorators;
            pi.concreteDecorators = concreteDecorators;
            componentToPatternInstance.put(null, pi);
        }

        return new ArrayList<PatternInstance>(componentToPatternInstance.values());
    }

    /**
     * Method for checking that a class contains a field of a certain type
     *
     * @param toTest The class to check
     * @param type   The class or interface that toTest should contain
     *
     * @return A feedback object containing the result
     */
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
     * TODO MUST BE UPDATED WITH ClassOrInterfaceDeclaration Method to check if all constructors in
     * a given class initialize the Component field in the class.
     *
     * @param toTest        The class to check.
     * @param interfaceName
     *
     * @return True iff all constructors in a given class does initialize the class' Component
     */
    private Feedback componentInitializedInConstructor(
        ClassOrInterfaceDeclaration toTest, ClassOrInterfaceDeclaration interfaceName) {
        Feedback result;
        AtomicBoolean isInitialized = new AtomicBoolean(true);
        List<FieldDeclaration> fieldsInClass = new ArrayList<>();
        toTest.findAll(FieldDeclaration.class).forEach(fieldDeclaration -> {
            fieldsInClass.add(fieldDeclaration);
        });
        String nameOfInterface = interfaceName.getFullyQualifiedName().get();
        for (FieldDeclaration currentField : fieldsInClass) {
            if (currentField.getCommonType().toString().equals(nameOfInterface)) {
                List<String> constructorParams = new ArrayList<>();
                isInitialized.compareAndSet(true, !currentField.isPublic());
                if (!isInitialized.get()) {
                    currentField.findAll(InitializerDeclaration.class).forEach(fieldInitializer -> {
                        isInitialized.set(false);
                    });
                    toTest.findAll(ConstructorDeclaration.class).forEach(constructorDeclaration -> {
                        constructorParams.add(constructorDeclaration.getParameterByType(
                            nameOfInterface).get().getNameAsString());
                    });
                    toTest.findAll(VariableDeclarator.class).forEach(variableDeclarator -> {
                        if (variableDeclarator.getNameAsString().equals(
                            currentField.getVariable(0).getNameAsString())) {
                            if (!variableDeclarator.getInitializer().isEmpty() &&
                                !constructorParams.contains(
                                    variableDeclarator.getInitializer().get().toString())) {
                                isInitialized.set(false);
                            }
                        }
                    });
                }
            }
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

    /**
     * <p>Used to verify whether or not an instance of the decorator pattern has all required
     * elements of said pattern</p>
     * <p>For a pattern instance to be valid it has to contain the following:
     *      <ul>
     *          <li>Exactly one interface component</li>
     *          <li>At least one concrete component</li>
     *          <li>At least one abstract decorator</li>
     *          <li>At least one concrete decorator</li>
     *      </ul>
     * </p>
     *
     * @param pi The pattern instance to verify
     *
     * @return A feedback object containing the boolean result
     */
    private Feedback hasAllElements(PatternInstance pi) {
        String feedbackMessage = "The following elements are missing: ";
        boolean errorOccurred = false;
        if (pi.interfaceComponent == null) {
            feedbackMessage += "Interface component, ";
            errorOccurred = false;
        }
        if (pi.concreteComponents.size() < 1) {
            feedbackMessage += "Concrete component(s), ";
            errorOccurred = false;
        }
        if (pi.abstractDecorators.size() < 1) {
            feedbackMessage += "Abstract component(s), ";
            errorOccurred = false;
        }
        if (pi.concreteDecorators.size() < 1) {
            feedbackMessage += "Concrete decorator(s), ";
            errorOccurred = false;
        }

        if (errorOccurred) {
            // We know that the last two characters are ", " and we want to remove those.
            feedbackMessage = feedbackMessage.substring(0, feedbackMessage.length() - 2);
            return Feedback.getPatternInstanceNoChildFeedback(feedbackMessage);
        }

        return Feedback.getSuccessfulFeedback();
    }
}

