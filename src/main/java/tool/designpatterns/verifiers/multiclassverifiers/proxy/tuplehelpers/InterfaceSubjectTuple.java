package tool.designpatterns.verifiers.multiclassverifiers.proxy.tuplehelpers;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Simple tuple4 to group an interface, a subject, an interfaceMethod and the subjectMethod that
 * implements it together.
 */
public class InterfaceSubjectTuple {

    private final ClassOrInterfaceDeclaration interfaceOrAClass;
    private final MethodDeclaration interfaceMethod;
    private final ClassOrInterfaceDeclaration subject;
    private final MethodDeclaration subjectMethod;

    public InterfaceSubjectTuple(
        ClassOrInterfaceDeclaration interfaceOrAClass, MethodDeclaration interfaceMethod,
        ClassOrInterfaceDeclaration subject, MethodDeclaration subjectMethod) {
        this.interfaceOrAClass = interfaceOrAClass;
        this.interfaceMethod = interfaceMethod;
        this.subject = subject;
        this.subjectMethod = subjectMethod;
    }

    public ClassOrInterfaceDeclaration getInterfaceOrAClass() {
        return interfaceOrAClass;
    }

    public MethodDeclaration getInterfaceMethod() {
        return interfaceMethod;
    }

    public ClassOrInterfaceDeclaration getSubject() {
        return subject;
    }

    public MethodDeclaration getSubjectMethod() {
        return subjectMethod;
    }
}