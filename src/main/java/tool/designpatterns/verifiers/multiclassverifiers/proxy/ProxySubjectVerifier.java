package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static tool.designpatterns.verifiers.multiclassverifiers.proxy.MethodVerification.classImplementsMethod;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import tool.designpatterns.verifiers.multiclassverifiers.proxy.tuplehelpers.InterfaceSubjectTuple;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.FeedbackWrapper;

/**
 * A class responsible for verifying a Proxy pattern Subject class.
 */
public class ProxySubjectVerifier {

    // Step 2 / 2b (also 2* but uses verifyProxy && classImplementsMethod for this) -- vidde

    /**
     * Method to validate that the given Proxy Subjects are valid and group them together with their
     * interfaces if possible.
     *
     * @param interfaceMethods a map containing ProxyInterfaces as the key and a list of their valid
     *                         proxy methods as values.
     * @param subjects         a list of all the proxy subjects.
     *
     * @return The feedback of the verification as well as a list of the interfaces,
     *     interfaceMethods, Subject and Subject methods grouped together.
     */
    static FeedbackWrapper<List<InterfaceSubjectTuple>> verifySubjects(
        Map<ClassOrInterfaceDeclaration, List<MethodDeclaration>> interfaceMethods,
        List<ClassOrInterfaceDeclaration> subjects) {

        // The groups of Interface, interfaceMethod and subjects that have been found.
        List<InterfaceSubjectTuple> interfaceSubjectGroups = new ArrayList<>();
        List<Feedback> interfaceFeedbacks = new ArrayList<>();

        interfaceMethods.keySet().forEach(interfaceOrAClass -> {
            interfaceMethods.get(interfaceOrAClass).forEach(method -> {
                AtomicBoolean interfaceIsUsed = new AtomicBoolean(false);
                subjects.forEach(subject -> {
                    FeedbackWrapper<MethodDeclaration> subjectMethod = classImplementsMethod(
                        subject, interfaceOrAClass, method);

                    if (subjectMethod != null) {
                        interfaceSubjectGroups.add(
                            new InterfaceSubjectTuple(interfaceOrAClass, method, subject,
                                                      subjectMethod.getOther()));
                        interfaceIsUsed.set(true);
                    }
                });

                // Validate that this interface is used.
                if (!interfaceIsUsed.get()) {
                    interfaceFeedbacks.add(Feedback.getNoChildFeedback(
                        "Interface appears to lack a proxy subject implementation",
                        new FeedbackTrace(interfaceOrAClass)));
                }
            });
        });

        List<Feedback> unusedSubjectFeedbacks = new ArrayList<>();
        subjects.forEach(subject -> {
            AtomicBoolean isUsed = new AtomicBoolean(false);

            interfaceSubjectGroups.forEach(group -> {
                if (group.getSubject().equals(subject)) {
                    isUsed.set(true);
                }
            });

            if (!isUsed.get()) {
                String message =
                    subject.getNameAsString() + " is not a valid Proxy Subject, it needs to " +
                    "implement a proxy interface method.";
                unusedSubjectFeedbacks.add(
                    Feedback.getNoChildFeedback(message, new FeedbackTrace(subject)));
            }
        });

        // Merge all subFeedbacks together.
        unusedSubjectFeedbacks.addAll(interfaceFeedbacks);

        return new FeedbackWrapper<>(
            Feedback.getPatternInstanceFeedback(unusedSubjectFeedbacks), interfaceSubjectGroups);
    }
}
