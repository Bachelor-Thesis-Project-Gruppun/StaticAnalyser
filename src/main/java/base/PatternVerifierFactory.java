package base;

public class PatternVerifierFactory {

    public static IVerifier getVerifier(Pattern p) {
        switch (p) {
            case SINGLETON:
                return new SingletonVerifier();
            default:
                throw new IllegalStateException();
        }
    }
}
