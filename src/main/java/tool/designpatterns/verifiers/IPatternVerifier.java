package tool.designpatterns.verifiers;

import com.github.javaparser.ast.CompilationUnit;

import tool.util.Feedback;

/**
 * An interface that defines the obligatory behaviour that all pattern verifiers must implement.
 */
public interface IPatternVerifier {

    Feedback verify(CompilationUnit compUnit);
}
