package tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers;

import java.util.ArrayList;
import java.util.List;

import static tool.designpatterns.verifiers.multiclassverifiers.proxy.ClassVerification.isSameClassOrInterfaceDeclaration;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.designpatterns.Pattern;
import tool.feedback.Feedback;

/**
 * Class that represents all the parts of a Proxy pattern.
 */
@SuppressWarnings("PMD.CommentSize")
public final class ProxyPatternGroup {

    private final ClassOrInterfaceDeclaration interfaceOrAClass;
    private final ClassOrInterfaceDeclaration subject;
    private final ClassOrInterfaceDeclaration proxy;

    private final List<MethodGroup> methods;

    private final List<Feedback> potentialErrors;

    private ProxyPatternGroup(
        ClassOrInterfaceDeclaration interfaceOrAClass, ClassOrInterfaceDeclaration subject,
        ClassOrInterfaceDeclaration proxy, List<MethodGroup> methods,
        List<Feedback> potentialErrors) {

        this.interfaceOrAClass = interfaceOrAClass;
        this.subject = subject;
        this.proxy = proxy;
        this.methods = methods;
        if (potentialErrors == null) {
            this.potentialErrors = new ArrayList<>();
        } else {
            this.potentialErrors = potentialErrors;
        }
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
    @Deprecated
    public static ProxyPatternGroup getInterfaceSubjectGroup(
        ClassOrInterfaceDeclaration interfaceOrAClass, ClassOrInterfaceDeclaration subject,
        List<MethodGroup> methodGroups) {
        return new ProxyPatternGroup(interfaceOrAClass, subject, null, methodGroups, null);
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
    @Deprecated
    public static ProxyPatternGroup getWithProxy(
        ProxyPatternGroup oldGroup, List<MethodGroup> newMethodGroups,
        ClassOrInterfaceDeclaration proxy) {

        return new ProxyPatternGroup(oldGroup.getInterfaceOrAClass(), oldGroup.getSubject(), proxy,
            newMethodGroups, null);
    }

    /**
     * Get a new valid ProxyPatternGroup from the given parts.
     *
     * @param interfaceOrAClass the interface or abstract class in the group.
     * @param subject           the subject class.
     * @param proxy             the proxy class.
     * @param methodGroups      the proxy methods.
     *
     * @return a new valid ProxyPatternGroup.
     */
    public static ProxyPatternGroup getValidProxyGroup(
        ClassOrInterfaceDeclaration interfaceOrAClass, ClassOrInterfaceDeclaration subject,
        ClassOrInterfaceDeclaration proxy, List<MethodGroup> methodGroups) {
        return new ProxyPatternGroup(interfaceOrAClass, subject, proxy, methodGroups, null);
    }

    /**
     * Get a new invalid ProxyPatternGroup from the given parts, i.e. a ProxyPatternGroup that is
     * identified as a ProxyPatternGroup but that has some potential errors.
     *
     * @param interfaceOrAClass the interface or abstract class in the group.
     * @param subject           the subject class.
     * @param proxy             the proxy class.
     * @param methodGroups      the proxy methods.
     * @param potentialErrors   the potential errors that makes this not a completely valid
     *                          ProxyPatternGroup.
     *
     * @return a new invalid ProxyPatternGroup.
     */
    public static ProxyPatternGroup getInvalidProxyGroup(
        ClassOrInterfaceDeclaration interfaceOrAClass, ClassOrInterfaceDeclaration subject,
        ClassOrInterfaceDeclaration proxy, List<MethodGroup> methodGroups,
        List<Feedback> potentialErrors) {
        return new ProxyPatternGroup(
            interfaceOrAClass, subject, proxy, methodGroups, potentialErrors);
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

    /**
     * Returns a ClassOrInterfaceDeclaration depending on the pattern given.
     *
     * @param pattern the pattern to return the class for.
     *
     * @return the ClassOrInterfaceDeclaration for the pattern or null otherwise.
     */
    public ClassOrInterfaceDeclaration getFromPattern(Pattern pattern) {
        switch (pattern) {
            case PROXY_INTERFACE:
                return getInterfaceOrAClass();
            case PROXY_PROXY:
                return getProxy();
            case PROXY_SUBJECT:
                return getSubject();
            default:
                return null;
        }
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder(57);
        stringBuilder.append("Proxy pattern containing: proxy: ").append(proxy.getNameAsString())
                     .append(", interface: ").append(interfaceOrAClass.getNameAsString()).append(
            ", subject: ").append(subject.getNameAsString());

        return stringBuilder.toString();
    }

    /**
     * Returns if this a valid ProxyPatternGroup or not.
     *
     * @return true if this is a valid ProxyPatternGroup and false otherwise.
     */
    public boolean isValid() {
        return potentialErrors.isEmpty();
    }

    public List<Feedback> getPotentialErrors() {
        return potentialErrors;
    }

    /**
     * Checks if the given class is a part of this pattern group.
     *
     * @param classOrI the class or interface to check for.
     *
     * @return true if the class is used and false otherwise.
     */
    public boolean usesClass(ClassOrInterfaceDeclaration classOrI) {
        return isSameClassOrInterfaceDeclaration(proxy, classOrI) ||
               isSameClassOrInterfaceDeclaration(interfaceOrAClass, classOrI) ||
               isSameClassOrInterfaceDeclaration(subject, classOrI);
    }
}
