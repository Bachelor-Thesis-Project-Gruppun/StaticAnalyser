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
import tool.feedback.FeedbackWrapper;

/**
 * A class responsible for verifying a Proxy pattern Subject class.
 */
public class ProxySubjectVerifier {

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
    static FeedbackWrapper<List<ProxyPatternGroup>> verifySubjects(
        Map<ClassOrInterfaceDeclaration, List<MethodDeclaration>> interfaceMethods,
        List<ClassOrInterfaceDeclaration> subjects) {

        List<Feedback> groupingFeedbacks = new ArrayList<>();
        List<ProxyPatternGroup> proxyPatterns = new ArrayList<>();

        for (ClassOrInterfaceDeclaration subject : subjects) {
            for (ClassOrInterfaceDeclaration interfaceOrAClass : interfaceMethods.keySet()) {
                List<MethodGroup> methodGroups = new ArrayList<>();
                for (MethodDeclaration method : interfaceMethods.get(interfaceOrAClass)) {
                    FeedbackWrapper<MethodDeclaration> result = classImplementsMethod(subject,
                                                                                      interfaceOrAClass,
                                                                                      method);

                    groupingFeedbacks.add(result.getFeedback());

                    if (result.getOther() != null) {
                        methodGroups.add(new MethodGroup(method, result.getOther()));
                    }
                }

                if (!methodGroups.isEmpty()) {
                    // The subject and interface are now Grouped.
                    proxyPatterns.add(ProxyPatternGroup
                                          .getInterfaceSubjectGroup(interfaceOrAClass, subject,
                                                                    methodGroups));
                }
            }
        }

        return new FeedbackWrapper<>(Feedback.getPatternInstanceFeedback(groupingFeedbacks),
                                     proxyPatterns);
    }
}
