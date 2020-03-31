package tool.designpatterns.verifiers.multiclassverifiers.compositeverifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.designpatterns.Pattern;
import tool.designpatterns.verifiers.APatternInstance;
import tool.feedback.Feedback;

public class CompositePatternInstance extends APatternInstance {

    private ClassOrInterfaceDeclaration component;
    private List<ClassOrInterfaceDeclaration> nodes = new ArrayList<>();
    private List<ClassOrInterfaceDeclaration> leaves = new ArrayList<>();

    private CompositePatternInstance(ClassOrInterfaceDeclaration component) {
        this.component = component;
        this.leaves = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    private ClassOrInterfaceDeclaration getComponent() {
        return component;
    }

    private List<ClassOrInterfaceDeclaration> getNodes() {
        return nodes;
    }

    private List<ClassOrInterfaceDeclaration> getLeaves() {
        return leaves;
    }

    /**
     * Method for identifying which classes are part of the same decorator pattern instance.
     *
     * @param map A map where every element of the decorator pattern (e.g. concrete
     *            decorator) is
     *            mapped to all the classes of said element type
     *
     * @return A list of all identified instances of the pattern
     */
    public static List<CompositePatternInstance> getPatternInstances(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {
        List<ClassOrInterfaceDeclaration> components = map.get(
            Pattern.COMPOSITE_COMPONENT);
        List<ClassOrInterfaceDeclaration> nodes = map.get(
            Pattern.COMPOSITE_NODES);
        List<ClassOrInterfaceDeclaration> leaves = map.get(
            Pattern.COMPOSITE_LEAF);

        HashMap<ClassOrInterfaceDeclaration, CompositePatternInstance> patternInstances =
            new HashMap();

        components.forEach((component) -> {
            CompositePatternInstance patternInstance = new CompositePatternInstance(component);
            patternInstances.putIfAbsent(component, patternInstance);
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

        return new ArrayList<CompositePatternInstance>(patternInstances.values());
    }

    /**
     *
     *
     * @param feedbackMessage Stringbuilder used in hasAllElements
     * @return boolean signifying if an error has occured
     */
    @Override
    protected boolean checkElements(StringBuilder feedbackMessage) {
        boolean errorOccured = false;

        if (component == null) {
            feedbackMessage.append("Component, ");
            errorOccured = true;
        }
        if (nodes.isEmpty()) {
            feedbackMessage.append("node(s), ");
            errorOccured =true;
        }

        return errorOccured;
    }
}
