package tool.feedback;

/**
 * A wrapper to be able to return something more than a feedback from methods.
 *
 * @param <T> the type that one wants to be able to return.
 */
public class FeedbackWrapper<T> {

    private final Feedback feedback;
    private final T other;

    public FeedbackWrapper(Feedback feedback, T other) {
        this.feedback = feedback;
        this.other = other;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public T getOther() {
        return other;
    }

}
