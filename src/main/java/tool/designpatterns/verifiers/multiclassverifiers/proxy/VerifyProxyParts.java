package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import jdk.jshell.spi.ExecutionControl;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.MethodGroup;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.MethodGroupPart;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyInterfaceImplementation;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyPatternGroup;
import tool.feedback.Feedback;
import tool.feedback.FeedbackWrapper;

/**
 * A class to verify and group parts or proxy patterns (ProxyInterfaceImplmenetations)
 */
public class VerifyProxyParts {

    /**
     * Verifies and groups together the given subjects with the given proxies.
     *
     * @param subjects the subjects to verify and group.
     * @param proxies  the proxies to verify and group.
     *
     * @return A list of complete ProxyPatternGroups
     */
    public static FeedbackWrapper<List<ProxyPatternGroup>> verifyParts(
        List<ProxyInterfaceImplementation> subjects, List<ProxyInterfaceImplementation> proxies) {

        List<Feedback> feedbacks = new ArrayList<>();

        for (ProxyInterfaceImplementation subject : subjects) {
            for (ProxyInterfaceImplementation proxy : proxies) {

            }
        }
    }

    /**
     * Tries to group the given proxy with the given subject.
     *
     * @param subject the subject to check for.
     * @param proxy   the proxy to check for.
     *
     * @return The result of the verification / grouping and a ProxyPatternGroup should they be
     *     compatible or null otherwise.
     */
    private static FeedbackWrapper<ProxyPatternGroup> tryGroup(
        ProxyInterfaceImplementation subject, ProxyInterfaceImplementation proxy) {

        // Compare if they have the same interfaces.
        boolean sameInterface = isSameClassOrInterface(subject.getInterfaceOrAClass(),
            proxy.getInterfaceOrAClass());
        if (!sameInterface) {
            // Not the same interface, definantely not the same proxy pattern.
            return new FeedbackWrapper<>(Feedback.getSuccessfulFeedback(), null);
        }

        // They implement the same interface, check if the proxy has a private variable of the
        // subjects type.
        boolean hasVariable = hasVariable(
            subject.getInterfaceImplementor(), proxy.getInterfaceImplementor());
        if (!hasVariable) {
            // Does not have variable.
            return new FeedbackWrapper<>(Feedback.getSuccessfulFeedback(), null);
        }

        // Try to group the methods together.

        // Find the proxies methods that calls the subjects methods.
        boolean callsMethods = proxyCallsSubject(subject.getMethods(), proxy.getMethods());
    }

    /**
     * Compares if two ClassOrInterfaceImplementations are the same.
     *
     * @param a the first to check.
     * @param b the second to check.
     *
     * @return if a is the same as b.
     */
    private static boolean isSameClassOrInterface(
        ClassOrInterfaceDeclaration a, ClassOrInterfaceDeclaration b) {

        ResolvedReferenceTypeDeclaration subjectInterface = a.resolve();
        ResolvedReferenceTypeDeclaration proxyInterface = b.resolve();
        return subjectInterface.getQualifiedName().equals(proxyInterface.getQualifiedName());
    }

    /**
     * Returns if the proxy has a (private) variable of the subjects type.
     *
     * @param subject the type to check for.
     * @param proxy   the class to check in.
     *
     * @return true if the proxy has the variable and false otherwise.
     */
    private static boolean hasVariable(
        ClassOrInterfaceDeclaration subject, ClassOrInterfaceDeclaration proxy) {

        ResolvedReferenceTypeDeclaration resolvedRefTypeDec = subject.resolve();

        for (FieldDeclaration field : proxy.getFields()) {
            if (field.isPrivate()) {
                // Check if the field has the correct type.
                ResolvedReferenceType fieldType = field.resolve().getType().asReferenceType();

                if (fieldType.getQualifiedName().equals(resolvedRefTypeDec.getQualifiedName())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static List<MethodGroup> groupMethods() {
        
    }

    private static boolean proxyCallsSubject(
        List<MethodGroupPart> subjectMethods, List<MethodGroupPart> proxyMethods) {
        for (MethodGroupPart subjectMethod : subjectMethods) {
            for (MethodGroupPart proxyMethod : proxyMethods) {
                if (MethodVerification.methodsAreSame(subjectMethod.getInterfaceMethod(),
                    proxyMethod.getInterfaceMethod())) {
                    // These
                }
            }
        }

        throw new ExecutionControl.NotImplementedException();
    }
}
