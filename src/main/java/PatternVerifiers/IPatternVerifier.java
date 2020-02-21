package PatternVerifiers;

import com.github.javaparser.ast.CompilationUnit;

public interface IPatternVerifier {

    boolean verify(CompilationUnit cu);
}
