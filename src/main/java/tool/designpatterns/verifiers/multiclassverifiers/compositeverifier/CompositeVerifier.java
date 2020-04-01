package tool.designpatterns.verifiers.multiclassverifiers.compositeverifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.FeedbackWrapper;
import tool.feedback.PatternGroupFeedback;

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
            FeedbackWrapper<List<String>> collectionFields = getCollectionFieldsOfType(node,
                                                                                       patternInstance
                                                                                           .getComponent());
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

    private FeedbackWrapper<List<String>> getCollectionFieldsOfType(
        ClassOrInterfaceDeclaration node, ClassOrInterfaceDeclaration componentType) {
        List<String> fieldNames = new ArrayList<>();
        Feedback feedback = Feedback.getNoChildFeedback(
            "har inte implementerat det här ännu", new FeedbackTrace(node));

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
        return Feedback.getNoChildFeedback("finns inget fält med typen collection och nåt mer",
                                           new FeedbackTrace(node));
    }
}
