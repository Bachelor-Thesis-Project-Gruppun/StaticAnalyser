package base;

import com.github.javaparser.ast.CompilationUnit;

public interface IVerifier {

    boolean verify(CompilationUnit cu);
}
