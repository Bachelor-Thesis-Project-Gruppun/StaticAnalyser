package tool.feedback;

public class FeedbackWrapper<T> {

    private Feedback feedback;
    private T other;

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

