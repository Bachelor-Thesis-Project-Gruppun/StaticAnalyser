package patternverifiers;

import java.io.File;
import java.io.IOException;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.gradle.internal.impldep.org.junit.Assert.assertTrue;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import org.junit.jupiter.api.Test;

/**
 * Pattern verifier for the singleton pattern. More tests need to be added for each predicate.
 */
public class SingletonVerifierTest {

    @Test
    public void testVerify() throws IOException {
        String basePath = "src/test/java/designpatternmocks/singletonpattern/";
        String failingName = basePath + "FailingSingletonMock.java";
        String baseMock = basePath + "SingletonMock.java";
        String multipleConstructors = basePath + "SingletonMockMultipleConstructors.java";
        File failingFile = new File(failingName);
        File baseFile = new File(baseMock);
        File multipleConsFile = new File(multipleConstructors);
        CompilationUnit failingCompUnit = StaticJavaParser.parse(failingFile);
        CompilationUnit baseCompUnit = StaticJavaParser.parse(baseFile);
        CompilationUnit multipleConsCompUnit = StaticJavaParser.parse(multipleConsFile);
        assertFalse(new SingletonVerifier().verify(failingCompUnit).getValue());
        assertTrue(new SingletonVerifier().verify(baseCompUnit).getValue());
        assertFalse(new SingletonVerifier().verify(multipleConsCompUnit).getValue());

    }

}