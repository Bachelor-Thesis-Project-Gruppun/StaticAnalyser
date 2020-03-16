package patternverifiers;

import java.io.File;
import java.io.FileNotFoundException;
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

    String basePath = "src/test/java/designpatternmocks/singletonpattern/";

    @Test
    public void testVerifyLazySingleton() throws IOException {
        String baseMock = basePath + "LazySingletonMock.java";
        File baseFile = new File(baseMock);
        CompilationUnit baseCompUnit = StaticJavaParser.parse(baseFile);
        assertTrue(new SingletonVerifier().verify(baseCompUnit).getValue());
    }

    @Test
    public void testVerifyIncorrectImplementation() throws FileNotFoundException {
        String failingName = basePath + "FailingSingletonMock.java";
        File failingFile = new File(failingName);
        CompilationUnit failingCompUnit = StaticJavaParser.parse(failingFile);
        assertFalse(new SingletonVerifier().verify(failingCompUnit).getValue());
    }

    @Test
    public void testVerifyMultipleConstructors() throws FileNotFoundException {
        String multipleConstructors = basePath + "SingletonMockMultipleConstructors.java";
        File multipleConsFile = new File(multipleConstructors);
        CompilationUnit multipleConsCompUnit = StaticJavaParser.parse(multipleConsFile);
        assertTrue(new SingletonVerifier().verify(multipleConsCompUnit).getValue());

    }

    @Test
    public void testVerifyEagerSingleton() throws FileNotFoundException {
        String eagerSingleton = basePath + "EagerSingletonMock.java";
        File eagerSingletonFile = new File(eagerSingleton);
        CompilationUnit eagerSingletonCU = StaticJavaParser.parse(eagerSingletonFile);
        assertTrue(new SingletonVerifier().verify(eagerSingletonCU).getValue());
    }

    @Test
    public void testVerifyElseStmt() throws FileNotFoundException {
        String failMockElseStmt = basePath + "SingletonMockWithElseStmt.java";
        File failMockElseStmtFile = new File(failMockElseStmt);
        CompilationUnit failMockElseStmtCU = StaticJavaParser.parse(failMockElseStmtFile);
        assertFalse(new SingletonVerifier().verify(failMockElseStmtCU).getValue());
    }

    @Test
    public void testVerifyMultipleAcces() throws FileNotFoundException {
        String multipleAccessMethod = basePath + "SingletonMockMultipleAccessMethods.java";
        File multipleAccessFile = new File(multipleAccessMethod);
        CompilationUnit multipleAccessCU = StaticJavaParser.parse(multipleAccessFile);
        assertFalse(new SingletonVerifier().verify(multipleAccessCU).getValue());
    }

}