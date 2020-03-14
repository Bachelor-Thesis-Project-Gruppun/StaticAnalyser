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
        String failMockElseStmt = basePath + "SingletonMockWithElseStmt.java";
        String eagerSingleton = basePath + "EagerSingletonMock.java";
        String multipleAccessMethod = basePath + "SingletonMockMultipleAccessMethods.java";
        File failingFile = new File(failingName);
        File eagerSingletonFile = new File(eagerSingleton);
        File baseFile = new File(baseMock);
        File multipleConsFile = new File(multipleConstructors);
        File failMockElseStmtFile = new File(failMockElseStmt);
        File multipleAccessFile = new File(multipleAccessMethod);
        CompilationUnit failingCompUnit = StaticJavaParser.parse(failingFile);
        CompilationUnit eagerSingletonCU = StaticJavaParser.parse(eagerSingletonFile);
        CompilationUnit baseCompUnit = StaticJavaParser.parse(baseFile);
        CompilationUnit multipleConsCompUnit = StaticJavaParser.parse(multipleConsFile);
        CompilationUnit failMockElseStmtCU = StaticJavaParser.parse(failMockElseStmtFile);
        CompilationUnit multipleAccessCU = StaticJavaParser.parse(multipleAccessFile);
        assertFalse(new SingletonVerifier().verify(multipleAccessCU).getValue());
        assertFalse(new SingletonVerifier().verify(failingCompUnit).getValue());
        assertFalse(new SingletonVerifier().verify(failMockElseStmtCU).getValue());
        assertTrue(new SingletonVerifier().verify(baseCompUnit).getValue());
        assertTrue(new SingletonVerifier().verify(multipleConsCompUnit).getValue());
        assertTrue(new SingletonVerifier().verify(eagerSingletonCU).getValue());

    }

}