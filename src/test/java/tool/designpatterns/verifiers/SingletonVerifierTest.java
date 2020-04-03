package tool.designpatterns.verifiers;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.gradle.internal.impldep.org.junit.Assert.assertTrue;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import org.junit.jupiter.api.Test;
import tool.designpatterns.verifiers.singleclassverifiers.singleton.SingletonVerifier;
import utilities.TestHelper;

/**
 * Pattern verifier for the singleton pattern. More tests need to be added for each predicate.
 */
public class SingletonVerifierTest {

    ClassOrInterfaceDeclaration toTest;

    @Test
    public void testVerifyLazySingleton() throws IOException {
        toTest = TestHelper.getMockClassOrI("singleton", "LazySingletonMock");
        assertFalse(new SingletonVerifier().verify(toTest).getIsError());
    }

    @Test
    public void testVerifyIncorrectImplementation() throws FileNotFoundException {
        toTest = TestHelper.getMockClassOrI("singleton", "FailingSingletonMock");
        assertTrue(new SingletonVerifier().verify(toTest).getIsError());
    }

    @Test
    public void testVerifyMultipleConstructors() throws FileNotFoundException {
        toTest = TestHelper.getMockClassOrI("singleton", "SingletonMockMultipleConstructors");
        assertFalse(new SingletonVerifier().verify(toTest).getIsError());

    }

    @Test
    public void testVerifyEagerSingleton() throws FileNotFoundException {
        toTest = TestHelper.getMockClassOrI("singleton", "EagerSingletonMock");
        assertFalse(new SingletonVerifier().verify(toTest).getIsError());
    }

    @Test
    public void testVerifyElseStmt() throws FileNotFoundException {
        toTest = TestHelper.getMockClassOrI("singleton", "SingletonMockWithElseStmt");
        assertTrue(new SingletonVerifier().verify(toTest).getIsError());
    }

    @Test
    public void testVerifyMultipleAcces() throws FileNotFoundException {
        toTest = TestHelper.getMockClassOrI("singleton", "SingletonMockMultipleAccessMethods");
        assertTrue(new SingletonVerifier().verify(toTest).getIsError());
    }

}