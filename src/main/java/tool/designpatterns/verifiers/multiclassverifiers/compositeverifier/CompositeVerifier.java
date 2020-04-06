package tool.designpatterns.verifiers.multiclassverifiers.compositeverifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;

import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.designpatterns.verifiers.VerifierUtils;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.FeedbackWrapper;
import tool.feedback.PatternGroupFeedback;
import tool.util.VariableReader;

/**
 * A verifier for the compsite pattern.
 */
public class CompositeVerifier implements IPatternGrouper {

    public CompositeVerifier() {
    }

    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {
        List<CompositePatternInstance> patternInstances =
            CompositePatternInstance.createInstancesFromMap(map);
        List<Feedback> results = new ArrayList<>();
        patternInstances.forEach(patternInstance -> {
            results.add(verifyPatternInstance(patternInstance));
        });

        return new PatternGroupFeedback(PatternGroup.COMPOSITE, results);
    }

    private Feedback verifyPatternInstance(CompositePatternInstance patternInstance) {
        Feedback allElementsFeedback = patternInstance.hasAllElements();
        if (allElementsFeedback.getIsError()) {
            return allElementsFeedback;
        } else { // the instance is complete and the predicates may now be checked
            List<Feedback> childFeedbacks = verifyPredicates(patternInstance);
            return Feedback.getPatternInstanceFeedback(childFeedbacks);
        }
    }

    private List<Feedback> verifyPredicates(CompositePatternInstance patternInstance) {
        List<Feedback> feedbacks = new ArrayList<>();
        feedbacks.add(componentHasMethods(patternInstance.getComponent()));
        for (ClassOrInterfaceDeclaration container : patternInstance.getContainers()) {
            FeedbackWrapper<List<FieldDeclaration>> collectionFields = getCollectionFieldsOfType(
                container, patternInstance.getComponent());
            if (collectionFields.getOther().isEmpty()) {
                feedbacks.add(collectionFields.getFeedback());
            } else {
                feedbacks.add(Feedback.getSuccessfulFeedback());
                feedbacks.add(delegatesToCollection(container, patternInstance.getComponent()));
            }
        }
        return feedbacks;
    }

    private Feedback componentHasMethods(ClassOrInterfaceDeclaration component) {
        return VerifierUtils.hasAtLeastOnePublicMethod(component);
    }

    private FeedbackWrapper<List<FieldDeclaration>> getCollectionFieldsOfType(
        ClassOrInterfaceDeclaration container, ClassOrInterfaceDeclaration componentType) {
        List<FieldDeclaration> fieldNames = new ArrayList<>();

        final String collectionType = "java.util.Collection";

        for (FieldDeclaration field : VariableReader.readVariables(container)) {
            for (ResolvedReferenceType type : field.getElementType().resolve().asReferenceType()
                                                   .getAllAncestors()) {
                if (type.getQualifiedName().equals(collectionType)) {
                    for (var pair : type.getTypeParametersMap()) {
                        if (pair.b.describe().equals(componentType.getFullyQualifiedName().get())) {
                            fieldNames.add(field);
                        }
                    }
                }
            }
        }
        Feedback feedback;
        if (fieldNames.isEmpty()) {
            feedback = Feedback.getNoChildFeedback(
                "The container has no Collection with the component as parameter",
                new FeedbackTrace(container));
        } else {
            feedback = Feedback.getSuccessfulFeedback();
        }
        return new FeedbackWrapper<>(feedback, fieldNames);
    }

    /**
     * Checks that a method delegates the call to at least one child of the type componentType.
     *
     * @param container     The container that is being checked
     * @param componentType The type of the children
     *
     * @return The result of the validation of the predicate
     */
    private Feedback delegatesToCollection(
        ClassOrInterfaceDeclaration container, ClassOrInterfaceDeclaration componentType) {
        List<Feedback> responses = new ArrayList<>();
        container.findAll(MethodDeclaration.class).forEach(methodInContainer -> {
            if (methodBelongsToComponent(methodInContainer, componentType)) {
                Feedback feedback = methodInContainer.accept(new LoopVisitor(), methodInContainer);
                if (feedback == null) {
                    responses.add(Feedback.getNoChildFeedback(
                        "There is no iterating block in container",
                        new FeedbackTrace(methodInContainer)));
                } else {
                    responses.add(feedback);
                }
            }
        });

        return Feedback.getFeedbackWithChildren(new FeedbackTrace(container), responses);
    }

    private boolean methodBelongsToComponent(
        MethodDeclaration method, ClassOrInterfaceDeclaration component) {
        for (MethodDeclaration methodInContainer : component.getMethods()) {
            Optional<AnnotationExpr> overrideAnn = method.getAnnotationByClass(Override.class);
            if (isSameMethod(methodInContainer, method) && overrideAnn.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameMethod(MethodDeclaration method1, MethodDeclaration method2) {
        boolean hasSameName = method1.getName().equals(method2.getName());
        boolean hasSameParameters = method1.getParameters().equals(method2.getParameters());
        return hasSameName && hasSameParameters;
    }

    /**
     * This Visitor finds all loops and then accepts the Visitor {@link ComponentMethodVisitor}.
     */
    private final class LoopVisitor extends GenericVisitorAdapter<Feedback, MethodDeclaration> {

        private LoopVisitor() {
            super();
        }

        @Override
        public Feedback visit(
            ForStmt loopStmt, MethodDeclaration parentMethod) {
            super.visit(loopStmt, parentMethod);
            return verifyLoop(loopStmt.getBody(), parentMethod);
        }

        @Override
        public Feedback visit(
            WhileStmt loopStmt, MethodDeclaration parentMethod) {
            super.visit(loopStmt, parentMethod);
            return verifyLoop(loopStmt.getBody(), parentMethod);
        }

        @Override
        public Feedback visit(
            DoStmt loopStmt, MethodDeclaration parentMethod) {
            super.visit(loopStmt, parentMethod);
            return verifyLoop(loopStmt.getBody(), parentMethod);
        }

        @Override
        public Feedback visit(
            ForEachStmt loopStmt, MethodDeclaration parentMethod) {
            super.visit(loopStmt, parentMethod);
            return verifyLoop(loopStmt.getBody(), parentMethod);
        }

        @Override
        public Feedback visit(
            MethodCallExpr loopFunc, MethodDeclaration parentMethod) {
            super.visit(loopFunc, parentMethod);
            if (loopFunc.getNameAsString().equalsIgnoreCase("forEach")) {
                return verifyLoop(loopFunc.getArgument(0), parentMethod);
            }
            return Feedback.getNoChildFeedback(
                "Container method does not contain forEach statement",
                new FeedbackTrace(parentMethod));
        }

        private Feedback verifyLoop(Visitable loop, MethodDeclaration parentMethod) {
            ComponentMethodVisitor visitor = new ComponentMethodVisitor();
            loop.accept(visitor, parentMethod);
            if (visitor.getDoesDelegate().stream().anyMatch(e -> e)) {
                return Feedback.getSuccessfulFeedback();
            } else {
                return Feedback.getNoChildFeedback(
                    "Container method does not delegate method call",
                    new FeedbackTrace(parentMethod));
            }
        }
    }


    /**
     * This visitor is used in The Class LoopVisitor and checks the method passed is ever called.
     */
    private class ComponentMethodVisitor extends VoidVisitorAdapter<MethodDeclaration> {

        private final List<Boolean> doesDelegate;

        private ComponentMethodVisitor() {
            super();
            this.doesDelegate = new ArrayList<>();
        }

        private List<Boolean> getDoesDelegate() {
            return doesDelegate;
        }

        @Override
        public void visit(
            MethodCallExpr methodCall, MethodDeclaration parentMethod) {
            super.visit(methodCall, parentMethod);
            try {
                JavaParserMethodDeclaration methodDeclaration =
                    (JavaParserMethodDeclaration) methodCall.resolve();
                doesDelegate.add(isSameMethod(methodDeclaration.getWrappedNode(), parentMethod));
            } catch (ClassCastException exception) {
                doesDelegate.add(Boolean.FALSE);
            }
        }


        @Override
        public void visit(
            MethodReferenceExpr methodRefernce, MethodDeclaration parentMethod) {
            super.visit(methodRefernce, parentMethod);
            try {
                JavaParserMethodDeclaration methodDeclaration =
                    (JavaParserMethodDeclaration) methodRefernce.resolve();
                doesDelegate.add(isSameMethod(methodDeclaration.getWrappedNode(), parentMethod));
            } catch (ClassCastException | UnsolvedSymbolException exception) {
                doesDelegate.add(Boolean.FALSE);
            }
        }

    }

}
