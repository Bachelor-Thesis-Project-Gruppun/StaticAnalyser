package base;

import org.gradle.internal.impldep.org.apache.commons.lang.NotImplementedException;

import com.github.javaparser.ast.CompilationUnit;

public class SingletonVerifier implements IVerifier {

    @Override
    public boolean verify(CompilationUnit cu) {
        throw new NotImplementedException();
    }
}
