package tool.designpatterns.verifiers;

import tool.feedback.Feedback;

/**
 * An abstract template for what a PatternInstance class should be able to do.
 * <p>
 * In a subclass there should be private fields for each component of the pattern.
 */
public abstract class APatternInstance {

    public APatternInstance() {
    }

    /**
     * A methods that checks if alla necessary parts a design pattern exists.
     *
     * @return A positive or negative feedback depending on if all parts existed.
     */
    @SuppressWarnings("PMD.LinguisticNaming")
    public Feedback hasAllElements() {
        StringBuilder feedbackMessage = new StringBuilder(127);
        feedbackMessage.append("The following elements are missing: ");

        boolean errorOccurred = checkElements(feedbackMessage);

        if (errorOccurred) {
            // We know that the last two characters are ", " and we want to remove those.
            feedbackMessage.replace(feedbackMessage.length() - 2, feedbackMessage.length(), ".");
            return Feedback.getPatternInstanceNoChildFeedback(feedbackMessage.toString());
        }

        return Feedback.getSuccessfulFeedback();
    }

    /**
     * An abstract method wherein a subclass defines what it needs to be a complete configuration.
     *
     * @param feedbackMessage {@link StringBuilder} sent from hasAllElements in order to specify
     *                        which elements may be missing.
     *
     * @return a boolean specifying whether an error occured or not.
     */
    protected abstract boolean checkElements(StringBuilder feedbackMessage);

}
