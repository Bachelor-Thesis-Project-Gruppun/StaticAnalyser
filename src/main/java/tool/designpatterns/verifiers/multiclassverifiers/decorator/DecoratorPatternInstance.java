package tool.designpatterns.verifiers.multiclassverifiers.decorator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.designpatterns.Pattern;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;

/**
 * Used to group parts of the same instance of the decorator pattern (an implementation of the
 * pattern) in one place.
 */
@SuppressWarnings({"PMD.LinguisticNaming", "PMD.CommentSize"})
final class DecoratorPatternInstance {

    private ClassOrInterfaceDeclaration interfaceComponent;
    private List<ClassOrInterfaceDeclaration> concreteComponents = new ArrayList<>();
    private List<ClassOrInterfaceDeclaration> abstractDecorators = new ArrayList<>();
    private List<ClassOrInterfaceDeclaration> concreteDecorators = new ArrayList<>();

    private DecoratorPatternInstance() {
    }

    public ClassOrInterfaceDeclaration getInterfaceComponent() {
        return interfaceComponent;
    }

    public List<ClassOrInterfaceDeclaration> getConcreteComponents() {
        return concreteComponents;
    }

    public List<ClassOrInterfaceDeclaration> getAbstractDecorators() {
        return abstractDecorators;
    }

    public List<ClassOrInterfaceDeclaration> getConcreteDecorators() {
        return concreteDecorators;
    }

    /**
     * Method for identifying which classes are part of the same decorator pattern instance.
     *
     * @param map A map where every element of the decorator pattern (e.g. concrete decorator) is
     *            mapped to all the classes of said element type
     *
     * @return A list of all identified instances of the pattern
     */
    public static List<DecoratorPatternInstance> getPatternInstances(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {
        List<ClassOrInterfaceDeclaration> interfaceComponents = map.get(
            Pattern.DECORATOR_INTERFACE_COMPONENT);
        List<ClassOrInterfaceDeclaration> concreteComponents = map.get(
            Pattern.DECORATOR_CONCRETE_COMPONENT);
        List<ClassOrInterfaceDeclaration> abstractDecorators = map.get(
            Pattern.DECORATOR_ABSTRACT_DECORATOR);
        List<ClassOrInterfaceDeclaration> concreteDecorators = map.get(
            Pattern.DECORATOR_CONCRETE_DECORATOR);
        HashMap<ClassOrInterfaceDeclaration, DecoratorPatternInstance> patternInstances =
            new HashMap();

        interfaceComponents.forEach((interfaceComponent) -> {
            DecoratorPatternInstance patternInstance = new DecoratorPatternInstance();
            patternInstance.interfaceComponent = interfaceComponent;
            patternInstances.putIfAbsent(interfaceComponent, patternInstance);
        });

        ArrayList<ClassOrInterfaceDeclaration> identifiedElements = new ArrayList<>();

        // Found no better way to populate PatternInstances than looping
        // through everything multiple times
        interfaceComponents.forEach((interfaceComponent) -> {
            String interfaceName = interfaceComponent.getNameAsString();
            concreteComponents.forEach(cc -> {
                cc.getImplementedTypes().forEach(implementedInterface -> {
                    if (implementedInterface.getNameAsString().equals(interfaceName)) {
                        DecoratorPatternInstance patternInstance = patternInstances.get(
                            interfaceComponent);
                        patternInstance.concreteComponents.add(cc);
                        identifiedElements.add(cc);
                    }
                });
            });

            abstractDecorators.forEach(ad -> {
                ad.getImplementedTypes().forEach(implementedInterface -> {
                    if (implementedInterface.getNameAsString().equals(interfaceName)) {
                        DecoratorPatternInstance patternInstance = patternInstances.get(
                            interfaceComponent);
                        patternInstance.abstractDecorators.add(ad);
                        identifiedElements.add(ad);

                        concreteDecorators.forEach(cd -> {
                            cd.getExtendedTypes().forEach(extendedClass -> {
                                if (extendedClass.getNameAsString().equals(
                                    ad.getName().asString())) {
                                    patternInstance.concreteDecorators.add(cd);
                                    identifiedElements.add(cd);
                                }
                            });
                        });
                    }
                });
            });
            // If any elements are left then they are invalid instances
            concreteComponents.removeAll(identifiedElements);
            abstractDecorators.removeAll(identifiedElements);
            concreteDecorators.removeAll(identifiedElements);

        });
        // If there are elements that do not relate to any of the previous
        // interface components, since they are invalid, put them in an
        // invalid pattern instance object for verify() to handle
        if (!(
            concreteComponents.isEmpty() && abstractDecorators.isEmpty() &&
            concreteDecorators.isEmpty())) {
            var patternInstance = new DecoratorPatternInstance();
            patternInstance.concreteComponents = concreteComponents;
            patternInstance.abstractDecorators = abstractDecorators;
            patternInstance.concreteDecorators = concreteDecorators;
            patternInstances.put(null, patternInstance);
        }

        return new ArrayList<DecoratorPatternInstance>(patternInstances.values());
    }

    /**
     * <p>Verifies whether or not an instance of the pattern has all required elements.</p>
     * <p>For a pattern instance to be valid it has to contain the following:
     *      <ul>
     *          <li>Exactly one interface component</li>
     *          <li>At least one of each of following: concrete component, abstract decorator and
     *          concrete decorator</li>
     *      </ul>
     * </p>
     *
     * @return A feedback object
     */
    public Feedback hasAllElements() {
        if (this.interfaceComponent == null) {
            return getInvalidInstanceFeedback();
        }

        StringBuilder feedbackMessage = new StringBuilder(127);
        boolean errorOccurred = false;
        if (this.interfaceComponent.isInterface()) {
            String interfaceCompName = this.getInterfaceComponent().getFullyQualifiedName().get();
            String initMsg = "The following elements are missing in the interface " + "component " +
                             interfaceCompName + ": ";
            feedbackMessage.append(initMsg);
            if (this.concreteComponents.isEmpty()) {
                feedbackMessage.append("Concrete component(s): ");
                errorOccurred = true;
            }
            if (this.abstractDecorators.isEmpty()) {
                feedbackMessage.append("Abstract component(s): ");
                errorOccurred = true;
            }
            if (this.concreteDecorators.isEmpty()) {
                feedbackMessage.append("Concrete decorator(s): ");
                errorOccurred = true;
            }
        } else {
            String intCompName = this.getInterfaceComponent().getFullyQualifiedName().get();
            String msg = "The interface component " + intCompName + " is not an interface.";
            feedbackMessage.append(msg);
            errorOccurred = true;
        }
        if (errorOccurred) {
            return Feedback.getNoChildFeedback(feedbackMessage.toString(),
                                               new FeedbackTrace(interfaceComponent));
        }
        return Feedback.getSuccessfulFeedback();
    }

    /**
     * Constructs a feedback message for "the" invalid pattern instance. "The" invalid pattern
     * instance contains all annotated elements that are invalid, in other words not part of any
     * structurally valid instance.
     *
     * @return a feedback object containing information about each class annotated, but not found to
     *         be part of a pattern instance
     */
    private Feedback getInvalidInstanceFeedback() {
        List<Feedback> childFeedbacks = new ArrayList<>();
        String msg = "Several elements were not identified to be part of any instance of the " +
                     "decorator pattern. Please verify that all decorators and concrete " +
                     "components implement an interface marked with the interface component " +
                     "annotation and that all concrete decorators inherit from an abstract " +
                     "decorator: ";
        childFeedbacks.add(Feedback.getPatternInstanceNoChildFeedback(msg));
        this.getConcreteComponents().forEach(cc -> {
            childFeedbacks.add(Feedback.getNoChildFeedback(
                "Could not identify the class to be part of any instance of the " +
                "decorator pattern where it is a concrete component.", new FeedbackTrace(cc)));
        });
        this.getAbstractDecorators().forEach(ad -> {
            childFeedbacks.add(Feedback.getNoChildFeedback(
                "Could not identify the class to be part of any instance of the " +
                "decorator pattern where it is an abstract decorator.", new FeedbackTrace(ad)));
        });
        this.getConcreteDecorators().forEach(cd -> {
            childFeedbacks.add(Feedback.getNoChildFeedback(
                "Could not identify the class to be part of any instance of the " +
                "decorator pattern where it is a concrete decorator.", new FeedbackTrace(cd)));
        });
        return Feedback.getPatternInstanceFeedback(childFeedbacks);
    }
}
