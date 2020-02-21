package patternverifiers;

import org.gradle.internal.impldep.org.apache.commons.lang.NotImplementedException;

import com.github.javaparser.ast.CompilationUnit;

public class SingletonVerifier implements IPatternVerifier {

    public SingletonVerifier() {
    }

    @Override
    public boolean verify(CompilationUnit cu) {
        throw new NotImplementedException();
    }
}
