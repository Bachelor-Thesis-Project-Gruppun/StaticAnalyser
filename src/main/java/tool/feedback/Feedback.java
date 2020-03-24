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

    private boolean isError;
    private final String message;
    private final List<Feedback> children;
    private final FeedbackTrace stackTrace;

    private static final String LINE_PREFIX = "  ";

    private Feedback(
        boolean isError, String message, FeedbackTrace stackTrace, List<Feedback> children) {
        this.isError = isError;
        this.message = message;
        this.stackTrace = stackTrace;
        this.children = new ArrayList<>();

        if (children != null) {
            for (Feedback child : children) {
                if (child.getIsError()) {
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
     * Get a new feedback that has children, in case any of the children has any errors this will
     * also be an error, otherwise be successful.
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
        return isError;
    }

    /**
     * Returns the message specified..
     *
     * @return the message.
     */
    public String getMessage() {
        return message;
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
        if (!isError) {
            return "";
        }

        StringBuilder message = new StringBuilder();
        message.append(linePrefix);
        if (stackTrace != null) {
            message.append(stackTrace.toString());
            message.append(" : ");
        }

        if (children.isEmpty()) {
            // We don't have any children, i.e. we're the 'leaf' node and therefore we print our
            // message.
            message.append(getMessage());
        } else {
            message.append('\n');
            String childPrefix = linePrefix + LINE_PREFIX;
            // We want to print all of our children in a nice way.
            for (Feedback child : children) {
                message.append(child.getFullMessage(childPrefix));
            }
        }

        return message.toString();
    }
}
