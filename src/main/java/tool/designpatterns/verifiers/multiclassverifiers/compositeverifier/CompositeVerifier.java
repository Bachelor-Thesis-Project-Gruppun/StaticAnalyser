package tool.designpatterns.verifiers.multiclassverifiers.compositeverifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

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
        patternInstances.forEach(
            patternInstance -> results.add(verifyPatternInstance(patternInstance)));

        return new PatternGroupFeedback(PatternGroup.COMPOSITE, results);
    }

    /**
     * Verifies one instance of the composite pattern.
     *
     * @param patternInstance a pattern instance
     *
     * @return a feedback
     */
    public Feedback verifyPatternInstance(CompositePatternInstance patternInstance) {
        Feedback allElementsFeedback = patternInstance.hasAllElements();
        if (allElementsFeedback.getIsError()) {
            return allElementsFeedback;
        } else { // the instance is complete and the predicates may now be checked
            List<Feedback> childFeedbacks = verifyPredicates(patternInstance);
            return Feedback.getPatternInstanceFeedback(childFeedbacks);
        }
    }

    /**
     * This method checks all predicates and is only called if the PatternInstance.hasAllElements
     * return true.
     *
     * @param patternInstance a complete pattern instance
     *
     * @return feedbacks
     */
    private List<Feedback> verifyPredicates(CompositePatternInstance patternInstance) {
        List<Feedback> feedbacks = new ArrayList<>();
        feedbacks.add(componentHasMethods(patternInstance.getComponent()));
        for (ClassOrInterfaceDeclaration container : patternInstance.getContainers()) {
            FeedbackWrapper<List<FieldDeclaration>> collectionFields = getCollectionFieldsOfType(
                container, patternInstance.getComponent());
            if (collectionFields.getOther().isEmpty()) {
                feedbacks.add(collectionFields.getFeedback());
            } else {
                feedbacks.add(delegatesToCollection(container, patternInstance.getComponent()));
            }
        }
        return feedbacks;
    }

    /**
     * Composite is defined by its delegate structure so if there are no methods which can delegate
     * then it cannot be a composite.
     *
     * @param component the interface or abstract class defining the composite
     *
     * @return a feedback
     */
    private Feedback componentHasMethods(ClassOrInterfaceDeclaration component) {
        return VerifierUtils.hasAtLeastOnePublicMethod(component);
    }

    /**
     * A container has to a have a field which extends from Collection with a type parameter the
     * same as the Component.
     *
     * @param container     the class which has to the specified field
     * @param componentType define the type parameter of the collection.
     *
     * @return a feedback
     */
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
        LoopVisitor looper = new LoopVisitor();
        container.findAll(MethodDeclaration.class).forEach(methodInContainer -> {
            if (VerifierUtils.methodBelongsToComponent(methodInContainer, componentType)) {
                Feedback doesDelegateFeedback = methodInContainer.accept(looper, methodInContainer);
                responses.add(doesDelegateFeedback);
                responses.add(looper.hasIteratingBlock(methodInContainer));
                looper.resetIteratingBlocks();
            }
        });

        return Feedback.getFeedbackWithChildren(new FeedbackTrace(container), responses);
    }

}
