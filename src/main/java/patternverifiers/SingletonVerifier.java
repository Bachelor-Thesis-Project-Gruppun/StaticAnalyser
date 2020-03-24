package patternverifiers;

import com.github.javaparser.ast.CompilationUnit;

/**
 * A verifier for the singleton pattern.
 */
public class SingletonVerifier implements IPatternVerifier {

    public SingletonVerifier() {
    }

    @Override
    public Feedback verify(CompilationUnit compUnit) {
        throw new UnsupportedOperationException();
    }
}
