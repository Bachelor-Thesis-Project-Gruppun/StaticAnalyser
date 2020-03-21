package patternverifiers;

import java.util.List;

import base.PatternGroup;

/**
 * The feedback for an entire patternGroup.
 */
public class PatternGroupFeedback {

    private PatternGroup patternGroup;
    private String message;
    private List<Feedback> feedbacks;

    public PatternGroupFeedback(
        PatternGroup patternGroup, String message, List<Feedback> feedbacks) {
        this.message = message;
        this.feedbacks = feedbacks;
    }

    public String getMessage() {
        return message;
    }

    public String getFullMessage() {
        boolean verifySuccesful = true;

        StringBuilder baseMsg = new StringBuilder("Verification of patternGroup");
        baseMsg.append(patternGroup.toString());
        StringBuilder message = new StringBuilder();

        if (verifySuccesful) {
            baseMsg.append(" was successful");
        } else {
            baseMsg.append(" failed due to");
            baseMsg.append(message);
        }

        return message;
    }
}
