package tool.designpatterns.verifiers.multiclassverifiers.decorator;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.designpatterns.Pattern;
import tool.feedback.Feedback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to group parts of the same instance of the decorator pattern (an
 * implementation of the pattern) in one place.
 */
final class DecoratorPatternInstance {

    public ClassOrInterfaceDeclaration interfaceComponent;
    public List<ClassOrInterfaceDeclaration> concreteComponents =
        new ArrayList<>();
    public List<ClassOrInterfaceDeclaration> abstractDecorators =
        new ArrayList<>();
    public List<ClassOrInterfaceDeclaration> concreteDecorators =
        new ArrayList<>();

    private DecoratorPatternInstance() {
    }

    /**
     * Method for identifying which classes are part of the same decorator
     * pattern instance.
     *
     * @param map A map where every element of the decorator pattern (e.g.
     *            concrete decorator) is mapped to all the classes of said
     *            element type
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
        HashMap<ClassOrInterfaceDeclaration, DecoratorPatternInstance>
            patternInstances = new HashMap();

        interfaceComponents.forEach((interfaceComponent) -> {
            DecoratorPatternInstance patternInstance =
                new DecoratorPatternInstance();
            patternInstance.interfaceComponent = interfaceComponent;
            patternInstances.putIfAbsent(interfaceComponent, patternInstance);
        });

        ArrayList<ClassOrInterfaceDeclaration> identifiedElements =
            new ArrayList<>();

        // Found no better way to populate PatternInstances than looping
        // through everything multiple times
        interfaceComponents.forEach((interfaceComponent) -> {
            if (interfaceComponent.isInterface()) {
                String interfaceName = interfaceComponent.getNameAsString();
                concreteComponents.forEach(cc -> {
                    cc.getImplementedTypes().forEach(implementedInterface -> {
                        if (implementedInterface.getNameAsString().equals(
                            interfaceName)) {
                            DecoratorPatternInstance patternInstance =
                                patternInstances.get(interfaceComponent);
                            patternInstance.concreteComponents.add(cc);
                            identifiedElements.add(cc);
                        }
                    });
                });

                abstractDecorators.forEach(ad -> {
                    ad.getImplementedTypes().forEach(implementedInterface -> {
                        if (implementedInterface.getNameAsString().equals(
                            interfaceName)) {
                            DecoratorPatternInstance patternInstance =
                                patternInstances.get(interfaceComponent);
                            patternInstance.abstractDecorators.add(ad);
                            identifiedElements.add(ad);

                            concreteDecorators.forEach(cd -> {
                                cd.getExtendedTypes().forEach(extendedClass -> {
                                    if (extendedClass.getNameAsString().equals(
                                        ad.getName().asString())) {
                                        patternInstance.concreteDecorators.add(
                                            cd);
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
            } else {
                throw new UnsupportedOperationException("Was not an interface");
            }
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

        return new ArrayList<DecoratorPatternInstance>(
            patternInstances.values());
    }

    /**
     * <p>Verifies whether or not an instance of the pattern has all required
     * elements.</p>
     * <p>For a pattern instance to be valid it has to contain the following:
     * <ul><li>Exactly one interface component</li><li>At least one of each of
     * following concrete component, abstract decorator and concrete
     * decorator</li></ul></p>
     *
     * @param patternInstance The pattern instance to verify
     *
     * @return A feedback object containing the boolean result
     */
    @SuppressWarnings("PMD.LinguisticNaming")
    public Feedback hasAllElements(DecoratorPatternInstance patternInstance) {
        StringBuilder feedbackMessage = new StringBuilder(127);
        feedbackMessage.append("The following elements are missing: ");
        boolean errorOccurred = false;
        if (patternInstance.interfaceComponent == null) {
            feedbackMessage.append("Interface component, ");
            errorOccurred = true;
        }
        if (patternInstance.concreteComponents.isEmpty()) {
            feedbackMessage.append("Concrete component(s), ");
            errorOccurred = true;
        }
        if (patternInstance.abstractDecorators.isEmpty()) {
            feedbackMessage.append("Abstract component(s), ");
            errorOccurred = true;
        }
        if (patternInstance.concreteDecorators.isEmpty()) {
            feedbackMessage.append("Concrete decorator(s), ");
            errorOccurred = true;
        }

        if (errorOccurred) {
            // We know that the last two characters are ", " and we want to remove those.
            feedbackMessage.deleteCharAt(feedbackMessage.length() - 1);
            feedbackMessage.deleteCharAt(feedbackMessage.length() - 2);
            feedbackMessage.append('.');
            return Feedback.getPatternInstanceNoChildFeedback(
                feedbackMessage.toString());
        }

        return Feedback.getSuccessfulFeedback();
    }
}
