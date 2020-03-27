package tool.designpatterns.verifiers.multiclassverifiers.proxy.helpers;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Simple tuple3 that groups an InterfaceSubjectTuple, a proxyClass and the proxyMethod that
 * implements the interfaceSubjects interfaceMethod together.
 */
public class InterfaceSubjectProxyTuple {

    private final InterfaceSubjectTuple interfaceSubject;
    private final ClassOrInterfaceDeclaration proxy;
    private final MethodDeclaration proxyMethod;

    public InterfaceSubjectProxyTuple(
        InterfaceSubjectTuple interfaceSubject, ClassOrInterfaceDeclaration proxy,
        MethodDeclaration proxyMethod) {
        this.interfaceSubject = interfaceSubject;
        this.proxy = proxy;
        this.proxyMethod = proxyMethod;
    }

    public InterfaceSubjectTuple getInterfaceSubject() {
        return interfaceSubject;
    }

    public ClassOrInterfaceDeclaration getProxy() {
        return proxy;
    }

    public MethodDeclaration getProxyMethod() {
        return proxyMethod;
    }
}
