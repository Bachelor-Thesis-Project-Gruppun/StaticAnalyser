package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tool.designpatterns.verifiers.multiclassverifiers.proxy.ProxyProxyVerifier.verifyProxies;
import static tool.designpatterns.verifiers.multiclassverifiers.proxy.ProxySubjectVerifier.verifySubjects;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;

import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import org.apache.commons.lang.NotImplementedException;
import tool.designpatterns.Pattern;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyPatternGroup;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
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

        MethodDeclarationVisitor mdv = new MethodDeclarationVisitor(classOrI);
        List<MethodDeclaration> methodDeclarations = classOrI.accept(mdv, classOrI);
        List<MethodDeclaration> validMethodDeclarations = new ArrayList<>();

        if(methodDeclarations.isEmpty()){
            String message = "There are no methods to implement.";
            return new Tuple2<>(Feedback.getNoChildFeedback(message, new FeedbackTrace(classOrI)),
                                methodDeclarations);
        } else if(classOrI.isInterface()) {
            validMethodDeclarations.addAll(methodDeclarations);
            String message = "";
        } else {
            for (MethodDeclaration md : classOrI.accept(mdv, classOrI)) {
                if (md.isAbstract()){
                    validMethodDeclarations.add(md);
                }
            }
            if(validMethodDeclarations.isEmpty()){
                String message = "There are no abstract methods to implement.";
                return new Tuple2<>(
                    Feedback.getNoChildFeedback(message, new FeedbackTrace(classOrI)),
                    methodDeclarations);
            }

        }

        return new Tuple2<>(Feedback.getSuccessfulFeedback(), validMethodDeclarations);
    }

    /**
     * A class used to visit nodes in a AST created by JavaParser.
     */
    private static class MethodDeclarationVisitor
        extends GenericListVisitorAdapter<MethodDeclaration, ClassOrInterfaceDeclaration> {

        public MethodDeclarationVisitor(ClassOrInterfaceDeclaration currentClass) {
            super();
        }

        /**
         * Visit all MethodDeclarations in a ClassOrInterfaceDeclaration and add them to a list.
         *
         * @param method the Method currently being visited.
         * @param classOrI the ClassOrInterfaceDeclaration the method is declared in.
         *
         * @return a list of MethodDeclarations present in the ClassOrInterfaceDeclaration.
         */
        @Override
        public List<MethodDeclaration> visit(
            MethodDeclaration method, ClassOrInterfaceDeclaration classOrI) {
            List<MethodDeclaration> resultList = super.visit(method, classOrI);
            resultList.add(method);
            return resultList;
        }

    }
}
