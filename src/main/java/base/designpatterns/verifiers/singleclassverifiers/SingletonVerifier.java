package base.designpatterns.verifiers.singleclassverifiers;

import com.github.javaparser.ast.CompilationUnit;

import base.util.Feedback;

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
