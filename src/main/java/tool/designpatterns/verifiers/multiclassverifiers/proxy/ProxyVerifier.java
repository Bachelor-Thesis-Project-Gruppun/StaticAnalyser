package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import groovy.lang.Tuple2;
import org.apache.commons.lang.NotImplementedException;
import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
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

        map.get(Pattern.PROXY_INTERFACE).forEach(interfaceOrAClass -> {
            Tuple2<Feedback, List<MethodDeclaration>> interfaceMethods = getValidMethods(
                interfaceOrAClass);
            interfaceFeedbacks.add(interfaceMethods.getFirst());
            interfaceMethodMap.put(interfaceOrAClass, interfaceMethods.getSecond());
        });

        List<ClassOrInterfaceDeclaration> subjects = map.get(Pattern.PROXY_SUBJECT);
        FeedbackWrapper<List<InterfaceSubjectTuple>> interfaceSubjects = verifySubjects(
            interfaceMethodMap, subjects);

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
    private FeedbackWrapper<List<InterfaceSubjectTuple>> verifySubjects(
        Map<ClassOrInterfaceDeclaration, List<MethodDeclaration>> interfaceMethods,
        List<ClassOrInterfaceDeclaration> subjects) {

        // The groups of Interface, interfaceMethod and subjects that have been found.
        List<InterfaceSubjectTuple> groups = new ArrayList<>();
        interfaceMethods.keySet().forEach(interfaceOrAClass -> {
            interfaceMethods.get(interfaceOrAClass).forEach(method -> {
                subjects.forEach(subject -> {
                    MethodDeclaration subjectMethod = classImplementsMethod(
                        subject, interfaceOrAClass, method);

                    if (subjectMethod != null) {
                        groups.add(new InterfaceSubjectTuple(interfaceOrAClass, method, subject));
                    }
                });
            });
        });

        List<Feedback> unusedSubjectFeedbacks = new ArrayList<>();
        subjects.forEach(subject -> {
            AtomicBoolean isUsed = new AtomicBoolean(false);

            groups.forEach(group -> {
                if (group.subject.equals(subject)) {
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

        return new FeedbackWrapper<>(
            Feedback.getPatternInstanceFeedback(unusedSubjectFeedbacks), groups);
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

        throw new NotImplementedException();
    }

    /**
     * Simple tuple3 to group an interface, a subject and the interface method that belongs
     * together.
     */
    private class InterfaceSubjectTuple {

        private ClassOrInterfaceDeclaration interfaceOrAClass;
        private MethodDeclaration interfaceMethod;
        private ClassOrInterfaceDeclaration subject;

        InterfaceSubjectTuple(
            ClassOrInterfaceDeclaration interfaceOrAClass, MethodDeclaration interfaceMethod,
            ClassOrInterfaceDeclaration subject) {
            this.interfaceOrAClass = interfaceOrAClass;
            this.interfaceMethod = interfaceMethod;
            this.subject = subject;
        }
    }

    // Steps 2c / 2e -- nemo
    private MethodDeclaration classImplementsMethod(
        ClassOrInterfaceDeclaration theClass, ClassOrInterfaceDeclaration theInterface,
        MethodDeclaration method) {
        throw new NotImplementedException();
    }
}
