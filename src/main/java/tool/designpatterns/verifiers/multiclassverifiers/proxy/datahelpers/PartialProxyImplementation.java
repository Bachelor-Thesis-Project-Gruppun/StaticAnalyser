package tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers;

import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

/**
 * Represents a partial proxy pattern implementation with an interface and either a subject or a
 * proxy class.
 */
public class PartialProxyImplementation {

    private final ClassOrInterfaceDeclaration interfaceOrAClass;
    private final ClassOrInterfaceDeclaration interfaceImplementor;
    private final List<MethodGroupPart> methods;

    /**
     * Constructs a new PartialProxyImplementation.
     *
     * @param interfaceOrAClass    the Proxy Interface..
     * @param interfaceImplementor the Proxy Subject or Proxy Class.
     * @param methods              the potential proxy methods.
     */
    public PartialProxyImplementation(
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
