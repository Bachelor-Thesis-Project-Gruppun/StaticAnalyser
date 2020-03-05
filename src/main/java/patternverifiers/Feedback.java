package patternverifiers;

/**
 * Simple class to store and manage feedback to the user.
 */
public class Feedback {

    private final boolean value;
    private final String message;

    /**
     * Constructs a new feedback.
     *
     * @param value   the result.
     * @param message a message of what happened. (Maybe a justification for the value and a line
     *                number?)
     */
    public Feedback(boolean value, String message) {
        this.value = value;
        this.message = message;
    }

    /**
     * Constructs a new Feedback with an empty message.
     *
     * @param value the result.
     */
    public Feedback(boolean value) {
        this.value = value;
        this.message = "";
    }

    @SuppressWarnings("PMD.BooleanGetMethodName") // If anyone
    public boolean getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}
