package tool.designpatterns.verifiers;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.designpatterns.Pattern;
import tool.feedback.Feedback;

public abstract class APatternInstance {


    public abstract List<APatternInstance> createInstancesFromMap(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map);

    @SuppressWarnings("PMD.LinguisticNaming")
    public Feedback hasAllElements() {
        StringBuilder feedbackMessage = new StringBuilder(127);
        feedbackMessage.append("The following elements are missing: ");

        boolean errorOccurred = checkElements(feedbackMessage);

        if (errorOccurred) {
            // We know that the last two characters are ", " and we want to remove those.
            feedbackMessage.deleteCharAt(feedbackMessage.length() - 1);
            feedbackMessage.deleteCharAt(feedbackMessage.length() - 1);
            feedbackMessage.append('.');
            return Feedback.getPatternInstanceNoChildFeedback(feedbackMessage.toString());
        }

        return Feedback.getSuccessfulFeedback();
    }

    protected abstract boolean checkElements(StringBuilder feedbackMessage);

}
