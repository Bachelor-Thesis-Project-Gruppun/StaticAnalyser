package patternverifiers;

import com.github.javaparser.ast.CompilationUnit;

public class SingletonVerifier implements IPatternVerifier {

    public SingletonVerifier() {
    }

    @Override
    public boolean verify(CompilationUnit cu) {
        throw new UnsupportedOperationException();
    }
}
