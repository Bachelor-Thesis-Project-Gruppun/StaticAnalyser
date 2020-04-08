package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.MethodGroup;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.MethodGroupPart;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyInterfaceImplementation;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyPatternGroup;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.FeedbackWrapper;

/**
 * A class to verify and group parts or proxy patterns (ProxyInterfaceImplmenetations)
 */
public class VerifyProxyParts {

    /**
     * Verifies and groups together the given subjects with the given proxies.
     *
     * @param subjects the subjects to verify and group.
     * @param proxies  the proxies to verify and group.
     *
     * @return A list of complete ProxyPatternGroups
     */
    public static FeedbackWrapper<List<ProxyPatternGroup>> verifyParts(
        List<ProxyInterfaceImplementation> subjects, List<ProxyInterfaceImplementation> proxies) {

        List<Feedback> feedbacks = new ArrayList<>();
        List<ProxyPatternGroup> patternGroups = new ArrayList<>();

        for (ProxyInterfaceImplementation subject : subjects) {
            for (ProxyInterfaceImplementation proxy : proxies) {
                FeedbackWrapper<ProxyPatternGroup> group = tryGroup(subject, proxy);
                feedbacks.add(group.getFeedback());
                if (group.getOther() != null) {
                    // It found a ProxyPatternGroup!
                    patternGroups.add(group.getOther());
                }
            }
        }

        return new FeedbackWrapper<>(Feedback.getPatternInstanceFeedback(feedbacks), patternGroups);
    }

    /**
     * Tries to group the given proxy with the given subject.
     *
     * @param subject the subject to check for.
     * @param proxy   the proxy to check for.
     *
     * @return The result of the verification / grouping and a ProxyPatternGroup should they be
     *     compatible or null otherwise.
     */
    private static FeedbackWrapper<ProxyPatternGroup> tryGroup(
        ProxyInterfaceImplementation subject, ProxyInterfaceImplementation proxy) {

        // Compare if they have the same interfaces.
        boolean sameInterface = isSameClassOrInterface(subject.getInterfaceOrAClass(),
            proxy.getInterfaceOrAClass());
        if (!sameInterface) {
            // Not the same interface, definitely not the same proxy pattern.
            return new FeedbackWrapper<>(Feedback.getSuccessfulFeedback(), null);
        }

        List<Feedback> potentialErrors = new ArrayList<>();
        String subjectQualName = subject.getInterfaceImplementor().resolve().getQualifiedName();

        // They implement the same interface, check if the proxy has a private variable of the
        // subjects type.
        boolean hasVariable = hasVariable(
            subject.getInterfaceImplementor(), proxy.getInterfaceImplementor());

        // Try to group the methods together.
        List<MethodGroup> methodGroups = groupMethods(subject.getMethods(), proxy.getMethods());

        // Find the proxies methods that calls the subjects methods.
        boolean callsMethods = proxyCallsSubject(methodGroups, subject.getInterfaceImplementor());

        // Decide if this is the same Proxy pattern and either return them as a group or give an
        // error (or both).

        if (!hasVariable) {
            // Does not have variable, could still be the same proxy pattern.
            potentialErrors.add(Feedback.getNoChildFeedback(
                "Proxy has no (private) variable of subject type '" + subjectQualName + "'",
                new FeedbackTrace(proxy.getInterfaceImplementor())));
        } else {
            if (methodGroups.isEmpty()) {
                // ASSUMPTION:: The proxy has a variable of the subjects type and they implement
                // the same interface / abstract class, this means they are a part of the same
                // pattern.

                potentialErrors.add(Feedback.getNoChildFeedback(
                    "Proxy does not implement any common methods with subject '" + subjectQualName +
                    "'", new FeedbackTrace(proxy.getInterfaceImplementor())));
                return new FeedbackWrapper<>(Feedback.getPatternInstanceFeedback(potentialErrors),
                    null);
            }
        }

        if (callsMethods) {
            // Everything appears to be in order, return a valid ProxyPatternGroup.
            ProxyPatternGroup proxyPattern = ProxyPatternGroup.getValidProxyGroup(
                subject.getInterfaceOrAClass(), subject.getInterfaceImplementor(),
                proxy.getInterfaceImplementor(), methodGroups);
            return new FeedbackWrapper<>(Feedback.getSuccessfulFeedback(), proxyPattern);
        } else if (hasVariable && !methodGroups.isEmpty()) {
            // ASSUMPTION:: The proxy does not call the subject but it has a variable of the
            // subjects type and they implement the same methods, therefore they are the same
            // pattern instance.
            // Return an invalid ProxyPatternGroup.
            potentialErrors.add(Feedback.getNoChildFeedback(
                "Proxy does not call any subject (" + subjectQualName + ") methods ",
                new FeedbackTrace(proxy.getInterfaceImplementor())));
            ProxyPatternGroup invalidGroup = ProxyPatternGroup.getInvalidProxyGroup(
                subject.getInterfaceOrAClass(), subject.getInterfaceImplementor(),
                proxy.getInterfaceImplementor(), methodGroups, potentialErrors);
            return new FeedbackWrapper<>(Feedback.getSuccessfulFeedback(), invalidGroup);
        }

        // Not identified as a ProxyPatternGroup.
        return new FeedbackWrapper<>(Feedback.getSuccessfulFeedback(), null);
    }

    /**
     * Compares if two ClassOrInterfaceImplementations are the same.
     *
     * @param a the first to check.
     * @param b the second to check.
     *
     * @return if a is the same as b.
     */
    private static boolean isSameClassOrInterface(
        ClassOrInterfaceDeclaration a, ClassOrInterfaceDeclaration b) {

        ResolvedReferenceTypeDeclaration subjectInterface = a.resolve();
        ResolvedReferenceTypeDeclaration proxyInterface = b.resolve();
        return subjectInterface.getQualifiedName().equals(proxyInterface.getQualifiedName());
    }

    /**
     * Returns if the proxy has a (private) variable of the subjects type.
     *
     * @param subject the type to check for.
     * @param proxy   the class to check in.
     *
     * @return true if the proxy has the variable and false otherwise.
     */
    private static boolean hasVariable(
        ClassOrInterfaceDeclaration subject, ClassOrInterfaceDeclaration proxy) {

        ResolvedReferenceTypeDeclaration resolvedRefTypeDec = subject.resolve();

        for (FieldDeclaration field : proxy.getFields()) {
            if (field.isPrivate()) {
                // Check if the field has the correct type.
                ResolvedReferenceType fieldType = field.resolve().getType().asReferenceType();

                if (fieldType.getQualifiedName().equals(resolvedRefTypeDec.getQualifiedName())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Verifies and groups together MethodGroupParts for subject and proxies.
     *
     * @param subjectGroups the subjects methodGroupParts.
     * @param proxyGroups   the proxies methodGroupParts.
     *
     * @return a new list with the grouped MethodGroups.
     */
    private static List<MethodGroup> groupMethods(
        List<MethodGroupPart> subjectGroups, List<MethodGroupPart> proxyGroups) {
        List<MethodGroup> methodGroups = new ArrayList<>();
        for (MethodGroupPart subjectGroup : subjectGroups) {
            for (MethodGroupPart proxyGroup : proxyGroups) {
                // Compare if they have the same interface method.
                if (MethodVerification.methodsAreSame(subjectGroup.getInterfaceMethod(),
                    proxyGroup.getInterfaceMethod())) {
                    methodGroups.add(new MethodGroup(subjectGroup.getInterfaceMethod(),
                        subjectGroup.getImplementorMethod(), proxyGroup.getImplementorMethod()));
                }
            }
        }

        return methodGroups;
    }

    /**
     * Returns true if at least one of the proxy methods in the list calls its' corresponding
     * subjects method.
     *
     * @param methodGroups the methodGroups to verify for.
     *
     * @return true if a proxy calls its' subject, otherwise false.
     */
    private static boolean proxyCallsSubject(
        List<MethodGroup> methodGroups, ClassOrInterfaceDeclaration subject) {

        for (MethodGroup methodGroup : methodGroups) {
            ResolvedReferenceTypeDeclaration subjectType = subject.resolve();

            if (MethodVerification.methodCallsOther(methodGroup.getProxyMethod(),
                methodGroup.getSubjectMethod(), subject.resolve())) {
                return true;
            }
        }
        return false;
    }
}
