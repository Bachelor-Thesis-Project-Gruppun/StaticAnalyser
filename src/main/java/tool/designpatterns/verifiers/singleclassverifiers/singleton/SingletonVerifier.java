package tool.designpatterns.verifiers.singleclassverifiers.singleton;

import com.github.javaparser.ast.CompilationUnit;

import tool.designpatterns.verifiers.IPatternVerifier;
import tool.feedback.Feedback;

/**
 * A verifier for the singleton pattern.
 */
public class SingletonVerifier implements IPatternVerifier {

    public SingletonVerifier() {
    }

    @Override
    public Feedback verify(CompilationUnit compUnit) {
        throw new UnsupportedOperationException();
    }
}
