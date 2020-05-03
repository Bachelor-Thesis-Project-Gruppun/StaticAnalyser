package tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers;

import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Groups an interface (or abstract class) method with a method that implements it.
 */
public class MethodGroupPart {

    private final MethodDeclaration interfaceMethod;
    private final MethodDeclaration implementorMethod;

    public MethodGroupPart(MethodDeclaration interfaceMethod, MethodDeclaration implementorMethod) {
        this.interfaceMethod = interfaceMethod;
        this.implementorMethod = implementorMethod;
    }

    public MethodDeclaration getInterfaceMethod() {
        return this.interfaceMethod;
    }

    public MethodDeclaration getImplementorMethod() {
        return this.implementorMethod;
    }
}
