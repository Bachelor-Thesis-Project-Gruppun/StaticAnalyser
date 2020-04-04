package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import tool.designpatterns.verifiers.multiclassverifiers.proxy.visitors.MethodDeclarationVisitor;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.FeedbackWrapper;

/**
 * A class containing useful helper methods when verifying methods.
 */
public final class MethodVerification {

    private MethodVerification() {

    }

    /**
     * A method to find out if both the interface and the class implement the method.
     *
     * @param theClass     The class to find a matching MethodDeclaration in.
     * @param theInterface The interface to find a matching MethodDeclaration in.
     * @param method       The MethodDeclaration to be found in the class and interface.
     *
     * @return A feedbackwrapper containing a feedback and the class MethodDeclaration object.
     */
    static FeedbackWrapper<MethodDeclaration> classImplementsMethod(
        ClassOrInterfaceDeclaration theClass, ClassOrInterfaceDeclaration theInterface,
        MethodDeclaration method) {

        // Check if the class implements the interface / abstract class.
        if (classImplementsInterface(theClass, theInterface)) {
            return new FeedbackWrapper<>(Feedback.getNoChildFeedback(
                "Class '" + theClass.getNameAsString() + "' does not implement interface '" +
                theInterface.getNameAsString() + "'", new FeedbackTrace(theClass)), null);
        }

        MethodDeclarationVisitor mdv = new MethodDeclarationVisitor();
        List<MethodDeclaration> implementedMethods = theClass.accept(mdv, theClass);
        List<MethodDeclaration> interfaceMethods = theInterface.accept(mdv, theInterface);
        boolean isInClass = false;
        boolean isInInterface = false;
        MethodDeclaration classDeclaration = new MethodDeclaration();

        if (implementedMethods.isEmpty()) {
            String message = "There are no method declarations in the class";
            return new FeedbackWrapper<>(
                Feedback.getNoChildFeedback(message, new FeedbackTrace(theInterface)), null);
        } else if (interfaceMethods.isEmpty()) {
            String message = "There are no method declarations in the interface";
            return new FeedbackWrapper<>(
                Feedback.getNoChildFeedback(message, new FeedbackTrace(theInterface)), null);
        } else if (method == null) {
            String message = "No such method";
            return new FeedbackWrapper<>(
                Feedback.getNoChildFeedback(message, new FeedbackTrace(theInterface)), null);
        }

        for (MethodDeclaration md : implementedMethods) {
            if (md.getNameAsString().equalsIgnoreCase(method.getNameAsString())) {
                classDeclaration = md;
                isInClass = true;
                break;
            }
        }

        for (MethodDeclaration md : interfaceMethods) {
            if (md.getNameAsString().equalsIgnoreCase(method.getNameAsString())) {
                isInInterface = true;
                break;
            }
        }

        if (isInClass && isInInterface) {
            return new FeedbackWrapper<>(Feedback.getSuccessfulFeedback(), classDeclaration);
        }

        String message = "The method is not implemented in the class or the interface";
        return new FeedbackWrapper<>(
            Feedback.getNoChildFeedback(message, new FeedbackTrace(theInterface)), null);
    }

    /**
     * Verifies that the given class implements the given interface (or abstract class).
     *
     * @param theClass     the class to check.
     * @param theInterface the interface to check for.
     *
     * @return true if the class implements the interface, otherwise false.
     */
    private static boolean classImplementsInterface(
        ClassOrInterfaceDeclaration theClass, ClassOrInterfaceDeclaration theInterface) {
        ResolvedReferenceTypeDeclaration resolvedInterfaceDec = theInterface.resolve();
        for (ClassOrInterfaceType interf : theClass.getImplementedTypes()) {
            ResolvedReferenceType resolvedInterface = interf.resolve();
            if (resolvedInterface.getTypeDeclaration().equals(resolvedInterfaceDec)) {
                return true;
            }
        }

        // The class does not implement the interface but it could extend it as an abstract class.
        if (theInterface.isAbstract()) {
            for (ClassOrInterfaceType extended : theClass.getExtendedTypes()) {
                if (extended.resolve().getTypeDeclaration().equals(resolvedInterfaceDec)) {
                    return true;
                }
            }
        }

        return false;
    }
}
