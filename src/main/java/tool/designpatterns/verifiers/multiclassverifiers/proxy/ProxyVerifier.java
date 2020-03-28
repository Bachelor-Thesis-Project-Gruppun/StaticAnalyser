package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tool.designpatterns.verifiers.multiclassverifiers.proxy.ProxyProxyVerifier.verifyProxys;
import static tool.designpatterns.verifiers.multiclassverifiers.proxy.ProxySubjectVerifier.verifySubjects;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import groovy.lang.Tuple2;
import org.apache.commons.lang.NotImplementedException;
import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.helpers.InterfaceSubjectTuple;
import tool.feedback.Feedback;
import tool.feedback.FeedbackWrapper;
import tool.feedback.PatternGroupFeedback;

public class ProxyVerifier implements IPatternGrouper {

    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {

        List<Feedback> feedbacks = new ArrayList<>();
        List<Feedback> interfaceFeedbacks = new ArrayList<>();
        Map<ClassOrInterfaceDeclaration, List<MethodDeclaration>> interfaceMethodMap =
            new HashMap<>();

        // Verify the interfaces.
        map.get(Pattern.PROXY_INTERFACE).forEach(interfaceOrAClass -> {
            Tuple2<Feedback, List<MethodDeclaration>> interfaceMethods = getValidMethods(
                interfaceOrAClass);
            interfaceFeedbacks.add(interfaceMethods.getFirst());
            interfaceMethodMap.put(interfaceOrAClass, interfaceMethods.getSecond());
        });

        // Verify the subjects
        List<ClassOrInterfaceDeclaration> subjects = map.get(Pattern.PROXY_SUBJECT);
        FeedbackWrapper<List<InterfaceSubjectTuple>> interfaceSubjects = verifySubjects(
            interfaceMethodMap, subjects);

        // Verify the proxys
        List<ClassOrInterfaceDeclaration> proxys = map.get(Pattern.PROXY_PROXY);
        Feedback proxyFeedback = verifyProxys(proxys, interfaceSubjects.getOther());

        // 1. Gå igenom alla "Pattern.PROXY_INTERFACE":
        //    1b. hämta giltiga metoder.
        // 1c. Kolla så att alla interfaces används.
        // 2. För alla giltiga metoder:
        //    2b. Gå igenom alla "Pattern.PROXY_SUBJECT":
        //        2c. Kolla att den implementerar metoden (/ inhertetar från interfacet).
        //        2d. Gå igenom alla Pattern.PROXY_PROXY"
        //            2e. Kolla att den också har metoden (/ inhertetar från interfacet).
        //            2f. Kolla att den har en privat variable av typ SUBJECT.
        //            2g. Kolla att SUBJECTs metod kallas i metoden.
        //
        // 3. Kolla så att alla subjects har en interface och är valid.
        // 3b. Kolla så att alla proxys har en interface och en subject och är valid.

        return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
    }

    // Steps 1 / 1b -- nemo
    private Tuple2<Feedback, List<MethodDeclaration>> getValidMethods(
        ClassOrInterfaceDeclaration classOrI) {

        throw new NotImplementedException();
    }
}
