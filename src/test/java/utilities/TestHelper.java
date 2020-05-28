package utilities;

import java.io.File;
import java.io.FileNotFoundException;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.SolveThatSymbolSolver;

/**
 * A testmanager that provides some basic test utilities.
 */
public class TestHelper {

    private static String mockpath = "src/test/java/mocks/";

    // OBS:: Only get this through the getFullMockPath method!
    private static String fullMockPath = "";

    private static String getFullMockPath(String filePath) {
        if (fullMockPath.equals("")) {
            String userDir = System.getProperty("user.dir");
            fullMockPath = userDir + "/" + mockpath;
        }

        return fullMockPath + filePath;
    }

    /**
     * returns the 'File' for the mock file given a path.
     *
     * @param pattern   the pattern folder name.
     * @param className the name of the mock class to load.
     *
     * @return the file for the mockfile.
     *
     * @exception FileNotFoundException in case the file could not be found.
     */
    public static CompilationUnit getMockCompUnit(String pattern, String className)
        throws FileNotFoundException {
        String path = getFullMockPath(pattern + "/" + className + ".java");
        File file = new File(path);
        SolveThatSymbolSolver.getConfig("src");
        return StaticJavaParser.parse(file);
    }

    /**
     * returns the Class given a path to a file with the same name.
     *
     * @param pattern   the pattern folder name.
     * @param className the name of the mock class to load.
     *
     * @return the file for the mockfile.
     *
     * @exception FileNotFoundException in case the file could not be found.
     */
    public static ClassOrInterfaceDeclaration getMockClassOrI(
        String pattern, String className) throws FileNotFoundException {
        CompilationUnit compUnit = getMockCompUnit(pattern, className);

        ClassOrInterfaceDeclaration classOrIToReturn = null;
        for (ClassOrInterfaceDeclaration classOrI : compUnit.findAll(
            ClassOrInterfaceDeclaration.class)) {
            if (classOrI.getNameAsString().equals(className)) {
                classOrIToReturn = classOrI;
            }
        }
        return classOrIToReturn;
    }

    /**
     * returns the 'File' for the mock file given a path.
     *
     * @param path      the absolute path to the parent folder.
     * @param className the name of the mock class to load.
     *
     * @return the ClassOrInterfaceDeclaration of the class.
     *
     * @exception FileNotFoundException in case the file could not be found.
     */
    public static CompilationUnit getMockCompUnitAbsPath(String path, String className)
        throws FileNotFoundException {
        String fullPath = path + "/" + className + ".java";
        File file = new File(fullPath);
        SolveThatSymbolSolver.getConfig("src");
        return StaticJavaParser.parse(file);
    }

    /**
     * returns the Class given a path to a file with the same name.
     *
     * @param path      the absolute path to the parent folder of the class.
     * @param className the name of the mock class to load.
     *
     * @return the ClassOrInterfaceDeclaration of the class.
     *
     * @exception FileNotFoundException in case the file could not be found.
     */
    public static ClassOrInterfaceDeclaration getMockClassOrIAbsPath(
        String path, String className) throws FileNotFoundException {
        CompilationUnit compUnit = getMockCompUnitAbsPath(path, className);

        ClassOrInterfaceDeclaration classOrIToReturn = null;
        for (ClassOrInterfaceDeclaration classOrI : compUnit.findAll(
            ClassOrInterfaceDeclaration.class)) {
            if (classOrI.getNameAsString().equals(className)) {
                classOrIToReturn = classOrI;
            }
        }
        return classOrIToReturn;
    }
}
