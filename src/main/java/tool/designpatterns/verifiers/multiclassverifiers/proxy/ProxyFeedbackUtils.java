package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;

import static tool.designpatterns.verifiers.multiclassverifiers.proxy.ClassVerification.classOrInterfaceListContains;
import static tool.designpatterns.verifiers.multiclassverifiers.proxy.ClassVerification.isSameClassOrInterfaceDeclaration;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import groovy.lang.Tuple2;
import groovy.lang.Tuple3;
import tool.designpatterns.Pattern;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.PartialProxyImplementation;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyPatternGroup;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;

/**
 * Utility class to assist with finding issues with / providing feedback for the Proxy pattern
 * verification.
 */
@SuppressWarnings("PMD.CommentSize") // Justification: Did not work when I put it on methods, the
// reason for why the lines are soo long are due to the number of parameters. There are too many
// parameters but they would, I believe, take too much to refactor away in a good mannor.
public final class ProxyFeedbackUtils {

    private ProxyFeedbackUtils() {

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
    public static Feedback allClassesAreUsed(
        List<ProxyPatternGroup> proxyPatternGroups, List<ClassOrInterfaceDeclaration> proxies,
        List<ClassOrInterfaceDeclaration> interfaces, List<ClassOrInterfaceDeclaration> subjects,
        List<PartialProxyImplementation> proxyGroups,
        List<PartialProxyImplementation> subjectGroups) {

        // Remove the classes used in valid instances from the maps.
        List<ClassOrInterfaceDeclaration> unusedProxies = getUnused(proxies, proxyPatternGroups,
            Pattern.PROXY_PROXY);
        List<ClassOrInterfaceDeclaration> unusedInterfaces = getUnused(interfaces,
            proxyPatternGroups, Pattern.PROXY_INTERFACE);
        List<ClassOrInterfaceDeclaration> unusedSubjects = getUnused(subjects, proxyPatternGroups,
            Pattern.PROXY_SUBJECT);

        List<PartialProxyImplementation> partialProxyGroups = getCorrectPartial(proxyGroups,
            proxyPatternGroups, Pattern.PROXY_PROXY);
        List<PartialProxyImplementation> partialSubjectGroups = getCorrectPartial(subjectGroups,
            proxyPatternGroups, Pattern.PROXY_SUBJECT);

        Tuple2<List<ProxyPatternGroup>, List<ProxyPatternGroup>> validInvalidTuple
            = splitToValidInvalid(proxyPatternGroups);
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
    private static List<ClassOrInterfaceDeclaration> getUnused(
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
     * Returns the partial patterns that are not in any complete ProxyPatternGroup.
     *
     * @param partial  all the partial groups for either proxy or subject.
     * @param complete the complete groups.
     * @param pattern  the pattern (either subject or proxy) that are in the partial list.
     *
     * @return a new list containing the partial patterns that were not in any of the complete
     *     groups.
     */
    private static List<PartialProxyImplementation> getCorrectPartial(
        List<PartialProxyImplementation> partial, List<ProxyPatternGroup> complete,
        Pattern pattern) {
        List<PartialProxyImplementation> newPartial = new ArrayList<>();

        for (PartialProxyImplementation partialGroup : partial) {
            boolean isUsed = false;
            for (ProxyPatternGroup completeGroup : complete) {
                if (!isSameClassOrInterfaceDeclaration(partialGroup.getInterfaceOrAClass(),
                    completeGroup.getInterfaceOrAClass())) {
                    // The interface is not used.
                    continue;
                }

                if (isSameClassOrInterfaceDeclaration(partialGroup.getInterfaceImplementor(),
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
     * Splits the given list in valid and invalid pattern groups.
     *
     * @param patternGroups the pattern groups to split.
     *
     * @return a tuple containing two new lists, the first containing only the valid
     *     ProxyPatternGroups in the original list and the second containing only the invalid ones.
     */
    private static Tuple2<List<ProxyPatternGroup>, List<ProxyPatternGroup>> splitToValidInvalid(
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
    private static List<Feedback> getInvalidFeedback(
        List<ProxyPatternGroup> invalid, List<ProxyPatternGroup> valid) {
        List<Feedback> feedbacks = new ArrayList<>();

        for (ProxyPatternGroup invalidGroup : invalid) {
            // Must compare the qualified names, otherwise there can be issues with identical
            // classes in different packages.
            String invalidProxyName = invalidGroup.getProxy().resolve().getQualifiedName();
            String invalidInterfName = invalidGroup.getInterfaceOrAClass().resolve()
                                                   .getQualifiedName();
            String invalidSubjectName = invalidGroup.getSubject().resolve().getQualifiedName();

            // Add the feedback if one or more of the classes aren't used in a valid PatternGroup.
            boolean proxyUsed = false;
            boolean interfaceUsed = false;
            boolean subjectUsed = false;

            for (ProxyPatternGroup validGroup : valid) {
                String validProxyQualName = validGroup.getProxy().resolve().getQualifiedName();
                String validInterfQualName = validGroup.getInterfaceOrAClass().resolve()
                                                       .getQualifiedName();
                String validSubjectQualName = validGroup.getSubject().resolve().getQualifiedName();

                if (validProxyQualName.equals(invalidProxyName)) {
                    proxyUsed = true;
                }

                if (validInterfQualName.equals(invalidInterfName)) {
                    interfaceUsed = true;
                }

                if (validSubjectQualName.equals(invalidSubjectName)) {
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
    private static Feedback getUnusedFeedbacks(
        List<ClassOrInterfaceDeclaration> unusedProxies,
        List<ClassOrInterfaceDeclaration> unusedInterfaces,
        List<ClassOrInterfaceDeclaration> unusedSubjects,
        List<PartialProxyImplementation> proxyGroups,
        List<PartialProxyImplementation> subjectGroups) {

        List<Feedback> unusedFeedbacks = new ArrayList<>();

        // The lists of the previously 'unused' classes that were actually used in partial patterns.
        Tuple3<List<ClassOrInterfaceDeclaration>, List<ClassOrInterfaceDeclaration>,
            List<ClassOrInterfaceDeclaration>>
            usedUnusedClasses = getUsedUnusedClasses(unusedProxies, unusedInterfaces,
            unusedSubjects, proxyGroups, subjectGroups);

        List<ClassOrInterfaceDeclaration> usedProxies = usedUnusedClasses.getFirst();
        unusedFeedbacks.addAll(getPartialFeedbacks(proxyGroups, subjectGroups));

        // Now add the feedback for the unused classes that are NOT in the usedUnusedList.
        unusedProxies.forEach(proxy -> {
            if (!classOrInterfaceListContains(usedProxies, proxy)) {
                unusedFeedbacks.add(Feedback.getNoChildFeedback(
                    proxy.getNameAsString() + " is marked as proxy class but no " +
                    "accompanying interface or subject could be found.", new FeedbackTrace(proxy)));
            }
        });
        List<ClassOrInterfaceDeclaration> usedInterfaces = usedUnusedClasses.getSecond();
        unusedInterfaces.forEach(interf -> {
            if (!classOrInterfaceListContains(usedInterfaces, interf)) {
                unusedFeedbacks.add(Feedback.getNoChildFeedback(
                    interf.getNameAsString() + " is marked as proxy interface but no " +
                    "accompanying interface or subject could be found.",
                    new FeedbackTrace(interf)));
            }
        });
        List<ClassOrInterfaceDeclaration> usedSubjects = usedUnusedClasses.getThird();
        unusedProxies.forEach(subject -> {
            if (!classOrInterfaceListContains(usedSubjects, subject)) {
                unusedFeedbacks.add(Feedback.getNoChildFeedback(
                    subject.getNameAsString() + " is marked as proxy subject but no " +
                    "accompanying interface or subject could be found.",
                    new FeedbackTrace(subject)));
            }
        });

        return Feedback.getPatternInstanceFeedback(unusedFeedbacks);
    }

    /**
     * From the unused classes retrieve the ones that are in fact used.
     *
     * @param unusedProxies    the proxies not used by the complete proxy groups.
     * @param unusedInterfaces the interfaces not used by the complete proxy groups.
     * @param unusedSubjects   the subjects not used by the complete proxy groups.
     * @param proxyGroups      the partially complete proxy groups (proxy/interface).
     * @param subjectGroups    the partially complete subject groups (subject/interface).
     *
     * @return A tuple3 containing the
     */
    private static Tuple3<List<ClassOrInterfaceDeclaration>, List<ClassOrInterfaceDeclaration>,
        List<ClassOrInterfaceDeclaration>> getUsedUnusedClasses(
        List<ClassOrInterfaceDeclaration> unusedProxies,
        List<ClassOrInterfaceDeclaration> unusedInterfaces,
        List<ClassOrInterfaceDeclaration> unusedSubjects,
        List<PartialProxyImplementation> proxyGroups,
        List<PartialProxyImplementation> subjectGroups) {

        List<ClassOrInterfaceDeclaration> usedProxies = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> usedInterfaces = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> usedSubjects = new ArrayList<>();

        // Remove all the 'unused' classes used in partial groups (proxyGroups/subjectGroups).
        proxyGroups.forEach(proxyGroup -> {
            ClassOrInterfaceDeclaration proxy = proxyGroup.getInterfaceImplementor();
            if (classOrInterfaceListContains(unusedProxies, proxy) && !classOrInterfaceListContains(
                usedProxies, proxy)) {
                usedProxies.add(proxy);
            }

            ClassOrInterfaceDeclaration interf = proxyGroup.getInterfaceOrAClass();
            if (classOrInterfaceListContains(unusedInterfaces, interf) &&
                !classOrInterfaceListContains(usedInterfaces, interf)) {
                usedInterfaces.add(interf);
            }
        });

        subjectGroups.forEach(subjectGroup -> {
            ClassOrInterfaceDeclaration subject = subjectGroup.getInterfaceImplementor();
            if (classOrInterfaceListContains(unusedSubjects, subject) &&
                !classOrInterfaceListContains(usedSubjects, subject)) {
                usedSubjects.add(subject);

            }

            ClassOrInterfaceDeclaration interf = subjectGroup.getInterfaceOrAClass();
            if (classOrInterfaceListContains(unusedInterfaces, interf) &&
                !classOrInterfaceListContains(usedInterfaces, interf)) {
                usedInterfaces.add(interf);

            }
        });

        return new Tuple3<>(usedProxies, usedInterfaces, usedSubjects);
    }

    private static List<Feedback> getPartialFeedbacks(
        List<PartialProxyImplementation> proxyGroups,
        List<PartialProxyImplementation> subjectGroups) {

        List<Feedback> feedbacks = new ArrayList<>();

        proxyGroups.forEach(proxyGroup -> {
            // Add feedback about this proxyGroup.
            String proxyName = proxyGroup.getInterfaceImplementor().resolve().getQualifiedName();
            String interfaceName = proxyGroup.getInterfaceOrAClass().resolve().getQualifiedName();
            feedbacks.add(Feedback.getPatternInstanceNoChildFeedback(
                "No subject found for Proxy class / Proxy interface pair '" + proxyName + "' and " +
                "'" + interfaceName + "'."));
        });

        subjectGroups.forEach(subjectGroup -> {
            // Add feedback about this subjectGroup.
            String subjectName = subjectGroup.getInterfaceImplementor().resolve()
                                             .getQualifiedName();
            String interfaceName = subjectGroup.getInterfaceOrAClass().resolve().getQualifiedName();
            feedbacks.add(Feedback.getPatternInstanceNoChildFeedback(
                "No subject found for Proxy subject / Proxy interface pair '" + subjectName + "' " +
                "and '" + interfaceName + "'."));

        });

        return feedbacks;
    }
}