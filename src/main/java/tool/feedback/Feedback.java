package tool.feedback;

import java.util.ArrayList;
import java.util.List;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

/**
 * Simple class to store and manage feedback to the user.
 */
@DesignPattern(pattern = {Pattern.IMMUTABLE})
public final class Feedback {

    private static final String LINE_PREFIX = "  ";
    private final String message;
    private final List<Feedback> children;
    private final FeedbackTrace stackTrace;
    private boolean isError;

    private Feedback(
        boolean isError, String message, FeedbackTrace stackTrace, List<Feedback> children) {
        this.isError = isError;
        this.message = message;
        this.stackTrace = stackTrace;
        this.children = new ArrayList<>();

        if (children != null) {
            for (Feedback child : children) {
                if (child != null && child.getIsError()) {
                    this.isError = true;
                    this.children.add(child);
                }
            }
        }
    }

    /**
     * Get a new feedback that is an error with the given message and stacktrace. It does not have
     * any children and is therefore the leaf feedback.
     *
     * @param message    what caused the error?
     * @param stackTrace the element where the error occured.
     *
     * @return the new feedback.
     */
    public static Feedback getNoChildFeedback(
        String message, FeedbackTrace stackTrace) {
        return new Feedback(true, message, stackTrace, null);
    }

    /**
     * Get a new feedback that has children. Calls the constructor which will in term determine
     * wether this is an error or not by looking at if the children are errors or not.
     *
     * @param stackTrace the element where the error occured.
     * @param children   the child feedback elements to this feedback.
     *
     * @return a new feedback.
     */
    public static Feedback getFeedbackWithChildren(
        FeedbackTrace stackTrace, List<Feedback> children) {
        if (children == null) {
            throw new IllegalArgumentException("Child list must not be null.");
        }

        return new Feedback(false, "", stackTrace, children);
    }

    /**
     * Returns a new feedback that is an error and that has no children and no stacktrace (as it is
     * an error with higher-order elements than classes such as relationships between classes).
     *
     * @param message the error message.
     *
     * @return the new feedback.
     */
    public static Feedback getPatternInstanceNoChildFeedback(String message) {
        return new Feedback(true, message, null, null);
    }

    /**
     * Get a feedback representing the entire verification of a Pattern.
     *
     * @param children the feedbacks of each of the verification classes. For single-class patterns
     *                 this list will only contain 1 element. For multi-class patterns this will
     *                 contain multiple.
     */
    public static Feedback getPatternInstanceFeedback(List<Feedback> children) {
        return new Feedback(false, "", null, children);
    }

    /**
     * Get a new feedback that is not an error.
     *
     * @return a feedback that is not an error.
     */
    public static Feedback getSuccessfulFeedback() {
        return new Feedback(false, "", null, null);
    }

    @SuppressWarnings("PMD.BooleanGetMethodName") // If anyone
    public boolean getIsError() {
        return this.isError;
    }

    /**
     * Returns the message specified..
     *
     * @return the message.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Returns the full stackTrace with messages.
     *
     * @return full message with stackTrace.
     */
    public String getFullMessage() {
        return getFullMessage(LINE_PREFIX);
    }

    private String getFullMessage(String linePrefix) {
        // We only want a message if it was an error.
        if (!this.isError) {
            return "";
        }

        StringBuilder message = new StringBuilder();
        StringBuilder childPrefix = new StringBuilder(linePrefix);
        if (this.stackTrace != null) {
            message.append(linePrefix);
            message.append(this.stackTrace.toString());
            message.append(" : ");
            childPrefix.append(LINE_PREFIX);
        }

        if (this.children.isEmpty()) {
            // We don't have any children, i.e. we're the 'leaf' node and therefore we print our
            // message.
            if (this.stackTrace == null) {
                message.append(linePrefix);
            }

            message.append(getMessage()).append('\n');
        } else if (this.stackTrace != null) {
            message.append('\n');
        }

        if (!this.children.isEmpty()) {
            // We want to print all of our children in a nice way.
            for (Feedback child : this.children) {
                message.append(child.getFullMessage(childPrefix.toString()));
            }
        }

        return message.toString();
    }
}
