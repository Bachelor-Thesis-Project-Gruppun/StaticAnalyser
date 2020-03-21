package patternverifiers;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple class to store and manage feedback to the user.
 */
public class Feedback {

    private boolean isError;
    private final String message;
    private List<Feedback> children;
    private FeedbackImplementations stackTrace;

    private static final String LINE_PREFIX = "  ";

    private Feedback(
        boolean isError, String message, FeedbackImplementations stackTrace,
        List<Feedback> children) {
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
        String message, FeedbackImplementations stackTrace) {
        return new Feedback(true, message, stackTrace, null);
    }

    /**
     * Get a new feedback that is an error and has the
     *
     * @param stackTrace the element where the error occured.
     * @param children   the child feedback elements to this feedback.
     *
     * @return a new feedback.
     */
    public static Feedback getFeedbackWithChildren(
        FeedbackImplementations stackTrace, List<Feedback> children) {
        if (children == null || children.size() == 0) {
            throw new IllegalArgumentException("A non-leaf feedback must have children.");
        }

        return new Feedback(false, "", stackTrace, children);
    }

    /**
     * Get a new feedback that is not an error.
     *
     * @return a feedback that is not an error.
     */
    public static Feedback getNoErrorFeedback() {
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
        message.append(stackTrace.toString());
        message.append(" : ");

        if (children.size() == 0) {
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
