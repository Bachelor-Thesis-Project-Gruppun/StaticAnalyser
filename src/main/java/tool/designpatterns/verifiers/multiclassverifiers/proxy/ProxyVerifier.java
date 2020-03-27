package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import groovy.lang.Tuple2;
import org.apache.commons.lang.NotImplementedException;
import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.helpers.InterfaceSubjectProxyTuple;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.helpers.InterfaceSubjectTuple;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.FeedbackWrapper;
import tool.feedback.PatternGroupFeedback;

public class ProxyVerifier implements IPatternGrouper {

    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {

        List<Feedback> feedbacks = new ArrayList<>();
        List<Feedback> interfaceFeedbacks = new ArrayList<>();
        Map<ClassOrInterfaceDeclaration, List<MethodDeclaration>> interfaceMethodMap =
            new HashMap<>();

        // Verify the interfaces.
        map.get(Pattern.PROXY_INTERFACE).forEach(interfaceOrAClass -> {
            Tuple2<Feedback, List<MethodDeclaration>> interfaceMethods = getValidMethods(
                interfaceOrAClass);
            interfaceFeedbacks.add(interfaceMethods.getFirst());
            interfaceMethodMap.put(interfaceOrAClass, interfaceMethods.getSecond());
        });

        // Verify the subjects
        List<ClassOrInterfaceDeclaration> subjects = map.get(Pattern.PROXY_SUBJECT);
        FeedbackWrapper<List<InterfaceSubjectTuple>> interfaceSubjects = verifySubjects(
            interfaceMethodMap, subjects);

        // Verify the proxys
        List<ClassOrInterfaceDeclaration> proxys = map.get(Pattern.PROXY_PROXY);
        Feedback proxyFeedback = verifyProxys(proxys, interfaceSubjects.getOther());

        // 1. Gå igenom alla "Pattern.PROXY_INTERFACE":
        //    1b. hämta giltiga metoder.
        // 1c. Kolla så att alla interfaces används.
        // 2. För alla giltiga metoder:
        //    2b. Gå igenom alla "Pattern.PROXY_SUBJECT":
        //        2c. Kolla att den implementerar metoden (/ inhertetar från interfacet).
        //        2d. Gå igenom alla Pattern.PROXY_PROXY"
        //            2e. Kolla att den också har metoden (/ inhertetar från interfacet).
        //            2f. Kolla att den har en privat variable av typ SUBJECT.
        //            2g. Kolla att SUBJECTs metod kallas i metoden.
        //
        // 3. Kolla så att alla subjects har en interface och är valid.
        // 3b. Kolla så att alla proxys har en interface och en subject och är valid.

        return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
    }

    // Steps 1 / 1b -- nemo
    private Tuple2<Feedback, List<MethodDeclaration>> getValidMethods(
        ClassOrInterfaceDeclaration classOrI) {

        throw new NotImplementedException();
    }

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
    private FeedbackWrapper<List<InterfaceSubjectTuple>> verifySubjects(
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
    private Feedback verifyProxys(
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
    private FeedbackWrapper<List<InterfaceSubjectProxyTuple>> addProxiesToGroup(

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
    private Feedback validateProxysAreUsed(
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
    private Feedback validateProxysHasVariables(
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

    private FeedbackWrapper<VariableDeclarator> validateProxyHasVariable(
        ClassOrInterfaceDeclaration proxy, ClassOrInterfaceDeclaration subject) {

        throw new NotImplementedException();
    }

    // Steps 2c / 2e -- nemo
    private FeedbackWrapper<MethodDeclaration> classImplementsMethod(
        ClassOrInterfaceDeclaration theClass, ClassOrInterfaceDeclaration theInterface,
        MethodDeclaration method) {
        throw new NotImplementedException();
    }
}
