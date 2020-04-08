package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import groovy.lang.Tuple2;
import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyInterfaceImplementation;
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
        System.out.println("NUM INTERFACES WITH METHODS : " + interfaceMethodMap.size());

        // Verify the subjects
        List<ClassOrInterfaceDeclaration> subjects = map.get(Pattern.PROXY_SUBJECT);
        FeedbackWrapper<List<ProxyInterfaceImplementation>> subjectGroups =
            ProxyInterfaceImplementorVerifier.verifyImplementors(interfaceMethodMap, subjects);
        feedbacks.add(subjectGroups.getFeedback());

        // Verify the proxies
        List<ClassOrInterfaceDeclaration> proxies = map.get(Pattern.PROXY_PROXY);
        FeedbackWrapper<List<ProxyInterfaceImplementation>> proxyGroups =
            ProxyInterfaceImplementorVerifier.verifyImplementors(interfaceMethodMap, proxies);
        feedbacks.add(subjectGroups.getFeedback());

        // Merge and verify the parts.
        FeedbackWrapper<List<ProxyPatternGroup>> proxyPatterns = VerifyProxyParts.verifyParts(
            subjectGroups.getOther(), proxyGroups.getOther());
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

    /**
     * Verifies that all the proxies, interfaces and subjects are used and that if they are only a
     * part of an 'invalid' proxy pattern, those errors are returned.
     *
     * @param proxyGroups the finished proxy groups to check in.
     * @param proxies     the proxies to check for.
     * @param interfaces  the interfaces to check for.
     * @param subjects    the subjects to check for.
     *
     * @return a feedback on how they are used.
     */
    private Feedback allClassesAreUsed(
        List<ProxyPatternGroup> proxyGroups, List<ClassOrInterfaceDeclaration> proxies,
        List<ClassOrInterfaceDeclaration> interfaces, List<ClassOrInterfaceDeclaration> subjects) {

        Tuple2<List<ProxyPatternGroup>, List<ProxyPatternGroup>> newLists =
            splitIntoValidAndInvalid(proxyGroups);
        List<ProxyPatternGroup> valid = newLists.getFirst();
        List<ProxyPatternGroup> invalid = newLists.getSecond();

        // Remove the classes used in valid instances from the maps.
        valid.forEach(group -> {
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

        List<Feedback> feedbacks = new ArrayList<>();
        // Remove the classes used in the invalid instance from the maps if they are left and if
        // so also add their potentialErrors feedbacks to our feedbacks.
        invalid.forEach(group -> {
            // To make sure we don't add the same feedbacks more than once.
            boolean addedFeedback = false;

            ClassOrInterfaceDeclaration proxy = group.getProxy();
            if (proxies.contains(proxy)) {
                proxies.remove(proxy);
                addedFeedback = true;
                feedbacks.add(Feedback.getPatternInstanceFeedback(group.getPotentialErrors()));
            }

            ClassOrInterfaceDeclaration interfaceOrAClass = group.getInterfaceOrAClass();
            if (interfaces.contains(interfaceOrAClass)) {
                interfaces.remove(interfaceOrAClass);

                if (!addedFeedback) {
                    addedFeedback = true;
                    feedbacks.add(Feedback.getPatternInstanceFeedback(group.getPotentialErrors()));
                }
            }

            ClassOrInterfaceDeclaration subject = group.getSubject();
            if (subjects.contains(subject)) {
                subjects.remove(subject);

                if (!addedFeedback) {
                    feedbacks.add(Feedback.getPatternInstanceFeedback(group.getPotentialErrors()));
                }
            }
        });

        // Now add feedback for those classes that are still not used.
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

        feedbacks.add(Feedback.getPatternInstanceFeedback(unusedProxies));
        feedbacks.add(Feedback.getPatternInstanceFeedback(unusedInterfaces));
        feedbacks.add(Feedback.getPatternInstanceFeedback(unusedSubjects));
        return Feedback.getPatternInstanceFeedback(feedbacks);
    }

    /**
     * Splits the given patternGroups into those marked as valid and those marked as invalid.
     *
     * @param patternGroups the list of patternGroups to split.
     *
     * @return a tuple with first a list of valid ProxyPatternGroups and then a list of invalid
     *     ProxyPatternGroups.
     */
    private Tuple2<List<ProxyPatternGroup>, List<ProxyPatternGroup>> splitIntoValidAndInvalid(
        List<ProxyPatternGroup> patternGroups) {
        List<ProxyPatternGroup> valid = new ArrayList<>();
        List<ProxyPatternGroup> invalid = new ArrayList<>();

        for (ProxyPatternGroup group : patternGroups) {
            if (group.isValid()) {
                valid.add(group);
            } else {
                invalid.add(group);
            }
        }
        return new Tuple2<>(valid, invalid);
    }
}