package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static tool.designpatterns.verifiers.multiclassverifiers.proxy.MethodVerification.classImplementsMethod;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.MethodGroup;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyPatternGroup;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.FeedbackWrapper;

/**
 * A class responsible for verifying a Proxy pattern Subject class.
 */
public final class ProxySubjectVerifier {

    private ProxySubjectVerifier() {
    }

    /**
     * Method to validate that the given Proxy Subjects are valid and group them together with their
     * interfaces if possible.
     *
     * @param interfaceMethods a map containing ProxyInterfaces and valid proxy methods.
     * @param subjects         a list of all the proxy subjects.
     *
     * @return The feedback of the verification as well as a list of the interfaces,
     *     interfaceMethods, Subject and Subject methods grouped together.
     */
    public static FeedbackWrapper<List<ProxyPatternGroup>> verifySubjects(
        Map<ClassOrInterfaceDeclaration, List<MethodDeclaration>> interfaceMethods,
        List<ClassOrInterfaceDeclaration> subjects) {

        List<Feedback> feedbacks = new ArrayList<>();
        List<ProxyPatternGroup> proxyPatterns = new ArrayList<>();

        for (ClassOrInterfaceDeclaration subject : subjects) {
            for (ClassOrInterfaceDeclaration interfaceOrAClass : interfaceMethods.keySet()) {
                FeedbackWrapper<ProxyPatternGroup> proxyPattern = getProxyPattern(
                    interfaceMethods.get(interfaceOrAClass), interfaceOrAClass, subject);
                feedbacks.add(proxyPattern.getFeedback());
                if (proxyPattern.getOther() != null) {
                    proxyPatterns.add(proxyPattern.getOther());
                }
            }
        }

        return new FeedbackWrapper<>(Feedback.getPatternInstanceFeedback(feedbacks), proxyPatterns);
    }

    private static FeedbackWrapper<ProxyPatternGroup> getProxyPattern(
        List<MethodDeclaration> interfaceMethods, ClassOrInterfaceDeclaration interfaceOrAClass,
        ClassOrInterfaceDeclaration subject) {
        List<Feedback> feedbacks = new ArrayList<>();

        List<MethodGroup> methodGroups = new ArrayList<>();
        for (MethodDeclaration method : interfaceMethods) {
            FeedbackWrapper<MethodDeclaration> subjectMethod = classImplementsMethod(subject,
                interfaceOrAClass, method);

            feedbacks.add(subjectMethod.getFeedback());

            if (subjectMethod.getOther() != null) {
                methodGroups.add(MethodGroup.getWithoutProxy(method, subjectMethod.getOther()));
            }
        }

        if (!methodGroups.isEmpty()) {
            // The subject and interface are now Grouped.
            return new FeedbackWrapper<>(
                Feedback.getFeedbackWithChildren(new FeedbackTrace(subject), feedbacks),
                ProxyPatternGroup
                    .getInterfaceSubjectGroup(interfaceOrAClass, subject, methodGroups));
        }

        return new FeedbackWrapper<>(
            Feedback.getNoChildFeedback(
                "Unable to find a subject method that implements any of the interface methods for" +
                " interface '" + interfaceOrAClass.getNameAsString() + "'",
                new FeedbackTrace(subject)), null);
    }
}
