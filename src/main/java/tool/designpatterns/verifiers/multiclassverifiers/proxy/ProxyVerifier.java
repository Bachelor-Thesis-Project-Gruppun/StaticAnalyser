package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class ProxyVerifier implements IPatternGrouper {

    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {

        List<Feedback> feedbacks = new ArrayList<>();
        List<Feedback> interfaceFeedbacks = new ArrayList<>();
        Map<ClassOrInterfaceDeclaration, List<MethodDeclaration>> interfaceMethodMap =
            new HashMap<>();

        // Verify the interfaces.
        List<ClassOrInterfaceDeclaration> interfaces = map.get(Pattern.PROXY_INTERFACE);
        interfaces.forEach(interfaceOrAClass -> {
            Tuple2<Feedback, List<MethodDeclaration>> interfaceMethods = getValidMethods(
                interfaceOrAClass);
            interfaceFeedbacks.add(interfaceMethods.getFirst());
            interfaceMethodMap.put(interfaceOrAClass, interfaceMethods.getSecond());
        });
        feedbacks.add(Feedback.getPatternInstanceFeedback(interfaceFeedbacks));

        // Verify the subjects
        List<ClassOrInterfaceDeclaration> subjects = map.get(Pattern.PROXY_SUBJECT);
        FeedbackWrapper<List<ProxyPatternGroup>> interfaceSubjects = verifySubjects(
            interfaceMethodMap, subjects);
        feedbacks.add(interfaceSubjects.getFeedback());

        // Verify the Proxies.
        List<ClassOrInterfaceDeclaration> proxies = map.get(Pattern.PROXY_PROXY);
        FeedbackWrapper<List<ProxyPatternGroup>> interfaceSubjectProxies = verifyProxies(
            interfaceSubjects.getOther(), proxies);
        feedbacks.add(interfaceSubjectProxies.getFeedback());

        // Validate that all classes marked as parts of a Proxy pattern are used at least once.
        feedbacks.add(
            allClassesAreUsed(interfaceSubjectProxies.getOther(), proxies, interfaces, subjects));

        return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
    }

    private Tuple2<Feedback, List<MethodDeclaration>> getValidMethods(
        ClassOrInterfaceDeclaration classOrI) {

        MethodDeclarationVisitor mdv = new MethodDeclarationVisitor();
        List<MethodDeclaration> methodDeclarations = classOrI.accept(mdv, classOrI);
        List<MethodDeclaration> validMethodDeclarations = new ArrayList<>();

        if (methodDeclarations.isEmpty()) {
            String message = "There are no methods to implement.";
            return new Tuple2<>(
                Feedback.getNoChildFeedback(message, new FeedbackTrace(classOrI)),
                methodDeclarations);
        } else if (classOrI.isInterface()) {
            validMethodDeclarations.addAll(methodDeclarations);
            String message = "";
        } else {
            for (MethodDeclaration md : classOrI.accept(mdv, classOrI)) {
                if (md.isAbstract()) {
                    validMethodDeclarations.add(md);
                }
            }
            if (validMethodDeclarations.isEmpty()) {
                String message = "There are no abstract methods to implement.";
                return new Tuple2<>(
                    Feedback.getNoChildFeedback(message, new FeedbackTrace(classOrI)),
                    methodDeclarations);
            }

        }

        return new Tuple2<>(Feedback.getSuccessfulFeedback(), validMethodDeclarations);
    }

    private Feedback allClassesAreUsed(
        List<ProxyPatternGroup> proxyGroups, List<ClassOrInterfaceDeclaration> proxies,
        List<ClassOrInterfaceDeclaration> interfaces, List<ClassOrInterfaceDeclaration> subjects) {

        proxyGroups.forEach(group -> {
            ClassOrInterfaceDeclaration proxy = group.getProxy();
            if (!proxies.contains(proxy)) {
                throw new IllegalArgumentException(
                    "Proxygroup contains proxy not in proxies list " + proxyGroups.toString() +
                    " -- " + proxy.toString());
            }
            proxies.remove(proxy);
            ClassOrInterfaceDeclaration interfaceOrAClass = group.getInterfaceOrAClass();
            if (!interfaces.contains(interfaceOrAClass)) {
                throw new IllegalArgumentException("Proxygroup contains interface (or abstract " +
                                                   "class) not in interfaces list " +
                                                   proxyGroups.toString() + " -- " +
                                                   interfaceOrAClass.toString());
            }
            interfaces.remove(interfaceOrAClass);
            ClassOrInterfaceDeclaration subject = group.getSubject();
            if (!subjects.contains(subject)) {
                throw new IllegalArgumentException(
                    "Proxygroup contains subject not in interfaces list " + proxyGroups.toString() +
                    " -- " + subject.toString());
            }
            subject.remove(interfaceOrAClass);
        });

        List<Feedback> unusedProxyFeedbacks = new ArrayList<>();
        if (!proxies.isEmpty()) {
            proxies.forEach(proxy -> {
                unusedProxyFeedbacks.add(Feedback.getNoChildFeedback(
                    proxy.getNameAsString() + " is marked as proxy class but no accompanying " +
                    "interface or subject could be found", new FeedbackTrace(proxy)));
            });
        }

        List<Feedback> unusedInterfaceFeedbacks = new ArrayList<>();
        if (!interfaces.isEmpty()) {
            interfaces.forEach(interf -> {
                unusedInterfaceFeedbacks.add(Feedback.getNoChildFeedback(
                    interf.getNameAsString() + " is marked as proxy interface but no accompanying" +
                    " proxy class or subject could be found", new FeedbackTrace(interf)));
            });
        }

        List<Feedback> unusedSubjectFeedbacks = new ArrayList<>();
        if (!subjects.isEmpty()) {
            proxies.forEach(subject -> {
                unusedSubjectFeedbacks.add(Feedback.getNoChildFeedback(
                    subject.getNameAsString() + " is marked as proxy subject but no accompanying " +
                    "interface or proxy class could be found", new FeedbackTrace(subject)));
            });
        }

        List<Feedback> feedbacks = new ArrayList<>();
        feedbacks.add(Feedback.getPatternInstanceFeedback(unusedProxyFeedbacks));
        feedbacks.add(Feedback.getPatternInstanceFeedback(unusedInterfaceFeedbacks));
        feedbacks.add(Feedback.getPatternInstanceFeedback(unusedSubjectFeedbacks));
        return Feedback.getPatternInstanceFeedback(feedbacks);
    }
}
