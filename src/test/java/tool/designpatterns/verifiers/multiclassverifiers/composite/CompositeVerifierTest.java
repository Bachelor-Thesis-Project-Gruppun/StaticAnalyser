package tool.designpatterns.verifiers.multiclassverifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.gradle.internal.impldep.org.junit.Assert.assertTrue;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import org.junit.jupiter.api.Test;
import tool.designpatterns.Pattern;
import tool.designpatterns.verifiers.multiclassverifiers.compositeverifier.CompositePatternInstance;
import tool.designpatterns.verifiers.multiclassverifiers.compositeverifier.CompositeVerifier;
import utilities.TestHelper;

public class CompositeVerifierTest {

    @Test
    public void testCorrectVerify() throws IOException {
        ClassOrInterfaceDeclaration container = TestHelper.getMockClassOrI(
            "composite", "CorrectContainerAllLoops");
        ClassOrInterfaceDeclaration leaf = TestHelper.getMockClassOrI("composite", "Leaf");
        ClassOrInterfaceDeclaration component = TestHelper.getMockClassOrI("composite",
                                                                           "Component");

        CompositePatternInstance patternInstance = new CompositePatternInstance(component);
        patternInstance.getContainers().add(container);
        patternInstance.getLeaves().add(leaf);
        assertFalse(new CompositeVerifier().verifyPatternInstance(patternInstance).getIsError());
    }

    @Test
    public void testFailingContainerNotDelegating() throws IOException {
        ClassOrInterfaceDeclaration container = TestHelper.getMockClassOrI(
            "composite", "FailingContainerNoDelegate");
        ClassOrInterfaceDeclaration leaf = TestHelper.getMockClassOrI("composite", "Leaf");
        ClassOrInterfaceDeclaration component = TestHelper.getMockClassOrI("composite",
                                                                           "Component");

        CompositePatternInstance patternInstance = new CompositePatternInstance(component);
        patternInstance.getContainers().add(container);
        patternInstance.getLeaves().add(leaf);
        assertTrue(new CompositeVerifier().verifyPatternInstance(patternInstance).getIsError());
    }

    @Test
    public void testFailingContainerNotImplementingComponent() throws IOException {
        ClassOrInterfaceDeclaration container = TestHelper.getMockClassOrI(
            "composite", "FailingContainerNoImplementsComponent");
        ClassOrInterfaceDeclaration leaf = TestHelper.getMockClassOrI("composite", "Leaf");
        ClassOrInterfaceDeclaration component = TestHelper.getMockClassOrI("composite",
                                                                           "Component");

        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = createPatternGroup(
            container, leaf, component);

        assertTrue(new CompositeVerifier().verifyGroup(patternGroup).hasError());
    }

    @Test
    public void testFailingContainerHasNoCollection() throws IOException {
        ClassOrInterfaceDeclaration container = TestHelper.getMockClassOrI(
            "composite", "FailingContainerNoCollection");
        ClassOrInterfaceDeclaration leaf = TestHelper.getMockClassOrI("composite", "Leaf");
        ClassOrInterfaceDeclaration component = TestHelper.getMockClassOrI("composite",
                                                                           "Component");

        CompositePatternInstance patternInstance = new CompositePatternInstance(component);
        patternInstance.getContainers().add(container);
        patternInstance.getLeaves().add(leaf);
        assertTrue(new CompositeVerifier().verifyPatternInstance(patternInstance).getIsError());
    }

    @Test
    public void testFailingContainerMissingPart() throws IOException {
        ClassOrInterfaceDeclaration container = TestHelper.getMockClassOrI(
            "composite", "CorrectContainerAllLoops");
        ClassOrInterfaceDeclaration leaf = TestHelper.getMockClassOrI("composite", "Leaf");
        ClassOrInterfaceDeclaration component = TestHelper.getMockClassOrI("composite",
                                                                           "Component");

        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> noContainer = createPatternGroup(
            null, leaf, component);
        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> noComponent = createPatternGroup(
            container, leaf, null);

        assertTrue(new CompositeVerifier().verifyGroup(noContainer).hasError());
        assertTrue(new CompositeVerifier().verifyGroup(noComponent).hasError());
    }

    private HashMap<Pattern, List<ClassOrInterfaceDeclaration>> createPatternGroup(
        ClassOrInterfaceDeclaration container, ClassOrInterfaceDeclaration leaf,
        ClassOrInterfaceDeclaration component) {
        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = new HashMap<>();
        List<ClassOrInterfaceDeclaration> containers = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> leaves = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> components = new ArrayList<>();

        if (container != null) {
            containers.add(container);
        }
        if (leaf != null) {
            leaves.add(leaf);
        }
        if (component != null) {
            components.add(component);
        }

        patternGroup.put(Pattern.COMPOSITE_COMPONENT, components);
        patternGroup.put(Pattern.COMPOSITE_LEAF, leaves);
        patternGroup.put(Pattern.COMPOSITE_CONTAINER, containers);
        return patternGroup;
    }

}
