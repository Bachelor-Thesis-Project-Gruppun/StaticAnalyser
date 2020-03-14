package utilities;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A testmanager that provides some basic test utilities.
 */
// @DesignPattern(pattern = {Pattern.SINGLETON})
public class TestHelper {
    private static String mockpath = "src/test/java/mocks/";

    // OBS:: Only get this through the getFullMockPath method!
    private static String fullMockPath = "";

    private static String getFullMockPath(String filePath) {
        if (fullMockPath == "") {
            String userDir = System.getProperty("user.dir");
            fullMockPath = userDir + "/" + mockpath;
        }

        return fullMockPath + filePath;
    }

    /**
     * returns the 'File' for the mock file given a path.
     *
     * @param pattern the pattern folder name.
     * @param className the name of the mock class to load.
     * @return the file for the mockfile.
     * @throws FileNotFoundException in case the file could not be found.
     */
    public static CompilationUnit getMockCompUnit(String pattern, String className) throws FileNotFoundException {
        String path = getFullMockPath(pattern + "/" + className + ".java");
        File file = new File(path);
        CompilationUnit compUnit = StaticJavaParser.parse(file);
        return compUnit;
    }
}
