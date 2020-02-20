package base;

import com.github.javaparser.ast.CompilationUnit;
import org.gradle.internal.impldep.org.apache.commons.lang.NotImplementedException;

public interface IVerifier {
    boolean verify(CompilationUnit cu);
}
