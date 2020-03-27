package tool.designpatterns.verifiers.multiclassverifiers.proxy.helpers;

import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;

public class ProxyMethodGroup {

    private final List<MethodDeclaration> proxyMethods;
    private final List<MethodDeclaration> subjectMethods;

    public ProxyMethodGroup(
        List<MethodDeclaration> proxyMethods, List<MethodDeclaration> subjectMethods) {
        this.proxyMethods = proxyMethods;
        this.subjectMethods = subjectMethods;
    }

    public List<MethodDeclaration> getProxyMethods() {
        return proxyMethods;
    }

    public List<MethodDeclaration> getSubjectMethods() {
        return subjectMethods;
    }
}
