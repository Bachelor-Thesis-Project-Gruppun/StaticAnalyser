package base;

import com.github.javaparser.ast.CompilationUnit;
import org.gradle.internal.impldep.org.apache.commons.lang.NotImplementedException;

public class SingletonVerifier implements IVerifier {
    @Override
    public boolean verify(CompilationUnit cu) {
        //throw new NotImplementedException();
        return true;
    }
}
