package tool.designpatterns.verifiers.multiclassverifiers.compositeverifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;

import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
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
        for (ClassOrInterfaceDeclaration node : patternInstance.getNodes()) {
            FeedbackWrapper<List<FieldDeclaration>> collectionFields = getCollectionFieldsOfType(
                node, patternInstance.getComponent());
            if (collectionFields.getOther().isEmpty()) {
                feedbacks.add(collectionFields.getFeedback());
            } else {
                feedbacks.add(Feedback.getSuccessfulFeedback());
                feedbacks.add(delegatesToCollection(node, patternInstance.getComponent()));
            }
        }
        return feedbacks;
    }

    private Feedback componentHasMethods(ClassOrInterfaceDeclaration component) {
        return Feedback.getNoChildFeedback("har inte implementerat det här ännu",
                                           new FeedbackTrace(component));
    }

    private FeedbackWrapper<List<FieldDeclaration>> getCollectionFieldsOfType(
        ClassOrInterfaceDeclaration node, ClassOrInterfaceDeclaration componentType) {
        List<FieldDeclaration> fieldNames = new ArrayList<>();

        for (FieldDeclaration field : VariableReader.readVariables(node)) {
            for (ResolvedReferenceType type : field.getElementType().resolve().asReferenceType()
                                                   .getAllAncestors()) {
                if (type.getQualifiedName().equals("java.util.Collection")) {
                    for (var pair : type.getTypeParametersMap()) {
                        if (pair.b.describe().equals(componentType.getFullyQualifiedName().get())) {
                            fieldNames.add(field);
                        }
                    }
                }
            }
        }
        Feedback feedback;
        if (fieldNames.size() > 0) {
            feedback = Feedback.getSuccessfulFeedback();
        } else {
            feedback = Feedback.getNoChildFeedback(
                "The container has no Collection with the " + "component as parameter",
                new FeedbackTrace(node));
        }
        return new FeedbackWrapper<>(feedback, fieldNames);
    }

    /**
     * Checks that a method delegates the call to at least one child of the type componentType
     *
     * @param node          The node that is being checked
     * @param componentType The type of the children
     *
     * @return The result of the validation of the predicate
     */
    private Feedback delegatesToCollection(
        ClassOrInterfaceDeclaration node, ClassOrInterfaceDeclaration componentType) {
        List<Feedback> responses = new ArrayList<>();
        node.findAll(MethodDeclaration.class).forEach(e -> {
            if (methodBelongsToComponent(e, componentType)) {
                Feedback f = e.accept(new LoopVisitor(), e);
                if (f == null) {
                    responses.add(Feedback.getNoChildFeedback(
                        "There is no iterating block in container", new FeedbackTrace(e)));
                } else {
                    responses.add(f);
                }
            }
        });

        return Feedback.getFeedbackWithChildren(new FeedbackTrace(node), responses);
    }

    private boolean methodBelongsToComponent(
        MethodDeclaration method, ClassOrInterfaceDeclaration component) {
        for (MethodDeclaration e : component.getMethods()) {
            if (isSameMethod(e, method)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameMethod(MethodDeclaration m1, MethodDeclaration m2) {
        boolean hasSameName = m1.getName().equals(m2.getName());
        boolean hasSameParameters = m1.getParameters().equals(m2.getParameters());
        return hasSameName && hasSameParameters;
    }

    private boolean isSameMethod(MethodDeclaration m1, MethodCallExpr m2) {
        boolean hasSameName = m1.getName().equals(m2.getName());
        boolean hasSameParameters = m1.getParameters().equals(m2.getArguments());
        return hasSameName && hasSameParameters;
    }

    private class LoopVisitor extends GenericVisitorAdapter<Feedback, MethodDeclaration> {

        @Override
        public Feedback visit(
            ForStmt n, MethodDeclaration arg) {
            super.visit(n, arg);
            return verifyLoop(n.getBody(), arg);
        }

        @Override
        public Feedback visit(
            WhileStmt n, MethodDeclaration arg) {
            super.visit(n, arg);
            return verifyLoop(n.getBody(), arg);
        }

        @Override
        public Feedback visit(
            DoStmt n, MethodDeclaration arg) {
            super.visit(n, arg);
            return verifyLoop(n.getBody(), arg);
        }

        @Override
        public Feedback visit(
            ForEachStmt n, MethodDeclaration arg) {
            super.visit(n, arg);
            return verifyLoop(n.getBody(), arg);
        }

        @Override
        public Feedback visit(
            MethodCallExpr n, MethodDeclaration arg) {
            super.visit(n, arg);
            if (n.getNameAsString().equalsIgnoreCase("forEach")) {
                return verifyLoop(n.getArgument(0), arg);
            }
            return Feedback.getNoChildFeedback(
                "Container method does not contain forEach " + "statement", new FeedbackTrace(arg));
        }

        private Feedback verifyLoop(Visitable loopStmt, MethodDeclaration parentMethod) {
            ComponentMethodVisitor visitor = new ComponentMethodVisitor();
            loopStmt.accept(visitor, parentMethod);
            if (visitor.getDoesDelegate().stream().anyMatch(e -> e)) {
                return Feedback.getSuccessfulFeedback();
            } else {
                return Feedback.getNoChildFeedback("Container method does not delegate method call",
                                                   new FeedbackTrace(parentMethod));
            }
        }
    }


    private class ComponentMethodVisitor extends VoidVisitorAdapter<MethodDeclaration> {

        private List<Boolean> doesDelegate;

        private ComponentMethodVisitor() {
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
                JavaParserMethodDeclaration m = (JavaParserMethodDeclaration) methodCall.resolve();
                doesDelegate.add(isSameMethod(m.getWrappedNode(), parentMethod));
            } catch (ClassCastException exception) {
                doesDelegate.add(Boolean.FALSE);
            }
        }
    }

}
