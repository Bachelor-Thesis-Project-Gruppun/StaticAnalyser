package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;

import static tool.designpatterns.verifiers.multiclassverifiers.proxy.MethodVerification.classImplementsMethod;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import org.apache.commons.lang.NotImplementedException;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.tuplehelpers.InterfaceSubjectProxyTuple;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.tuplehelpers.InterfaceSubjectTuple;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.FeedbackWrapper;

/**
 * Class responsible for verifying a Proxy pattern Proxy class
 */
public class ProxyProxyVerifier {

    // 2d / 2f / 2g (also 2e but uses ClassImplementsMethod) -- vidde

    /**
     * Takes a list of proxys and a list of InterfaceSubjectTuples, matches them together and
     * verifies that all proxies and interfaceSubjects are used.
     *
     * @param proxys            the proxies to verify.
     * @param interfaceSubjects the InterfaceSubjectTuples to verify.
     *
     * @return Feedback on the verification.
     */
    static Feedback verifyProxys(
        List<ClassOrInterfaceDeclaration> proxys, List<InterfaceSubjectTuple> interfaceSubjects) {

        List<Feedback> feedbacks = new ArrayList<>();

        // Find out which proxies belongs to which interfaceSubjects (note we allow multiple
        // classes to be proxys for the same interfaceSubjects).
        FeedbackWrapper<List<InterfaceSubjectProxyTuple>> interfaceSubjectProxyGroups =
            addProxiesToGroup(proxys, interfaceSubjects);
        feedbacks.add(interfaceSubjectProxyGroups.getFeedback());

        // Make sure that all proxy classes were used.
        Feedback unusedProxyFeedback = validateProxysAreUsed(
            proxys, interfaceSubjectProxyGroups.getOther());
        feedbacks.add(unusedProxyFeedback);

        // Validate that the proxy has a variable that holds the subject.
        Feedback proxysHasVariablesFeedback = validateProxysHasVariables(
            interfaceSubjectProxyGroups.getOther());

        // Validate that the proxys method calls the subjects method.
        // FeedbackWrapper<List<ProxyMethodGroup>> methodGroups = filterToMethods();

        throw new NotImplementedException();
    }

    /**
     * Finds a proxy class for each interfaceSubject and creates a InterfaceSubjectProxyGroup from
     * all of them.
     *
     * @param proxys            the proxies to check.
     * @param interfaceSubjects the interfaceSubjecs to check.
     *
     * @return the new interfaceSubjectProxyGroups and feedback.
     */
    private static FeedbackWrapper<List<InterfaceSubjectProxyTuple>> addProxiesToGroup(

        List<ClassOrInterfaceDeclaration> proxys, List<InterfaceSubjectTuple> interfaceSubjects) {
        List<Feedback> feedbacks = new ArrayList<>();

        List<InterfaceSubjectProxyTuple> interfaceSubjectProxyGroups = new ArrayList<>();
        for (ClassOrInterfaceDeclaration proxy : proxys) {
            for (InterfaceSubjectTuple interfaceSubject : interfaceSubjects) {
                FeedbackWrapper<MethodDeclaration> proxyMethod = classImplementsMethod(proxy,
                                                                                       interfaceSubject
                                                                                           .getInterfaceOrAClass(),
                                                                                       interfaceSubject
                                                                                           .getInterfaceMethod());

                feedbacks.add(proxyMethod.getFeedback());

                if (proxyMethod.getOther() != null) {
                    interfaceSubjectProxyGroups.add(
                        new InterfaceSubjectProxyTuple(interfaceSubject, proxy,
                                                       proxyMethod.getOther()));
                }
            }
        }

        return new FeedbackWrapper<>(
            Feedback.getPatternInstanceFeedback(feedbacks), interfaceSubjectProxyGroups);
    }

    /**
     * Validate that all the given proxy classes are in a InterfaceSubjectProxyTuple.
     *
     * @param proxys                      the proxys to check.
     * @param interfaceSubjectProxyGroups the InterfaceSubjectProxyTuples to check.
     *
     * @return a list of Feedbacks on the result.
     */
    private static Feedback validateProxysAreUsed(
        List<ClassOrInterfaceDeclaration> proxys,
        List<InterfaceSubjectProxyTuple> interfaceSubjectProxyGroups) {
        List<Feedback> feedbacks = new ArrayList<>();

        for (ClassOrInterfaceDeclaration proxy : proxys) {
            boolean proxyIsUsed = false;
            for (InterfaceSubjectProxyTuple interfaceSubjectProxy : interfaceSubjectProxyGroups) {
                if (proxy.equals(interfaceSubjectProxy.getProxy())) {
                    proxyIsUsed = true;
                }
            }

            if (!proxyIsUsed) {
                feedbacks.add(Feedback.getNoChildFeedback(
                    "Proxy class appears to not to have a matching proxy subject and proxy " +
                    "subject.))", new FeedbackTrace(proxy)));
            }
        }

        return Feedback.getPatternInstanceFeedback(feedbacks);
    }

    /**
     * Validates that all of the proxys in the given interfaceSubjectProxyGroups, hold a variable
     * with the type of the subject.
     * <p>
     * Query: Do we care about if the variable is assigned to or not? Answer: Probably not as we
     * will later check if the variable is used(?).
     *
     * @param interfaceSubjectProxyGroups
     *
     * @return
     */
    private static Feedback validateProxysHasVariables(
        List<InterfaceSubjectProxyTuple> interfaceSubjectProxyGroups) {
        List<Feedback> feedbacks = new ArrayList<>();

        for (InterfaceSubjectProxyTuple interfaceSubjectProxy : interfaceSubjectProxyGroups) {
            FeedbackWrapper<VariableDeclarator> proxyVariable = validateProxyHasVariable(
                interfaceSubjectProxy.getProxy(),
                interfaceSubjectProxy.getInterfaceSubject().getSubject());
            feedbacks.add(proxyVariable.getFeedback());

        }

        return Feedback.getPatternInstanceFeedback(feedbacks);
    }

    private static FeedbackWrapper<VariableDeclarator> validateProxyHasVariable(
        ClassOrInterfaceDeclaration proxy, ClassOrInterfaceDeclaration subject) {

        throw new NotImplementedException();
    }

}
