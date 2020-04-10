package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static tool.designpatterns.verifiers.multiclassverifiers.proxy.ProxyFeedbackUtils.allClassesAreUsed;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.InterfaceMethods;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.PartialProxyImplementation;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.datahelpers.ProxyPatternGroup;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.visitors.MethodDeclarationVisitor;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.FeedbackWrapper;
import tool.feedback.PatternGroupFeedback;

/**
 * Verifies proxy pattern groups in the given map.
 */
public class ProxyVerifier implements IPatternGrouper {

    public ProxyVerifier() {

    }

    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {

        List<Feedback> feedbacks = new ArrayList<>();

        // Verify the interfaces.
        List<ClassOrInterfaceDeclaration> interfaces = map.get(Pattern.PROXY_INTERFACE);
        if (interfaces == null || interfaces.isEmpty()) {
            feedbacks.add(Feedback.getPatternInstanceNoChildFeedback(
                "Unable to find any " + Pattern.PROXY_INTERFACE.toString() + "annotations"));
            return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
        }

        List<Feedback> interfaceFeedbacks = new ArrayList<>();

        List<InterfaceMethods> interfaceMethodList = new ArrayList<>();
        interfaces.forEach(interfaceOrAClass -> {
            FeedbackWrapper<List<MethodDeclaration>> interfaceMethods = getValidMethods(
                interfaceOrAClass);
            interfaceFeedbacks.add(interfaceMethods.getFeedback());
            interfaceMethodList.add(
                new InterfaceMethods(interfaceOrAClass, interfaceMethods.getOther()));
        });
        feedbacks.add(Feedback.getPatternInstanceFeedback(interfaceFeedbacks));

        // Verify the subjects
        List<ClassOrInterfaceDeclaration> subjects = map.get(Pattern.PROXY_SUBJECT);
        List<PartialProxyImplementation> subjectGroups = PartialProxyVerifier.verifyImplementors(
            interfaceMethodList, subjects);

        // Verify the proxies
        List<ClassOrInterfaceDeclaration> proxies = map.get(Pattern.PROXY_PROXY);
        List<PartialProxyImplementation> proxyGroups = PartialProxyVerifier.verifyImplementors(
            interfaceMethodList, proxies);

        // Merge and verify the parts.
        FeedbackWrapper<List<ProxyPatternGroup>> proxyPatterns = ProxyGroupVerifier.verifyParts(
            subjectGroups, proxyGroups);
        feedbacks.add(proxyPatterns.getFeedback());

        // Validate that all classes marked as parts of a Proxy pattern are used at least once.
        feedbacks.add(
            allClassesAreUsed(proxyPatterns.getOther(), proxies, interfaces, subjects, proxyGroups,
                subjectGroups));

        return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
    }

    /**
     * Returns the valid methods found in the interface or abstract class provided.
     *
     * @param classOrI the interface or abstract class to look in.
     *
     * @return the feedback result as well as a list of the valid MethodDeclarations found.
     */
    private FeedbackWrapper<List<MethodDeclaration>> getValidMethods(
        ClassOrInterfaceDeclaration classOrI) {

        MethodDeclarationVisitor mdv = new MethodDeclarationVisitor();
        List<MethodDeclaration> methodDeclarations = classOrI.accept(mdv, classOrI);
        List<MethodDeclaration> validMethodDecs = new ArrayList<>();

        if (methodDeclarations.isEmpty()) {
            String message = "There are no methods to implement.";
            return new FeedbackWrapper<>(
                Feedback.getNoChildFeedback(message, new FeedbackTrace(classOrI)),
                methodDeclarations);
        } else if (classOrI.isInterface()) {
            validMethodDecs.addAll(methodDeclarations);
        } else {
            for (MethodDeclaration md : classOrI.accept(mdv, classOrI)) {
                if (md.isAbstract()) {
                    validMethodDecs.add(md);
                }
            }
            if (validMethodDecs.isEmpty()) {
                String message = "There are no abstract methods to implement.";
                return new FeedbackWrapper<>(
                    Feedback.getNoChildFeedback(message, new FeedbackTrace(classOrI)),
                    methodDeclarations);
            }

        }

        return new FeedbackWrapper<>(Feedback.getSuccessfulFeedback(), validMethodDecs);
    }
}