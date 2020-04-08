package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.MethodGroupPart;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyInterfaceImplementation;
import tool.feedback.Feedback;
import tool.feedback.FeedbackWrapper;

/**
 * Verifies if a a certain list of implementation classes of a list of Proxy Interface is valid.
 */
public class ProxyInterfaceImplementorVerifier {

    /**
     * Verifies the given list of supposed implementors (proxies or subjects) for the list of the
     * given interfaces.
     *
     * @param interfaces   the interfaces to verify against.
     * @param implementors the implementors to verify for.
     *
     * @return the result of the verification and list of ProxyInterfaceImplementations that can be
     *     empty.
     */
    public static FeedbackWrapper<List<ProxyInterfaceImplementation>> verifyImplementors(
        Map<ClassOrInterfaceDeclaration, List<MethodDeclaration>> interfaces,
        List<ClassOrInterfaceDeclaration> implementors) {

        List<Feedback> feedbacks = new ArrayList<>();
        List<ProxyInterfaceImplementation> proxyPatternParts = new ArrayList<>();

        // Go through each implementor and each interface and try to find matches.
        for (ClassOrInterfaceDeclaration implementor : implementors) {
            System.out.println(
                "CHECKING FOR IMPLEMENTOR " + implementor.resolve().getQualifiedName());
            for (ClassOrInterfaceDeclaration interfaceOrAClass : interfaces.keySet()) {
                System.out.println(
                    "CHECKING FOR INTERFACE " + interfaceOrAClass.resolve().getQualifiedName());
                FeedbackWrapper<ProxyInterfaceImplementation> proxyPatternPart = getPatternPart(
                    implementor, interfaceOrAClass, interfaces.get(interfaceOrAClass));

                feedbacks.add(proxyPatternPart.getFeedback());
                ProxyInterfaceImplementation implementation = proxyPatternPart.getOther();
                if (implementation != null) {
                    // We found a part of a proxy pattern!
                    proxyPatternParts.add(implementation);
                }
            }
        }

        Feedback fullFeedback = Feedback.getPatternInstanceFeedback(feedbacks);
        return new FeedbackWrapper<>(fullFeedback, proxyPatternParts);
    }

    /**
     * Verifies if the given implementor implements the interface as well as at least one of it's
     * (given) methods.
     *
     * @param implementor       The implementor to verify for.
     * @param interfaceOrAClass the interface to verify for.
     * @param interfaceMethods  the methods to verify for.
     *
     * @return the result of the verification and a ProxyInterfaceImplementation or null if the
     *     verification failed.
     */
    private static FeedbackWrapper<ProxyInterfaceImplementation> getPatternPart(
        ClassOrInterfaceDeclaration implementor, ClassOrInterfaceDeclaration interfaceOrAClass,
        List<MethodDeclaration> interfaceMethods) {

        List<Feedback> feedbacks = new ArrayList<>();
        List<MethodGroupPart> implementedMethods = new ArrayList<>();

        for (MethodDeclaration interfaceMethod : interfaceMethods) {
            FeedbackWrapper<MethodDeclaration> implementedMethodRes =
                MethodVerification.classImplementsMethod(
                    implementor, interfaceOrAClass, interfaceMethod);

            feedbacks.add(implementedMethodRes.getFeedback());
            MethodDeclaration implementedMethod = implementedMethodRes.getOther();
            if (implementedMethod != null) {
                // The implementor implemented this interface with the given method, add them to
                // a MethodGroupPart
                implementedMethods.add(new MethodGroupPart(interfaceMethod, implementedMethod));
            }
        }

        Feedback feedback = Feedback.getPatternInstanceFeedback(feedbacks);
        if (!implementedMethods.isEmpty()) {
            // This implementor implements the interface with at least one method, return a
            // ProxyInterfaceImplementation with them all.
            ProxyInterfaceImplementation proxyPatternPart = new ProxyInterfaceImplementation(
                interfaceOrAClass, implementor, implementedMethods);
            return new FeedbackWrapper<>(feedback, proxyPatternPart);
        }

        // This implementor appears not to implement the interface / any of it's methods.
        return new FeedbackWrapper<>(feedback, null);
    }
}
