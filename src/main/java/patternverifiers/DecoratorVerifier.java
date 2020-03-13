package patternverifiers;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;

/**
 * A verifier for the decorator pattern.
 */
public class DecoratorVerifier implements IPatternVerifier {

    private final transient CompilationUnit interfaceName;
    //private final transient List<CompilationUnit> components;
    //    private List<CompilationUnit> absDecorator;
    //    // Does not necessarily need to be abstract
    //    // or an interface, could be a concrete class
    //    private List<CompilationUnit> decorators;    // Class that extends the iDecorator

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
        return hasAComponent(compUnit);
    }

    /**
     * Method for checking that the CompilationUnit toTest has a component of the same type as the
     * interface found in iComponent.
     *
     * @param toTest The CompilationUnit (class) to check
     *
     * @return true iff toTest has a variable of the same type as the interface found in iComponent
     */
    private boolean hasAComponent(CompilationUnit toTest) {
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
        return hasAComponent.get(); // Return result
    }

}
