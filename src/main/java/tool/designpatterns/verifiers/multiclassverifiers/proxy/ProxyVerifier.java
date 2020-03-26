package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import groovy.lang.Tuple2;
import org.apache.commons.lang.NotImplementedException;
import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.feedback.Feedback;
import tool.feedback.PatternGroupFeedback;

public class ProxyVerifier implements IPatternGrouper {

    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {

        List<Feedback> feedbacks = new ArrayList<>();
        // 1. Gå igenom alla "Pattern.PROXY_INTERFACE":
        //    1b. hämta giltiga metoder.
        // 2. För alla giltiga metoder:
        //    2b. Gå igenom alla "Pattern.PROXY_SUBJECT":
        //        2c. Kolla att den implementerar metoden (/ inhertetar från interfacet).
        //        2d. Gå igenom alla Pattern.PROXY_PROXY"
        //            2e. Kolla att den också har metoden (/ inhertetar från interfacet).
        //            2f. Kolla att den har en privat variable av typ SUBJECT.
        //            2g. Kolla att SUBJECTs metod kallas i metoden.

        return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
    }

    // Steps 1 / 1b -- nemo
    private Tuple2<Feedback, List<MethodDeclaration>> getValidMethods(
        ClassOrInterfaceDeclaration classOrI) {

        throw new NotImplementedException();
    }

    // Step 2 / 2b (also 2* but uses verifyProxy && classImplementsMethod for this) -- vidde
    private Feedback verifyImplementors(
        Map<ClassOrInterfaceDeclaration, List<MethodDeclaration>> interfaceMethods,
        List<ClassOrInterfaceDeclaration> subjects, List<ClassOrInterfaceDeclaration> proxys) {

        throw new NotImplementedException();
    }

    // 2d / 2f / 2g (also 2e but uses ClassImplementsMethod) -- vidde
    private Feedback verifyProxy(
        List<ClassOrInterfaceDeclaration> proxys, MethodDeclaration method,
        ClassOrInterfaceDeclaration subject, MethodDeclaration subjectMethod) {

        throw new NotImplementedException();
    }

    // Steps 2c / 2e -- nemo
    private Feedback classImplementsMethod(
        ClassOrInterfaceDeclaration theClass, ClassOrInterfaceDeclaration theInterface,
        MethodDeclaration method) {
        throw new NotImplementedException();
    }
}
