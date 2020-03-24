package patternverifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import base.Pattern;

/**
 * A verifier for the decorator pattern.
 */
public class DecoratorVerifier implements IPatternGroupVerifier {

    /**
     * Constructor.
     */
    public DecoratorVerifier() {
    }

    @Override
    public Feedback verifyGroup(Map<Pattern, List<CompilationUnit>> map) {
        List<PatternInstance> patternInstances = getPatternInstances(map);
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Verifies that a correct implementation of the decorator interface has been found.
     *
     * @param pi An instance of the decorator pattern to be verified
     *
     * @return A {@link Feedback} object that contains the result and information regarding whether
     *     or not the instance of the pattern was valid
     */
    public Feedback verify(PatternInstance pi) {
        throw new UnsupportedOperationException("not implemented");
    }

    //Maybe make a model-folder for all pattern instances instead of having any given PI inside
    // of its own verifier class?


    /**
     * Used to group parts of the same pattern instance (an implementation of the pattern) in one
     * place.
     */
    private class PatternInstance {

        CompilationUnit interfaceComponent;
        List<CompilationUnit> concreteComponents = new ArrayList<>();
        List<CompilationUnit> abstractDecorators = new ArrayList<>();
        List<CompilationUnit> concreteDecorators = new ArrayList<>();

        public PatternInstance() {
        }
    }

    /**
     * Checks that an interfaceComponent contains atleast one method.
     *
     * @param interfaceComponent
     *
     * @return
     */
    public Feedback interfaceContainsMethod(CompilationUnit interfaceComponent) {
        Feedback result;
        AtomicBoolean resultBool = new AtomicBoolean(false);
        interfaceComponent.findAll(MethodDeclaration.class).forEach(methodDeclaration -> {
            if (!methodDeclaration.isPrivate()) {
                resultBool.set(true);
            }
        });
        if (resultBool.get()) {
            result = new Feedback(true, "Interface contains atleast one method.");
        } else {
            result = new Feedback(false, "Interface does not contain any methods!");
        }

        return result;
    }

    /**
     * Method for identifying which {@link CompilationUnit}s are part of the same decorator pattern
     * instance. The identified instances will be returned as a list of {@link PatternInstance}
     *
     * @param map A map where every element of the decorator pattern (e.g. concrete decorator) is
     *            mapped to all the compilation units of said element type
     *
     * @return A list of all identified instances of the pattern
     */
    public List<PatternInstance> getPatternInstances(Map<Pattern, List<CompilationUnit>> map) {
        List<CompilationUnit> interfaceComponents = map.get(Pattern.DECORATOR_INTERFACE_COMPONENT);
        List<CompilationUnit> concreteComponents = map.get(Pattern.DECORATOR_CONCRETE_COMPONENT);
        List<CompilationUnit> abstractDecorators = map.get(Pattern.DECORATOR_ABSTRACT_DECORATOR);
        List<CompilationUnit> concreteDecorators = map.get(Pattern.DECORATOR_CONCRETE_DECORATOR);
        HashMap<CompilationUnit, PatternInstance> componentToPatternInstance = new HashMap();

        //Create incomplete PIs to be populated below, easier to use isDescendantOf below by
        // doing this
        interfaceComponents.forEach((interfaceComponent) -> {
            PatternInstance pi = new PatternInstance();
            pi.interfaceComponent = interfaceComponent;
            componentToPatternInstance.putIfAbsent(interfaceComponent, pi);
        });

        //Found no better way to populate PIs than looping through everything multiple times
        interfaceComponents.forEach((interfaceComponentCU) -> {
            var componentInterface = interfaceComponentCU.findFirst(
                ClassOrInterfaceDeclaration.class);
            if (componentInterface.isPresent() && componentInterface.get().isInterface()) {
                String interfaceName = componentInterface.get().getNameAsString();

                concreteComponents.forEach(cc -> {
                    ClassOrInterfaceDeclaration concComp = cc.findFirst(
                        ClassOrInterfaceDeclaration.class).get();
                    concComp.getImplementedTypes().forEach(implementedInterface -> {
                        if (implementedInterface.getNameAsString().equals(interfaceName)) {
                            PatternInstance pi = componentToPatternInstance.get(
                                interfaceComponentCU);
                            pi.concreteComponents.add(cc);
                        }
                    });
                });

                abstractDecorators.forEach(ad -> {
                    ClassOrInterfaceDeclaration absDec = ad.findFirst(
                        ClassOrInterfaceDeclaration.class).get();
                    absDec.getImplementedTypes().forEach(implementedInterface -> {
                        if (implementedInterface.getNameAsString().equals(interfaceName)) {
                            PatternInstance pi = componentToPatternInstance.get(
                                interfaceComponentCU);
                            pi.abstractDecorators.add(ad);

                            //Check which concreteComponents extend this abstractDecorator
                            //And update PI accordingly
                            concreteDecorators.forEach(cd -> {
                                ClassOrInterfaceDeclaration concDec = cd.findFirst(
                                    ClassOrInterfaceDeclaration.class).get();
                                concDec.getExtendedTypes().forEach(extendedClass -> {
                                    if (extendedClass.getNameAsString().equals(
                                        absDec.getName().asString())) {
                                        pi.concreteDecorators.add(cd);
                                    }
                                });
                            });
                        }
                    });
                });
            } else {
                throw new UnsupportedOperationException(
                    "Was not an interface or something wrong happened");
            }
        });
        return new ArrayList<PatternInstance>(componentToPatternInstance.values());
    }

    /**
     * Method for checking that the CompilationUnit toTest (class) contains a field of a certain
     * type.
     *
     * @param toTest      The CompilationUnit (class) to check
     * @param interfaceCU The (CompilationUnit of the) interface to search for in toTest
     *
     * @return true iff toTest has a variable of the same type as the interface in interfaceCU
     */
    private Feedback hasAComponent(CompilationUnit toTest, CompilationUnit interfaceCU) {
        Feedback result;
        AtomicBoolean hasAComponent = new AtomicBoolean(false);
        toTest.findAll(VariableDeclarator.class).forEach(fieldDeclaration -> {
            if (fieldDeclaration.getTypeAsString().contains(
                interfaceCU.getPrimaryTypeName().get())) {
                hasAComponent.set(true);
            }
        });
        if (hasAComponent.get()) {
            result = new Feedback(true, "Component was found for class " +
                                        toTest.getPrimaryTypeName().get());
        } else {
            result = new Feedback(false, "There was no Component field found for class " +
                                         toTest.getPrimaryTypeName().get());
        }
        return result;
    }

    /**
     * Method to check if all constructors in a given class initialize the Component field in the
     * class.
     *
     * @param toTest The CompilationUnit (class) to check.
     *
     * @return True iff all constructors in a given class does initialize the class' Component
     */
    private Feedback componentInitializedInConstructor(
        CompilationUnit toTest, CompilationUnit interfaceCU) {
        Feedback result;
        AtomicBoolean isInitialized = new AtomicBoolean(true);
        List<FieldDeclaration> fieldsInClass = new ArrayList<>();
        toTest.findAll(FieldDeclaration.class).forEach(fieldDeclaration -> {
            fieldsInClass.add(fieldDeclaration);
        });
        String nameOfInterface = interfaceCU.getPrimaryTypeName().get();
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
            result = new Feedback(true, "All constructors initialize the Component field");
        } else {
            result = new Feedback(false, "All constructors did not initialize the Component field");
        }
        return result;
    }
}
