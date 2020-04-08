package tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers;

import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public class ProxyInterfaceImplementation {

    private final ClassOrInterfaceDeclaration interfaceOrAClass;
    private final ClassOrInterfaceDeclaration interfaceImplementor;
    private final List<MethodGroupPart> methods;

    public ProxyInterfaceImplementation(
        ClassOrInterfaceDeclaration interfaceOrAClass,
        ClassOrInterfaceDeclaration interfaceImplementor, List<MethodGroupPart> methods) {
        this.interfaceOrAClass = interfaceOrAClass;
        this.interfaceImplementor = interfaceImplementor;
        this.methods = methods;
    }

    public ClassOrInterfaceDeclaration getInterfaceOrAClass() {
        return interfaceOrAClass;
    }

    public ClassOrInterfaceDeclaration getInterfaceImplementor() {
        return interfaceImplementor;
    }

    public List<MethodGroupPart> getMethods() {
        return methods;
    }
}
