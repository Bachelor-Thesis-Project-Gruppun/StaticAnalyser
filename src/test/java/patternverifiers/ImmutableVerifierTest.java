package patternverifiers;

import java.io.File;
import java.io.IOException;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import org.junit.jupiter.api.Test;

/**
 * Pattern verifier for the immutable pattern. The following predicates will be Every variable needs
 * to be static, final or it needs to be private and no method changes it.
 */
public class ImmutableVerifierTest {

    @Test
    public void testVerify() throws IOException {
        String fileName = "java/patternimplementors/ImmutableClass.java";
        ClassLoader classLoader = getClass().getClassLoader();

        File file = new File(classLoader.getResource(fileName).getFile());
        CompilationUnit cu = StaticJavaParser.parse(file);
        assertFalse(new ImmutableVerifier().verify(cu).getIsError());
    }

}