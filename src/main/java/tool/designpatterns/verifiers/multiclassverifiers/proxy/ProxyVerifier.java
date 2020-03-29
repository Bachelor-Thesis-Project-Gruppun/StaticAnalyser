package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tool.designpatterns.verifiers.multiclassverifiers.proxy.ProxyProxyVerifier.verifyProxies;
import static tool.designpatterns.verifiers.multiclassverifiers.proxy.ProxySubjectVerifier.verifySubjects;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import groovy.lang.Tuple2;
import org.apache.commons.lang.NotImplementedException;
import tool.designpatterns.Pattern;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyPatternGroup;
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
        FeedbackWrapper<List<ProxyPatternGroup>> interfaceSubjects = verifySubjects(
            interfaceMethodMap, subjects);

        feedbacks.add(interfaceSubjects.getFeedback());

        // Verify the Proxies.
        List<ClassOrInterfaceDeclaration> proxys = map.get(Pattern.PROXY_PROXY);
        FeedbackWrapper<List<ProxyPatternGroup>> interfaceSubjectProxies = verifyProxies(
            interfaceSubjects.getOther(), proxys);

        // Validate that all classes marked as parts of a Proxy pattern are used at least once.
        throw new NotImplementedException();

        //        return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
    }

    // Steps 1 / 1b -- nemo
    private Tuple2<Feedback, List<MethodDeclaration>> getValidMethods(
        ClassOrInterfaceDeclaration classOrI) {

        throw new NotImplementedException();
    }
}
