package tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers;

import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * A simple helper to group an interface with its' interface methods (or abstract class with its'
 * abstract methods).
 */
public class InterfaceMethods {

    private final ClassOrInterfaceDeclaration interfaceOrAClass;
    private final List<MethodDeclaration> methods;

    public InterfaceMethods(
        ClassOrInterfaceDeclaration interfaceOrAClass, List<MethodDeclaration> methods) {
        this.interfaceOrAClass = interfaceOrAClass;
        this.methods = methods;
    }

    public ClassOrInterfaceDeclaration getInterfaceOrAClass() {
        return interfaceOrAClass;
    }

    public List<MethodDeclaration> getMethods() {
        return methods;
    }
}
