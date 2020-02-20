package base;

import static base.Pattern.IMMUTABLE;
import static base.Pattern.SINGLETON;

import com.github.javaparser.ast.CompilationUnit;

import jdk.jshell.spi.ExecutionControl;

public class PatternVerifyer {
    // Very basic, use the enum instead of strings
    public static boolean verifyPattern(String p, CompilationUnit cu) {
        switch(p) {
            case "Singleton":
                return true; // verifySingleton(cu);
            case "Immutable":
                return true; // verifyImmutable(cu);
            default:
                throw new IllegalStateException("Unexpected value: " + p);
        }
    }
}
