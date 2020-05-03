package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.InterfaceMethods;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.MethodGroupPart;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.PartialProxyImplementation;
import tool.feedback.FeedbackWrapper;

/**
 * Verifies if a certain list of partial proxy implementations are valid.
 */
public final class PartialProxyVerifier {

    private PartialProxyVerifier() {

    }

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
    public static List<PartialProxyImplementation> verifyImplementors(
        List<InterfaceMethods> interfaces, List<ClassOrInterfaceDeclaration> implementors) {

        List<PartialProxyImplementation> proxyPatternParts = new ArrayList<>();

        // Go through each implementor and each interface and try to find matches.
        for (ClassOrInterfaceDeclaration implementor : implementors) {
            for (InterfaceMethods interfaceMethods : interfaces) {
                ClassOrInterfaceDeclaration interfaceOrAClass = interfaceMethods
                    .getInterfaceOrAClass();
                PartialProxyImplementation implementation = getPatternPart(
                    implementor, interfaceOrAClass, interfaceMethods.getMethods());

                if (implementation != null) {
                    // We found a part of a proxy pattern!
                    proxyPatternParts.add(implementation);
                }
            }
        }

        return proxyPatternParts;
    }

    /**
     * Verifies if the given implementor implements the interface as well as at least one of it's
     * (given) methods.
     *
     * @param implementor       The implementor to verify for.
     * @param interfaceOrAClass the interface to verify for.
     * @param interfaceMethods  the methods to verify for.
     *
     * @return a Partial Proxy representing the pattern part or null if the verification failed.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static PartialProxyImplementation getPatternPart(
        ClassOrInterfaceDeclaration implementor, ClassOrInterfaceDeclaration interfaceOrAClass,
        List<MethodDeclaration> interfaceMethods) {

        List<MethodGroupPart> implementedMethods = new ArrayList<>();

        for (MethodDeclaration interfaceMethod : interfaceMethods) {
            FeedbackWrapper<MethodDeclaration> implementedMethodRes = MethodVerification
                .classImplementsMethod(implementor, interfaceOrAClass, interfaceMethod);

            MethodDeclaration implementedMethod = implementedMethodRes.getOther();
            if (implementedMethod != null) {
                // The implementor implemented this interface with the given method, add them to
                // a MethodGroupPart
                implementedMethods.add(new MethodGroupPart(interfaceMethod, implementedMethod));
            }
        }

        if (!implementedMethods.isEmpty()) {
            // This implementor implements the interface with at least one method, return a
            // ProxyInterfaceImplementation with them all.
            return new PartialProxyImplementation(
                interfaceOrAClass, implementor, implementedMethods);
        }

        // This implementor appears not to implement the interface / any of it's methods.
        return null;
    }
}
