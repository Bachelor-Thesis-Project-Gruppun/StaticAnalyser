package tool.feedback;

import java.util.List;

import tool.designpatterns.PatternGroup;

/**
 * The feedback for an entire patternGroup.
 */
public class PatternGroupFeedback {

    private PatternGroup patternGroup;
    private List<Feedback> feedbacks;

    public PatternGroupFeedback(
        PatternGroup patternGroup, List<Feedback> feedbacks) {
        this.patternGroup = patternGroup;
        this.feedbacks = feedbacks;
    }

    public String getFullMessage() {

        StringBuilder message = new StringBuilder("Verification of patternGroup ");
        message.append(patternGroup.toString());

        if (hasError()) {
            message.append(" failed due to:");
        } else {
            message.append(" was successful");
        }

        for (Feedback childFeedback : feedbacks) {
            message.append(childFeedback.getFullMessage());
            message.append('\n');
        }

        return message.toString();
    }

    public boolean hasError() {
        boolean hasError = false;
        for (Feedback feedback : feedbacks) {
            if (feedback.getIsError()) {
                hasError = true;
            }
        }

        return hasError;
    }
}
