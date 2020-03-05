package patternverifiers;

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

/**
 * A verifier for the decorator pattern.
 */
public class DecoratorVerifier implements IPatternVerifier {

    private CompilationUnit interfaceCompUnit;
    private List<CompilationUnit> compUnits;

    /**
     * Constructor.
     */
    public DecoratorVerifier() {
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
        
        return false;
    }
}
