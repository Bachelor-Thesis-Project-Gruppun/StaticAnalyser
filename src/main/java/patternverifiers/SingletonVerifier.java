package patternverifiers;

import com.github.javaparser.ast.CompilationUnit;

/**
 * A verifier for the singleton pattern.
 */
public class SingletonVerifier implements IPatternVerifier {

    public SingletonVerifier() {
    }

    @Override
    public boolean verify(CompilationUnit compUnit) {
        throw new UnsupportedOperationException();
    }
}
