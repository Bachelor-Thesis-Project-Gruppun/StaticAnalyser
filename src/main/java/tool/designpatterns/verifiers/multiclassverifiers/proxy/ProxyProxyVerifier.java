package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static tool.designpatterns.verifiers.multiclassverifiers.proxy.MethodVerification.classImplementsMethod;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.MethodGroup;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyPatternGroup;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.visitors.MethodCallVisitor;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.FeedbackWrapper;

/**
 * Class to verify the Proxy class part of the Proxy pattern.
 */
public class ProxyProxyVerifier {

    public static FeedbackWrapper<List<ProxyPatternGroup>> verifyProxies(
        List<ProxyPatternGroup> interfaceSubjects, List<ClassOrInterfaceDeclaration> proxies) {

        List<Feedback> feedbacks = new ArrayList<>();

        FeedbackWrapper<List<ProxyPatternGroup>> noVariableGroups = addProxies(proxies,
                                                                               interfaceSubjects);

        feedbacks.add(noVariableGroups.getFeedback());

        Feedback proxyUsesSubject = verifyProxiesUsesSubjects(noVariableGroups.getOther());

        feedbacks.add(proxyUsesSubject);

        return new FeedbackWrapper<>(Feedback.getPatternInstanceFeedback(feedbacks),
                                     noVariableGroups.getOther());
    }

    private static FeedbackWrapper<List<ProxyPatternGroup>> addProxies(
        List<ClassOrInterfaceDeclaration> proxies, List<ProxyPatternGroup> interfaceSubjects) {

        List<Feedback> groupingFeedbacks = new ArrayList<>();
        List<ProxyPatternGroup> noVariableGroups = new ArrayList<>();

        // Group together the new ProxyPatternGroups
        for (ClassOrInterfaceDeclaration proxy : proxies) {
            for (ProxyPatternGroup interfaceSubject : interfaceSubjects) {
                List<MethodGroup> newMethodGroups = new ArrayList<>();
                for (MethodGroup oldMethodGroup : interfaceSubject.getMethods()) {
                    FeedbackWrapper<MethodDeclaration> proxyMethod = classImplementsMethod(proxy,
                                                                                           interfaceSubject
                                                                                               .getInterfaceOrAClass(),
                                                                                           oldMethodGroup
                                                                                               .getInterfaceMethod());

                    groupingFeedbacks.add(proxyMethod.getFeedback());

                    if (proxyMethod.getOther() != null) {
                        newMethodGroups.add(
                            new MethodGroup(oldMethodGroup, proxyMethod.getOther()));
                    }
                }

                if (!newMethodGroups.isEmpty()) {
                    // The class implements the same interface as the subject.
                    noVariableGroups.add(
                        ProxyPatternGroup.getWithProxy(interfaceSubject, newMethodGroups, proxy));
                }
            }
        }

        return new FeedbackWrapper<>(Feedback.getPatternInstanceFeedback(groupingFeedbacks),
                                     noVariableGroups);
    }

    private static Feedback verifyProxiesUsesSubjects(
        List<ProxyPatternGroup> withoutVariables) {

        List<Feedback> feedbacks = new ArrayList<>();

        for (ProxyPatternGroup proxyGroup : withoutVariables) {
            // Get all possible variables (i.e. variables with the correct type)
            FeedbackWrapper<List<VariableDeclarator>> variableCandidates = getVariableCandidates(
                proxyGroup);

            feedbacks.add(variableCandidates.getFeedback());

            // Validate that every proxy method uses the corresponding subject method.
            feedbacks.add(verifyProxyUsesSubject(proxyGroup, variableCandidates.getOther()));
        }

        return Feedback.getPatternInstanceFeedback(feedbacks);
    }

    private static Feedback verifyProxyUsesSubject(
        ProxyPatternGroup proxyGroup, List<VariableDeclarator> proxyVariables) {
        List<Feedback> childFeedbacks = new ArrayList<>();
        boolean atLeastOneCalls = false;

        for (MethodGroup methodGroup : proxyGroup.getMethods()) {
            for (VariableDeclarator variable : proxyVariables) {
                Feedback feedback = methodCallsOther(methodGroup.getProxyMethod(),
                                                     methodGroup.getSubjectMethod(), variable);
                childFeedbacks.add(feedback);

                if (!feedback.getIsError()) {
                    atLeastOneCalls = true;
                }
            }
        }

        if (atLeastOneCalls) {
            return Feedback.getSuccessfulFeedback();
        }

        return Feedback.getPatternInstanceFeedback(childFeedbacks);
    }

    private static Feedback methodCallsOther(
        MethodDeclaration method, MethodDeclaration other, VariableDeclarator otherReference) {
        Boolean isValid = method.accept(new MethodCallVisitor(otherReference, other), null);
        if (isValid == null || !isValid) {
            return Feedback.getNoChildFeedback(
                "Proxy method " + method.getNameAsString() + " " + "does not call subject method " +
                other.getNameAsString(), new FeedbackTrace(method));
        }

        return Feedback.getSuccessfulFeedback();
    }

    private static FeedbackWrapper<List<VariableDeclarator>> getVariableCandidates(
        ProxyPatternGroup proxyGroup) {
        List<VariableDeclarator> variables = new ArrayList<>();
        for (FieldDeclaration field : proxyGroup.getProxy().getFields()) {
            for (VariableDeclarator variable : field.getVariables()) {
                if (VariableIsTypeofClass(variable, proxyGroup.getSubject())) {
                    variables.add(variable);
                }
            }
        }

        if (variables.isEmpty()) {
            return new FeedbackWrapper<>(Feedback.getNoChildFeedback(
                "Proxy class is missing a variable of subject type '" +
                proxyGroup.getSubject().getNameAsString() + "'",
                new FeedbackTrace(proxyGroup.getProxy())), variables);
        }

        return new FeedbackWrapper<>(Feedback.getSuccessfulFeedback(), variables);
    }

    private static boolean VariableIsTypeofClass(
        VariableDeclarator variable, ClassOrInterfaceDeclaration type) {
        ResolvedReferenceTypeDeclaration classType = type.resolve();

        AtomicBoolean isSameType = new AtomicBoolean(false);
        variable.getType().toClassOrInterfaceType().ifPresent(variableClassType -> {
            ResolvedReferenceType resolvedType = variableClassType.resolve();
            isSameType.set(resolvedType.getTypeDeclaration().equals(classType));
        });

        return isSameType.get();
    }
}