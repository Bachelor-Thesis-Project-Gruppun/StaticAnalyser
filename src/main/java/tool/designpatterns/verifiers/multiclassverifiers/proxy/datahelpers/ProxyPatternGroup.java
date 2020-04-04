package tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers;

import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

/**
 * Class that represents all the parts of a Proxy pattern.
 */
public class ProxyPatternGroup {

    private final ClassOrInterfaceDeclaration interfaceOrAClass;
    private final ClassOrInterfaceDeclaration subject;
    private final ClassOrInterfaceDeclaration proxy;

    private final List<MethodGroup> methods;

    private ProxyPatternGroup(
        ClassOrInterfaceDeclaration interfaceOrAClass, ClassOrInterfaceDeclaration subject,
        ClassOrInterfaceDeclaration proxy, List<MethodGroup> methods) {

        this.interfaceOrAClass = interfaceOrAClass;
        this.subject = subject;
        this.proxy = proxy;
        this.methods = methods;
    }

    /**
     * Get a new ProxyPatternGroup representing only the interface, the subject and their methods.
     *
     * @param interfaceOrAClass the interface / abstract class.
     * @param subject           the subject class.
     * @param methodGroups      the methodGroups.
     *
     * @return a new ProxyPatternGroup
     */
    public static ProxyPatternGroup getInterfaceSubjectGroup(
        ClassOrInterfaceDeclaration interfaceOrAClass, ClassOrInterfaceDeclaration subject,
        List<MethodGroup> methodGroups) {
        return new ProxyPatternGroup(interfaceOrAClass, subject, null, methodGroups);
    }

    /**
     * Get a new ProxyPatternGroup representing the interface, the subject, the proxy and their
     * methods.
     *
     * @param oldGroup        A ProxyPatternGroup containing the interface, the subject and their
     *                        methods.
     * @param newMethodGroups the new methodGroups (i.e. with the proxy methods added)
     *
     * @return a new ProxyPatternGroup
     */
    public static ProxyPatternGroup getWithProxy(
        ProxyPatternGroup oldGroup, List<MethodGroup> newMethodGroups,
        ClassOrInterfaceDeclaration proxy) {

        return new ProxyPatternGroup(oldGroup.getInterfaceOrAClass(), oldGroup.getSubject(), proxy,
                                     newMethodGroups);
    }

    public ClassOrInterfaceDeclaration getInterfaceOrAClass() {
        return interfaceOrAClass;
    }

    public ClassOrInterfaceDeclaration getSubject() {
        return subject;
    }

    public ClassOrInterfaceDeclaration getProxy() {
        return proxy;
    }

    public List<MethodGroup> getMethods() {
        return methods;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Proxy pattern containing: ");
        stringBuilder.append("proxy: ").append(proxy.getNameAsString());
        stringBuilder.append(", interface: ").append(interfaceOrAClass.getNameAsString());
        stringBuilder.append(", subject: ").append(subject.getNameAsString());

        return stringBuilder.toString();
    }
}
