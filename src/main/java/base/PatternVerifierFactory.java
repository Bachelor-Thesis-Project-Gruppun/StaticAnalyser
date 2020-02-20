package base;

public class PatternVerifierFactory {
    public static IVerifier getVerifier(String s) {
        switch(s) {
            case "Singleton":
                return new SingletonVerifier();
            default:
                throw new IllegalStateException();
        }
    }

}
