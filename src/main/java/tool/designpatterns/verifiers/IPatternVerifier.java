package tool.designpatterns.verifiers;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.feedback.Feedback;

/**
 * An interface that defines the obligatory behaviour that all pattern verifiers must implement.
 */
public interface IPatternVerifier {

    Feedback verify(ClassOrInterfaceDeclaration compUnit);
}
