package patternverifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

/**
 * A verifier for the decorator pattern.
 */
public class DecoratorVerifier implements IPatternVerifier {

    private final CompilationUnit interfaceName;
    private final List<CompilationUnit> components = new ArrayList<>();
    private List<CompilationUnit> absDecorator;
    // Does not necessarily need to be abstract
    // or an interface, could be a concrete class
    private List<CompilationUnit> decorators;    // Class that extends the iDecorator

    /**
     * Constructor.
     */
    public DecoratorVerifier(
        CompilationUnit interfaceCompUnit/*, List<CompilationUnit> components*/) {
        this.interfaceName = interfaceCompUnit;
        //this.components = components;
    }

    /**
     * Verifies that every class in the CompilationUnit is part of the Decorator pattern.
     *
     * @param compUnit The CompilationUnit of files to look at
     *
     * @return True if all classes in the CompilationUnit is part of the Decorator design pattern
     */
    @Override
    public boolean verify(CompilationUnit compUnit) {
        return componentInitializedInConstructor(compUnit).getValue() && hasAComponent(compUnit)
            .getValue();
    }

    /**
     * Method for checking that the CompilationUnit toTest has a component of the same type as the
     * interface found in iComponent.
     *
     * @param toTest The CompilationUnit (class) to check
     *
     * @return true iff toTest has a variable of the same type as the interface found in iComponent
     */
    private Feedback hasAComponent(CompilationUnit toTest) {
        Feedback result;
        AtomicBoolean hasAComponent = new AtomicBoolean(false);  // Declare result variable
        toTest.findAll(VariableDeclarator.class).forEach(fieldDeclaration -> {  //For each
            // variable declaration in toTest
            if (fieldDeclaration.getTypeAsString()// If the variable declaration
                                .contains(interfaceName.getPrimaryTypeName().get())) {      // Is
                // of the same type as the
                // interface iComponents
                hasAComponent.set(true); // set the result to true

            }
        });
        if (hasAComponent.get()) {
            result = new Feedback(true, "Component was found for class " +
                                        toTest.getPrimaryTypeName().get());
        } else {
            result = new Feedback(false, "There was no Component field found for class " +
                                         toTest.getPrimaryTypeName().get());
        }
        return result; // Return result
    }

    /**
     * Method to check if all constructors in a given class initialize the Component field in the
     * class.
     *
     * @param toTest The CompilationUnit (class) to check.
     *
     * @return True iff all constructors in a given class does initialize the class' Component
     */
    private Feedback componentInitializedInConstructor(CompilationUnit toTest) {
        Feedback result;
        AtomicBoolean isInitialized = new AtomicBoolean(true);
        List<FieldDeclaration> fieldsInClass = new ArrayList<>();
        toTest.findAll(FieldDeclaration.class).forEach(fieldDeclaration -> {
            fieldsInClass.add(fieldDeclaration);
        });
        String nameOfInterface = interfaceName.getPrimaryTypeName().get();
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
