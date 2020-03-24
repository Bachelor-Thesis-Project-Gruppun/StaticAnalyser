package tool.feedback;

import java.util.List;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;

/**
 * The feedback for an entire patternGroup.
 */
@DesignPattern(pattern = {Pattern.IMMUTABLE})
public final class PatternGroupFeedback {

    private final PatternGroup patternGroup;
    private final List<Feedback> feedbacks;

    /**
     * Creates a new PatternGroupFeedback for the given patternGroup and with the given child
     * feedbacks.
     *
     * @param patternGroup the patternGroup this represents the feedback for.
     * @param feedbacks    the feedbacks that is this groups children.
     */
    public PatternGroupFeedback(
        PatternGroup patternGroup, List<Feedback> feedbacks) {
        this.patternGroup = patternGroup;
        this.feedbacks = feedbacks;
    }

    /**
     * Get a full feedback message for this PatternGroup and it's children.
     *
     * @return the message.
     */
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

    /**
     * Returns true if this feedback or any of its children is an error.
     *
     * @return if this is an error.
     */
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
