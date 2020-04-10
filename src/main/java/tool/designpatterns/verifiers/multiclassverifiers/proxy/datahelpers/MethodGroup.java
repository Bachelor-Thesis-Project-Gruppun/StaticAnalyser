package tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers;

import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Represents a group of a interface method and the proxy subject / proxy proxy class that
 * implements that method.
 */
public final class MethodGroup {

    private final MethodDeclaration interfaceMethod;
    private final MethodDeclaration subjectMethod;
    private final MethodDeclaration proxyMethod;

    /**
     * Constructs a MethodGroup.
     *
     * @param interfaceMethod the method in the interface.
     * @param subjectMethod   the method in the subject that implements the interfaceMethod.
     * @param proxyMethod     the method in the proxy that implements the interfaceMethod.
     */
    public MethodGroup(
        MethodDeclaration interfaceMethod, MethodDeclaration subjectMethod,
        MethodDeclaration proxyMethod) {
        this.interfaceMethod = interfaceMethod;
        this.subjectMethod = subjectMethod;
        this.proxyMethod = proxyMethod;
    }

    /**
     * Get a new MethodGroup with only a interfaceMethod and SubjectMethod (proxyMethod will be
     * null).
     *
     * @param interfaceMethod the interface method
     * @param subjectMethod   the subject method that implements it.
     */
    @Deprecated
    public static MethodGroup getWithoutProxy(
        MethodDeclaration interfaceMethod, MethodDeclaration subjectMethod) {
        return new MethodGroup(interfaceMethod, subjectMethod, null);
    }

    /**
     * Get a new MethodDeclaration that copies the interfaceMethod and subjectMethod from the given
     * other MethodGroup and sets the proxyMethod to the given value.
     *
     * @param other       the other MethodGroup to copy from.
     * @param proxyMethod the proxyMethod.
     */
    @Deprecated
    public static MethodGroup getWithProxy(MethodGroup other, MethodDeclaration proxyMethod) {
        return new MethodGroup(other.interfaceMethod, other.subjectMethod, proxyMethod);
    }

    public MethodDeclaration getInterfaceMethod() {
        return interfaceMethod;
    }

    public MethodDeclaration getSubjectMethod() {
        return subjectMethod;
    }

    public MethodDeclaration getProxyMethod() {
        return proxyMethod;
    }
}
