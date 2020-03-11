package base.designpatterns.verifiers.singleclassverifiers;

import com.github.javaparser.ast.CompilationUnit;

import base.util.Feedback;

/**
 * An interface that defines the obligatory behaviour that all pattern verifiers must implement.
 */
public interface IPatternVerifier {

    Feedback verify(CompilationUnit compUnit);
}
