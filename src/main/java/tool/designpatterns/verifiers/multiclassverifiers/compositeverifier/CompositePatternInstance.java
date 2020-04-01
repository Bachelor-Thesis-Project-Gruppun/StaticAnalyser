package tool.designpatterns.verifiers.multiclassverifiers.compositeverifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import tool.designpatterns.Pattern;
import tool.designpatterns.verifiers.APatternInstance;

/**
 * Class which defines how an instance of Composte is structured, created and is neccesary for it to
 * function.
 */
public final class CompositePatternInstance extends APatternInstance {

    private final ClassOrInterfaceDeclaration component;
    private final List<ClassOrInterfaceDeclaration> nodes;
    private final List<ClassOrInterfaceDeclaration> leaves;

    public ClassOrInterfaceDeclaration getComponent() {
        return component;
    }

    public List<ClassOrInterfaceDeclaration> getNodes() {
        return nodes;
    }

    /**
     * Cosntructor for the pattern instance. makes sure alla fields are initialised.
     *
     * @param component The interface or absract class which defines the composite instance.
     */
    public CompositePatternInstance(ClassOrInterfaceDeclaration component) {
        super();
        this.component = component;
        this.leaves = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    /**
     * Method for identifying which classes are part of the same decorator pattern instance.
     *
     * @param map A map where every element of the decorator pattern (e.g. concrete decorator) is
     *            mapped to all the classes of said element type
     *
     * @return A list of all identified instances of the pattern
     */
    public static List<CompositePatternInstance> createInstancesFromMap(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {
        List<ClassOrInterfaceDeclaration> components = map.get(Pattern.COMPOSITE_COMPONENT);
        List<ClassOrInterfaceDeclaration> nodes = map.get(Pattern.COMPOSITE_CONTAINER);
        List<ClassOrInterfaceDeclaration> leaves = map.get(Pattern.COMPOSITE_LEAF);

        HashMap<ClassOrInterfaceDeclaration, CompositePatternInstance> patternInstances =
            new HashMap<>();

        components.forEach((component) -> {
            CompositePatternInstance patternInstance = new CompositePatternInstance(component);
            patternInstances.putIfAbsent(component, patternInstance);
        });

        ArrayList<ClassOrInterfaceDeclaration> identifiedElements = new ArrayList<>();

        // Found no better way to populate PatternInstances than looping
        // through everything multiple times
        components.forEach((component) -> {
            String componentName = component.getNameAsString();
            nodes.forEach(node -> {
                List<ClassOrInterfaceType> types = new ArrayList<>();
                types.addAll(node.getExtendedTypes());
                types.addAll(node.getImplementedTypes());
                types.forEach(classOrInterface -> {
                    if (classOrInterface.getNameAsString().equals(componentName)) {
                        CompositePatternInstance patternInstance = patternInstances.get(component);
                        patternInstance.nodes.add(node);
                        identifiedElements.add(node);
                    }
                });
            });
            leaves.forEach(leaf -> {
                List<ClassOrInterfaceType> types = new ArrayList<>();
                types.addAll(leaf.getExtendedTypes());
                types.addAll(leaf.getImplementedTypes());
                types.forEach(classOrInterface -> {
                    if (classOrInterface.getNameAsString().equals(componentName)) {
                        CompositePatternInstance patternInstance = patternInstances.get(component);
                        patternInstance.leaves.add(leaf);
                        identifiedElements.add(leaf);
                    }
                });
            });

            // If any elements are left then they are invalid instances
            nodes.removeAll(identifiedElements);
            leaves.removeAll(identifiedElements);

        });
        // If there are elements that do not relate to any of the previous
        // interface components, since they are invalid, put them in an
        // invalid pattern instance object for verify() to handle
        if (!(nodes.isEmpty() && leaves.isEmpty())) {
            var noComponentInstance = new CompositePatternInstance(null);
            noComponentInstance.nodes.addAll(nodes);
            noComponentInstance.leaves.addAll(leaves);
            patternInstances.put(null, noComponentInstance);
        }

        return new ArrayList<>(patternInstances.values());
    }

    /**
     * Component may not bit be null and there has to be atleast one nodes.
     *
     * @param feedbackMessage Stringbuilder used in hasAllElements
     *
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
            errorOccured = true;
        }

        return errorOccured;
    }

    @Override
    public String toString() {
        return "CompositePatternInstance{" + "component=" + component + ", nodes=" + nodes.size() +
               ", leaves=" + leaves.size() + '}';
    }
}
