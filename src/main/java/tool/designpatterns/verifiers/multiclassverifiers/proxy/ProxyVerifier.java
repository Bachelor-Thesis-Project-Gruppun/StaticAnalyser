package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import groovy.lang.Tuple2;
import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.InterfaceMethods;
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

        List<InterfaceMethods> interfaceMethodGroups = new ArrayList<>();
        interfaces.forEach(interfaceOrAClass -> {
            Tuple2<Feedback, List<MethodDeclaration>> interfaceMethods = getValidMethods(
                interfaceOrAClass);
            interfaceFeedbacks.add(interfaceMethods.getFirst());
            interfaceMethodGroups.add(
                new InterfaceMethods(interfaceOrAClass, interfaceMethods.getSecond()));
        });
        feedbacks.add(Feedback.getPatternInstanceFeedback(interfaceFeedbacks));

        // Verify the subjects
        List<ClassOrInterfaceDeclaration> subjects = map.get(Pattern.PROXY_SUBJECT);
        List<ProxyInterfaceImplementation> subjectGroups =
            ProxyInterfaceImplementorVerifier.verifyImplementors(interfaceMethodGroups, subjects);

        // Verify the proxies
        List<ClassOrInterfaceDeclaration> proxies = map.get(Pattern.PROXY_PROXY);
        List<ProxyInterfaceImplementation> proxyGroups =
            ProxyInterfaceImplementorVerifier.verifyImplementors(interfaceMethodGroups, proxies);

        // Merge and verify the parts.
        FeedbackWrapper<List<ProxyPatternGroup>> proxyPatterns = VerifyProxyParts.verifyParts(
            subjectGroups, proxyGroups);
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

        // Remove the classes used in valid instances from the maps.
        List<ClassOrInterfaceDeclaration> unusedProxies = getUnused(proxies, proxyGroups,
            Pattern.PROXY_PROXY);
        List<ClassOrInterfaceDeclaration> unusedInterfaces = getUnused(interfaces, proxyGroups,
            Pattern.PROXY_INTERFACE);
        List<ClassOrInterfaceDeclaration> unusedSubjects = getUnused(subjects, proxyGroups,
            Pattern.PROXY_SUBJECT);

        Tuple2<List<ProxyPatternGroup>, List<ProxyPatternGroup>> validInvalidTuple =
            splitToValidInvalid(proxyGroups);
        List<ProxyPatternGroup> valid = validInvalidTuple.getFirst();
        List<ProxyPatternGroup> invalid = validInvalidTuple.getSecond();

        List<Feedback> feedbacks = getInvalidFeedback(invalid, valid);

        // Now add feedback for those classes that are still not used.
        List<Feedback> unusedProxyFeedbacks = new ArrayList<>();
        if (!unusedProxies.isEmpty()) {
            unusedProxies.forEach(proxy -> {
                unusedProxyFeedbacks.add(Feedback.getNoChildFeedback(
                    proxy.getNameAsString() + " is marked as proxy class but no accompanying " +
                    "interface or subject could be found", new FeedbackTrace(proxy)));
            });
        }

        List<Feedback> unnusedInterfaceFeedbacks = new ArrayList<>();
        if (!unusedInterfaces.isEmpty()) {
            unusedInterfaces.forEach(interf -> {
                unnusedInterfaceFeedbacks.add(Feedback.getNoChildFeedback(
                    interf.getNameAsString() + " is marked as proxy interface but no accompanying" +
                    " proxy class or subject could be found", new FeedbackTrace(interf)));
            });
        }

        List<Feedback> unnusedSubjectFeedbacks = new ArrayList<>();
        if (!unusedSubjects.isEmpty()) {
            unusedSubjects.forEach(subject -> {
                unnusedSubjectFeedbacks.add(Feedback.getNoChildFeedback(
                    subject.getNameAsString() + " is marked as proxy subject but no accompanying " +
                    "interface or proxy class could be found", new FeedbackTrace(subject)));
            });
        }

        feedbacks.add(Feedback.getPatternInstanceFeedback(unusedProxyFeedbacks));
        feedbacks.add(Feedback.getPatternInstanceFeedback(unnusedInterfaceFeedbacks));
        feedbacks.add(Feedback.getPatternInstanceFeedback(unnusedSubjectFeedbacks));
        return Feedback.getPatternInstanceFeedback(feedbacks);
    }

    /**
     * Finds the unused classes for a specific pattern.
     *
     * @param all     all classes marked with the pattern.
     * @param valid   all the valid ProxyPatternGroups found.
     * @param pattern the pattern.
     *
     * @return a new list of unused classes of the given pattern.
     */
    private List<ClassOrInterfaceDeclaration> getUnused(
        List<ClassOrInterfaceDeclaration> all, List<ProxyPatternGroup> valid, Pattern pattern) {

        List<ClassOrInterfaceDeclaration> unused = new ArrayList<>();
        for (ClassOrInterfaceDeclaration classOrI : all) {
            boolean used = false;
            String classQualName = classOrI.resolve().getQualifiedName();
            // We cannot use .contains due to the fact that it can add the wrong class if
            // identical classes are in different packages for example.
            for (ProxyPatternGroup group : valid) {
                String currQualName = group.getFromPattern(pattern).resolve().getQualifiedName();
                if (classQualName.equals(currQualName)) {
                    used = true;
                    break;
                }
            }

            if (!used) {
                unused.add(classOrI);
            }
        }

        return unused;
    }

    /**
     * Splits the given list in valid and invalid pattern groups.
     *
     * @param patternGroups the pattern groups to split.
     *
     * @return a tuple containing two new lists, the first containing only the valid
     *     ProxyPatternGroups in the original list and the second containing only the invalid ones.
     */
    private Tuple2<List<ProxyPatternGroup>, List<ProxyPatternGroup>> splitToValidInvalid(
        List<ProxyPatternGroup> patternGroups) {
        List<ProxyPatternGroup> invalid = new ArrayList<>();
        List<ProxyPatternGroup> valid = new ArrayList<>();

        for (ProxyPatternGroup group : patternGroups) {
            if (group.isValid()) {
                valid.add(group);
            } else {
                invalid.add(group);
            }
        }
        return new Tuple2<>(valid, invalid);
    }

    /**
     * Returns the feedbacks from the invalid ProxyPatternGroups.
     *
     * @param invalid the invalid ProxyPatternGroups.
     * @param valid   the valid ProxyPatternGroupos.
     *
     * @return the feedbacks from the given ProxyPatternGroups.
     */
    private List<Feedback> getInvalidFeedback(
        List<ProxyPatternGroup> invalid, List<ProxyPatternGroup> valid) {
        List<Feedback> feedbacks = new ArrayList<>();

        for (ProxyPatternGroup invalidGroup : invalid) {
            // Must compare the qualified names, otherwise there can be issues with identical
            // classes in different packages.
            String invalidProxyQualName = invalidGroup.getProxy().resolve().getQualifiedName();
            String invalidInterfQualName =
                invalidGroup.getInterfaceOrAClass().resolve().getQualifiedName();
            String invalidSubjectQualName = invalidGroup.getSubject().resolve().getQualifiedName();

            // Add the feedback if one or more of the classes aren't used in a valid PatternGroup.
            boolean proxyUsed = false;
            boolean interfaceUsed = false;
            boolean subjectUsed = false;

            for (ProxyPatternGroup validGroup : valid) {
                String validProxyQualName = validGroup.getProxy().resolve().getQualifiedName();
                String validInterfQualName =
                    validGroup.getInterfaceOrAClass().resolve().getQualifiedName();
                String validSubjectQualName = validGroup.getSubject().resolve().getQualifiedName();

                if (validProxyQualName.equals(invalidProxyQualName)) {
                    proxyUsed = true;
                }

                if (validInterfQualName.equals(invalidInterfQualName)) {
                    interfaceUsed = true;
                }

                if (validSubjectQualName.equals(invalidSubjectQualName)) {
                    subjectUsed = true;
                }
            }

            // If any of them is false we want to add the error.
            if (!(proxyUsed && interfaceUsed && subjectUsed)) {
                feedbacks.add(
                    Feedback.getPatternInstanceFeedback(invalidGroup.getPotentialErrors()));
            }
        }

        return feedbacks;
    }
}