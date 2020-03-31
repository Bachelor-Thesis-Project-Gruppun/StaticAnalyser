package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import tool.designpatterns.verifiers.multiclassverifiers.proxy.visitors.MethodDeclarationVisitor;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.FeedbackWrapper;

/**
 * A class containing useful helper methods when verifying methods.
 */
public class MethodVerification {

    // Steps 2c / 2e -- nemo
    static FeedbackWrapper<MethodDeclaration> classImplementsMethod(
        ClassOrInterfaceDeclaration theClass, ClassOrInterfaceDeclaration theInterface,
        MethodDeclaration method) {

        MethodDeclarationVisitor mdv = new MethodDeclarationVisitor();
        List<MethodDeclaration> implementedMethods = theClass.accept(mdv, theClass);
        List<MethodDeclaration> interfaceMethods = theInterface.accept(mdv, theInterface);
        boolean isInClass = false;
        boolean isInInterface = false;
        MethodDeclaration classDeclaration  = new MethodDeclaration();

        if(implementedMethods.isEmpty()){
            String message = "There are no method declarations in the class";
            return new FeedbackWrapper<>(Feedback.getNoChildFeedback(message,
                                                                     new FeedbackTrace(theInterface)), null);
        } else if (interfaceMethods.isEmpty()) {
            String message = "There are no method declarations in the interface";
            return new FeedbackWrapper<>(Feedback.getNoChildFeedback(message,
                                                                     new FeedbackTrace(theInterface)), null);
        } else if (method == null){
            String message = "No such method";
            return new FeedbackWrapper<>(Feedback.getNoChildFeedback(message,
                                                                     new FeedbackTrace(theInterface)), null);
        }

        for ( MethodDeclaration md : implementedMethods ) {
            if(md.getNameAsString().equalsIgnoreCase(method.getNameAsString())) {
                classDeclaration = md;
                isInClass = true;
                break;
            }
        }

        for ( MethodDeclaration md : interfaceMethods) {
            if(md.getNameAsString().equalsIgnoreCase(method.getNameAsString())) {
                isInInterface = true;
                break;
            }
        }

        if(isInClass && isInInterface){
            return new FeedbackWrapper<>(Feedback.getSuccessfulFeedback(), classDeclaration);
        }


        String message = "The method is not implemented in the class or the interface";
        return new FeedbackWrapper<>(Feedback.getNoChildFeedback(message,
                                                                 new FeedbackTrace(theInterface)), null);
    }

}
