package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static tool.designpatterns.verifiers.multiclassverifiers.proxy.ProxyProxyVerifier.verifyProxies;
import static tool.designpatterns.verifiers.multiclassverifiers.proxy.ProxySubjectVerifier.verifySubjects;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import groovy.lang.Tuple2;
import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyPatternGroup;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.visitors.MethodDeclarationVisitor;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.FeedbackWrapper;
import tool.feedback.PatternGroupFeedback;

/**
 * Verifies proxy pattern groups in the given map.
 */
public class ProxyVerifier implements IPatternGrouper {

    public ProxyVerifier() {

    }

    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {

        List<Feedback> feedbacks = new ArrayList<>();

        // Verify the interfaces.
        List<ClassOrInterfaceDeclaration> interfaces = map.get(Pattern.PROXY_INTERFACE);
        if (interfaces == null || interfaces.isEmpty()) {
            feedbacks.add(Feedback.getPatternInstanceNoChildFeedback(
                "Unable to find any " + Pattern.PROXY_INTERFACE.toString() + "annotations"));
            return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
        }

        List<Feedback> interfaceFeedbacks = new ArrayList<>();
        Map<ClassOrInterfaceDeclaration, List<MethodDeclaration>> interfaceMethodMap =
            new ConcurrentHashMap<>();

        interfaces.forEach(interfaceOrAClass -> {
            Tuple2<Feedback, List<MethodDeclaration>> interfaceMethods = getValidMethods(
                interfaceOrAClass);
            interfaceFeedbacks.add(interfaceMethods.getFirst());
            interfaceMethodMap.put(interfaceOrAClass, interfaceMethods.getSecond());
        });
        feedbacks.add(Feedback.getPatternInstanceFeedback(interfaceFeedbacks));

        // Verify the subjects
        List<ClassOrInterfaceDeclaration> subjects = map.get(Pattern.PROXY_SUBJECT);
        if (subjects == null || subjects.isEmpty()) {
            feedbacks.add(Feedback.getPatternInstanceNoChildFeedback(
                "Unable to find any " + Pattern.PROXY_SUBJECT.toString() + "annotations"));
            return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
        }
        FeedbackWrapper<List<ProxyPatternGroup>> interfaceSubjects = verifySubjects(
            interfaceMethodMap, subjects);
        feedbacks.add(interfaceSubjects.getFeedback());

        // Verify the Proxies.
        List<ClassOrInterfaceDeclaration> proxies = map.get(Pattern.PROXY_PROXY);
        if (proxies == null || proxies.isEmpty()) {
            feedbacks.add(Feedback.getPatternInstanceNoChildFeedback(
                "Unable to find any " + Pattern.PROXY_PROXY.toString() + "annotations"));
            return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
        }
        FeedbackWrapper<List<ProxyPatternGroup>> proxyPatterns = verifyProxies(
            interfaceSubjects.getOther(), proxies);
        feedbacks.add(proxyPatterns.getFeedback());

        // Validate that all classes marked as parts of a Proxy pattern are used at least once.
        feedbacks.add(allClassesAreUsed(proxyPatterns.getOther(), proxies, interfaces, subjects));

        return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
    }

    private Tuple2<Feedback, List<MethodDeclaration>> getValidMethods(
        ClassOrInterfaceDeclaration classOrI) {

        MethodDeclarationVisitor mdv = new MethodDeclarationVisitor();
        List<MethodDeclaration> methodDeclarations = classOrI.accept(mdv, classOrI);
        List<MethodDeclaration> validMethodDecs = new ArrayList<>();

        if (methodDeclarations.isEmpty()) {
            String message = "There are no methods to implement.";
            return new Tuple2<>(
                Feedback.getNoChildFeedback(message, new FeedbackTrace(classOrI)),
                methodDeclarations);
        } else if (classOrI.isInterface()) {
            validMethodDecs.addAll(methodDeclarations);
        } else {
            for (MethodDeclaration md : classOrI.accept(mdv, classOrI)) {
                if (md.isAbstract()) {
                    validMethodDecs.add(md);
                }
            }
            if (validMethodDecs.isEmpty()) {
                String message = "There are no abstract methods to implement.";
                return new Tuple2<>(
                    Feedback.getNoChildFeedback(message, new FeedbackTrace(classOrI)),
                    methodDeclarations);
            }

        }

        return new Tuple2<>(Feedback.getSuccessfulFeedback(), validMethodDecs);
    }

    private Feedback allClassesAreUsed(
        List<ProxyPatternGroup> proxyGroups, List<ClassOrInterfaceDeclaration> proxies,
        List<ClassOrInterfaceDeclaration> interfaces, List<ClassOrInterfaceDeclaration> subjects) {

        proxyGroups.forEach(group -> {
            ClassOrInterfaceDeclaration proxy = group.getProxy();
            if (proxies.contains(proxy)) {
                proxies.remove(proxy);
            }

            ClassOrInterfaceDeclaration interfaceOrAClass = group.getInterfaceOrAClass();
            if (interfaces.contains(interfaceOrAClass)) {
                interfaces.remove(interfaceOrAClass);
            }

            ClassOrInterfaceDeclaration subject = group.getSubject();
            if (subjects.contains(subject)) {
                subjects.remove(subject);
            }
        });

        List<Feedback> unusedProxies = new ArrayList<>();
        if (!proxies.isEmpty()) {
            proxies.forEach(proxy -> {
                unusedProxies.add(Feedback.getNoChildFeedback(
                    proxy.getNameAsString() + " is marked as proxy class but no accompanying " +
                    "interface or subject could be found", new FeedbackTrace(proxy)));
            });
        }

        List<Feedback> unusedInterfaces = new ArrayList<>();
        if (!interfaces.isEmpty()) {
            interfaces.forEach(interf -> {
                unusedInterfaces.add(Feedback.getNoChildFeedback(
                    interf.getNameAsString() + " is marked as proxy interface but no accompanying" +
                    " proxy class or subject could be found", new FeedbackTrace(interf)));
            });
        }

        List<Feedback> unusedSubjects = new ArrayList<>();
        if (!subjects.isEmpty()) {
            proxies.forEach(subject -> {
                unusedSubjects.add(Feedback.getNoChildFeedback(
                    subject.getNameAsString() + " is marked as proxy subject but no accompanying " +
                    "interface or proxy class could be found", new FeedbackTrace(subject)));
            });
        }

        List<Feedback> feedbacks = new ArrayList<>();
        feedbacks.add(Feedback.getPatternInstanceFeedback(unusedProxies));
        feedbacks.add(Feedback.getPatternInstanceFeedback(unusedInterfaces));
        feedbacks.add(Feedback.getPatternInstanceFeedback(unusedSubjects));
        return Feedback.getPatternInstanceFeedback(feedbacks);
    }
}