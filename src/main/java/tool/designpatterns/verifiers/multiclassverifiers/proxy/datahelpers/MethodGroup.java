package tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers;

import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Represents a group of a interface method and the proxy subject / proxy proxy class that
 * implements that method.
 */
public class MethodGroup {

    private final MethodDeclaration interfaceMethod;
    private final MethodDeclaration subjectMethod;
    private final MethodDeclaration proxyMethod;

    /**
     * Get a new MethodGroup with only a interfaceMethod and SubjectMethod (proxyMethod will be
     * null).
     *
     * @param interfaceMethod the interface method
     * @param subjectMethod   the subject method that implements it.
     */
    public MethodGroup(MethodDeclaration interfaceMethod, MethodDeclaration subjectMethod) {
        this.interfaceMethod = interfaceMethod;
        this.subjectMethod = subjectMethod;
        this.proxyMethod = null;
    }

    /**
     * Get a new MethodDeclaration that copies the interfaceMethod and subjectMethod from the given
     * other MethodGroup and sets the proxyMethod to the given value.
     *
     * @param other       the other MethodGroup to copy from.
     * @param proxyMethod the proxyMethod.
     */
    public MethodGroup(MethodGroup other, MethodDeclaration proxyMethod) {
        this.interfaceMethod = other.interfaceMethod;
        this.subjectMethod = other.subjectMethod;
        this.proxyMethod = proxyMethod;
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
