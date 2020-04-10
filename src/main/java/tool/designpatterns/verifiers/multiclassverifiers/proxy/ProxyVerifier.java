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
            FeedbackWrapper<List<MethodDeclaration>> interfaceMethods = getValidMethods(
                interfaceOrAClass);
            interfaceFeedbacks.add(interfaceMethods.getFeedback());
            interfaceMethodGroups.add(
                new InterfaceMethods(interfaceOrAClass, interfaceMethods.getOther()));
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
        feedbacks.add(
            allClassesAreUsed(proxyPatterns.getOther(), proxies, interfaces, subjects, proxyGroups,
                subjectGroups));

        return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
    }

    /**
     * Returns the valid methods found in the interface or abstract class provided.
     *
     * @param classOrI the interface or abstract class to look in.
     *
     * @return the feedback result as well as a list of the valid MethodDeclarations found.
     */
    private FeedbackWrapper<List<MethodDeclaration>> getValidMethods(
        ClassOrInterfaceDeclaration classOrI) {

        MethodDeclarationVisitor mdv = new MethodDeclarationVisitor();
        List<MethodDeclaration> methodDeclarations = classOrI.accept(mdv, classOrI);
        List<MethodDeclaration> validMethodDecs = new ArrayList<>();

        if (methodDeclarations.isEmpty()) {
            String message = "There are no methods to implement.";
            return new FeedbackWrapper<>(
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
                return new FeedbackWrapper<>(
                    Feedback.getNoChildFeedback(message, new FeedbackTrace(classOrI)),
                    methodDeclarations);
            }

        }

        return new FeedbackWrapper<>(Feedback.getSuccessfulFeedback(), validMethodDecs);
    }

    /**
     * Verifies that all the proxies, interfaces and subjects are used and that if they are only a
     * part of an 'invalid' proxy pattern, those errors are returned.
     *
     * @param proxyPatternGroups the finished proxy groups to check in.
     * @param proxies            the proxies to check for.
     * @param interfaces         the interfaces to check for.
     * @param subjects           the subjects to check for.
     * @param proxyGroups        the ungrouped proxy/interface groups.
     * @param subjectGroups      the ungrouped subject/interface groups.
     *
     * @return a feedback on how they are used.
     */
    private Feedback allClassesAreUsed(
        List<ProxyPatternGroup> proxyPatternGroups, List<ClassOrInterfaceDeclaration> proxies,
        List<ClassOrInterfaceDeclaration> interfaces, List<ClassOrInterfaceDeclaration> subjects,
        List<ProxyInterfaceImplementation> proxyGroups,
        List<ProxyInterfaceImplementation> subjectGroups) {

        // Remove the classes used in valid instances from the maps.
        List<ClassOrInterfaceDeclaration> unusedProxies = getUnused(proxies, proxyPatternGroups,
            Pattern.PROXY_PROXY);
        List<ClassOrInterfaceDeclaration> unusedInterfaces = getUnused(interfaces,
            proxyPatternGroups, Pattern.PROXY_INTERFACE);
        List<ClassOrInterfaceDeclaration> unusedSubjects = getUnused(subjects, proxyPatternGroups,
            Pattern.PROXY_SUBJECT);

        List<ProxyInterfaceImplementation> partialProxyGroups = getCorrectPartial(proxyGroups,
            proxyPatternGroups, Pattern.PROXY_PROXY);
        List<ProxyInterfaceImplementation> partialSubjectGroups = getCorrectPartial(subjectGroups,
            proxyPatternGroups, Pattern.PROXY_SUBJECT);

        Tuple2<List<ProxyPatternGroup>, List<ProxyPatternGroup>> validInvalidTuple =
            splitToValidInvalid(proxyPatternGroups);
        List<ProxyPatternGroup> valid = validInvalidTuple.getFirst();
        List<ProxyPatternGroup> invalid = validInvalidTuple.getSecond();

        List<Feedback> feedbacks = getInvalidFeedback(invalid, valid);

        // Now add feedback for those classes that are still not used.
        feedbacks.add(
            getUnusedFeedbacks(unusedProxies, unusedInterfaces, unusedSubjects, partialProxyGroups,
                partialSubjectGroups));

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

    /**
     * Returns a list of feedbacks for the unused classes and the partially implemented groups.
     *
     * @param unusedProxies    a list of proxies not used in complete patternGroups.
     * @param unusedInterfaces a list of interfaces not used in complete patternGroups.
     * @param unusedSubjects   a list of subjects not used in complete patternGroups.
     * @param proxyGroups      list of partially implemented groups with proxies / interfaces.
     * @param subjectGroups    list of partially implemented groups with subjects / interfaces.
     *
     * @return A feedback on all of the above combined.
     */
    private Feedback getUnusedFeedbacks(
        List<ClassOrInterfaceDeclaration> unusedProxies,
        List<ClassOrInterfaceDeclaration> unusedInterfaces,
        List<ClassOrInterfaceDeclaration> unusedSubjects,
        List<ProxyInterfaceImplementation> proxyGroups,
        List<ProxyInterfaceImplementation> subjectGroups) {

        List<Feedback> unusedFeedbacks = new ArrayList<>();

        // The lists of the previously 'unused' classes that were actually used in partial patterns.
        List<ClassOrInterfaceDeclaration> usedUnusedProxies = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> usedUnusedInterfaces = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> usedUnusedSubjects = new ArrayList<>();

        // Remove all the 'unused' classes used in partial groups (proxyGroups/subjectGroups).
        proxyGroups.forEach(proxyGroup -> {
            ClassOrInterfaceDeclaration proxy = proxyGroup.getInterfaceImplementor();
            if (ClassOrInterfaceListContains(unusedProxies, proxy)) {
                if (!ClassOrInterfaceListContains(usedUnusedProxies, proxy)) {
                    usedUnusedProxies.add(proxy);
                }
            }

            ClassOrInterfaceDeclaration interf = proxyGroup.getInterfaceOrAClass();
            if (ClassOrInterfaceListContains(unusedInterfaces, interf)) {
                if (!ClassOrInterfaceListContains(usedUnusedInterfaces, interf)) {
                    usedUnusedInterfaces.add(interf);
                }
            }

            // Add feedback about this proxyGroup.
            String proxyName = proxy.resolve().getQualifiedName();
            String interfaceName = interf.resolve().getQualifiedName();
            unusedFeedbacks.add(Feedback.getPatternInstanceNoChildFeedback(
                "No subject found for Proxy class / Proxy interface pair '" + proxyName + "' and " +
                "'" + interfaceName + "'"));
        });

        subjectGroups.forEach(subjectGroup -> {
            ClassOrInterfaceDeclaration subject = subjectGroup.getInterfaceImplementor();
            if (ClassOrInterfaceListContains(unusedSubjects, subject)) {
                if (!ClassOrInterfaceListContains(usedUnusedSubjects, subject)) {
                    usedUnusedSubjects.add(subject);
                }
            }

            ClassOrInterfaceDeclaration interf = subjectGroup.getInterfaceOrAClass();
            if (ClassOrInterfaceListContains(unusedInterfaces, interf)) {
                if (!ClassOrInterfaceListContains(usedUnusedInterfaces, interf)) {
                    usedUnusedInterfaces.add(interf);
                }
            }

            // Add feedback about this subjectGroup.
            String subjectName = subject.resolve().getQualifiedName();
            String interfaceName = interf.resolve().getQualifiedName();
            unusedFeedbacks.add(Feedback.getPatternInstanceNoChildFeedback(
                "No subject found for Proxy subject / Proxy interface pair '" + subjectName + "' " +
                "and " + "'" + interfaceName + "'"));
        });

        // Now add the feedback for the unused classes that are NOT in the usedUnusedList.
        unusedProxies.forEach(proxy -> {
            if (!ClassOrInterfaceListContains(usedUnusedProxies, proxy)) {
                unusedFeedbacks.add(Feedback.getNoChildFeedback(
                    proxy.getNameAsString() + " is marked as proxy class but no accompanying " +
                    "interface or subject could be found", new FeedbackTrace(proxy)));
            }
        });
        unusedInterfaces.forEach(interf -> {
            if (!ClassOrInterfaceListContains(usedUnusedInterfaces, interf)) {
                unusedFeedbacks.add(Feedback.getNoChildFeedback(
                    interf.getNameAsString() + " is marked as proxy interface but no accompanying" +
                    " interface or subject could be found", new FeedbackTrace(interf)));
            }
        });
        unusedProxies.forEach(subject -> {
            if (!ClassOrInterfaceListContains(usedUnusedSubjects, subject)) {
                unusedFeedbacks.add(Feedback.getNoChildFeedback(
                    subject.getNameAsString() + " is marked as proxy subject but no accompanying " +
                    "interface or subject could be found", new FeedbackTrace(subject)));
            }
        });

        return Feedback.getPatternInstanceFeedback(unusedFeedbacks);
    }

    /**
     * Returns the partial patterns that are not in any complete ProxyPatternGroup.
     *
     * @param partial  all the partial groups for either proxy or subject.
     * @param complete the complete groups.
     * @param pattern  the pattern (either subject or proxy) that are in the partial list.
     *
     * @return a new list containing the partial patterns that were not in any of the complete
     *     groups.
     */
    private List<ProxyInterfaceImplementation> getCorrectPartial(
        List<ProxyInterfaceImplementation> partial, List<ProxyPatternGroup> complete,
        Pattern pattern) {
        List<ProxyInterfaceImplementation> newPartial = new ArrayList<>();

        for (ProxyInterfaceImplementation partialGroup : partial) {
            boolean isUsed = false;
            for (ProxyPatternGroup completeGroup : complete) {
                if (!ClassOrInterfaceAreSame(partialGroup.getInterfaceOrAClass(),
                    completeGroup.getInterfaceOrAClass())) {
                    // The interface is not used.
                    continue;
                }

                if (ClassOrInterfaceAreSame(partialGroup.getInterfaceImplementor(),
                    completeGroup.getFromPattern(pattern))) {
                    // the pattern is also used.
                    isUsed = true;
                }
            }

            if (!isUsed) {
                newPartial.add(partialGroup);
            }
        }

        return newPartial;
    }

    /**
     * Returns true if class a equals class b
     *
     * @param a the first class to check.
     * @param b the second class to check.
     *
     * @return if a equals b.
     */
    private boolean ClassOrInterfaceAreSame(
        ClassOrInterfaceDeclaration a, ClassOrInterfaceDeclaration b) {
        return a.resolve().getQualifiedName().equals(b.resolve().getQualifiedName());
    }

    /**
     * Returns wether or not the given list of ClassOrInterfaceDeclarations contains the given class
     * or not.
     *
     * @param classOrInterfaceList the list to look in.
     * @param lookFor              the class to look for.
     *
     * @return true if the list contains the class, false otherwise.
     */
    private boolean ClassOrInterfaceListContains(
        List<ClassOrInterfaceDeclaration> classOrInterfaceList,
        ClassOrInterfaceDeclaration lookFor) {

        String qualLookFor = lookFor.resolve().getQualifiedName();
        for (ClassOrInterfaceDeclaration classOrI : classOrInterfaceList) {
            if (classOrI.resolve().getQualifiedName().equals(qualLookFor)) {
                return true;
            }
        }

        return false;
    }
}