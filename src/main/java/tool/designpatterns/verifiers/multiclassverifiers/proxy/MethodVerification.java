package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import org.apache.commons.lang.NotImplementedException;
import tool.feedback.FeedbackWrapper;

/**
 * A class containing useful helper methods when verifying methods.
 */
public class MethodVerification {

    // Steps 2c / 2e -- nemo
    static FeedbackWrapper<MethodDeclaration> classImplementsMethod(
        ClassOrInterfaceDeclaration theClass, ClassOrInterfaceDeclaration theInterface,
        MethodDeclaration method) {
        throw new NotImplementedException();
    }

}
