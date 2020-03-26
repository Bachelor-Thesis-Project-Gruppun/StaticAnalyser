package patternverifiers;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.gradle.internal.impldep.org.junit.Assert.assertTrue;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import org.junit.jupiter.api.Test;
import tool.designpatterns.Pattern;
import tool.designpatterns.verifiers.multiclassverifiers.DecoratorVerifier;
import utilities.TestHelper;

/**
 * Pattern verifier for the decorator pattern.
 */
public class DecoratorVerifierTest {

    @Test
    public void testCorrectImplementation() {
        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> testMap = new HashMap();
        ArrayList<ClassOrInterfaceDeclaration> interfaceComponents =
            new ArrayList<ClassOrInterfaceDeclaration>();
        ArrayList<ClassOrInterfaceDeclaration> concreteComponents =
            new ArrayList<ClassOrInterfaceDeclaration>();
        ArrayList<ClassOrInterfaceDeclaration> abstractDecorators =
            new ArrayList<ClassOrInterfaceDeclaration>();
        ArrayList<ClassOrInterfaceDeclaration> concreteDecorators =
            new ArrayList<ClassOrInterfaceDeclaration>();

        try {
            concreteComponents.add(
                TestHelper.getMockClassOrI("Decorator/correctpattern", "Coffee"));
            abstractDecorators.add(
                TestHelper.getMockClassOrI("Decorator/correctpattern", "CoffeeDecorator"));
            interfaceComponents.add(
                TestHelper.getMockClassOrI("Decorator/correctpattern", "IBeverageComponent"));
            concreteDecorators.add(TestHelper.getMockClassOrI("Decorator/correctpattern", "Milk"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        testMap.put(Pattern.DECORATOR_INTERFACE_COMPONENT, interfaceComponents);
        testMap.put(Pattern.DECORATOR_CONCRETE_COMPONENT, concreteComponents);
        testMap.put(Pattern.DECORATOR_ABSTRACT_DECORATOR, abstractDecorators);
        testMap.put(Pattern.DECORATOR_CONCRETE_DECORATOR, concreteDecorators);
        var decoratorVerifier = new DecoratorVerifier();
        assertFalse(decoratorVerifier.verifyGroup(testMap).hasError());
    }

    @Test
    public void testIncorrectImplementation() {
        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> testMap = new HashMap();
        ArrayList<ClassOrInterfaceDeclaration> interfaceComponents =
            new ArrayList<ClassOrInterfaceDeclaration>();
        ArrayList<ClassOrInterfaceDeclaration> concreteComponents =
            new ArrayList<ClassOrInterfaceDeclaration>();
        ArrayList<ClassOrInterfaceDeclaration> abstractDecorators =
            new ArrayList<ClassOrInterfaceDeclaration>();
        ArrayList<ClassOrInterfaceDeclaration> concreteDecorators =
            new ArrayList<ClassOrInterfaceDeclaration>();

        try {
            concreteComponents.add(
                TestHelper.getMockClassOrI("Decorator/failingpattern", "FailingCoffee"));
            abstractDecorators.add(
                TestHelper.getMockClassOrI("Decorator/failingpattern", "FailingCoffeeDecorator"));
            interfaceComponents.add(TestHelper.getMockClassOrI("Decorator/failingpattern",
                                                               "IFailingBeverageComponent"));
            concreteDecorators.add(
                TestHelper.getMockClassOrI("Decorator/failingpattern", "FailingMilk"));
            concreteDecorators.add(
                TestHelper.getMockClassOrI("Decorator/failingpattern", "FailingCream"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        testMap.put(Pattern.DECORATOR_INTERFACE_COMPONENT, interfaceComponents);
        testMap.put(Pattern.DECORATOR_CONCRETE_COMPONENT, concreteComponents);
        testMap.put(Pattern.DECORATOR_ABSTRACT_DECORATOR, abstractDecorators);
        testMap.put(Pattern.DECORATOR_CONCRETE_DECORATOR, concreteDecorators);
        var decoratorVerifier = new DecoratorVerifier();
        assertTrue(decoratorVerifier.verifyGroup(testMap).hasError());

    }
}
