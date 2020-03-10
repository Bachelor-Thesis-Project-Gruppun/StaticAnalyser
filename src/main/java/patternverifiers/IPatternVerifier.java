package patternverifiers;

import com.github.javaparser.ast.CompilationUnit;

/**
 * An interface that defines the obligatory behaviour that all pattern verifiers must implement.
 */
public interface IPatternVerifier {

    Feedback verify(CompilationUnit compUnit);
}
