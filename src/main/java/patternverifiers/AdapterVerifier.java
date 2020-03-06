package patternverifiers;

import com.github.javaparser.ast.CompilationUnit;

/**
 * A verifier for the adapter pattern.
 */
public class AdapterVerifier implements IPatternVerifier{

    public AdapterVerifier() {
    }

    @Override
    public boolean verify(CompilationUnit compUnit) {
        throw new UnsupportedOperationException();
    }

}
