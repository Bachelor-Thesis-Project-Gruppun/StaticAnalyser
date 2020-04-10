package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

/**
 * Utility methods for verifying ClassOrInterfaceDeclarations.
 */
public final class ClassVerification {

    private ClassVerification() {

    }

    /**
     * Returns true if the first class equals the second class.
     *
     * @param first  the first class to check.
     * @param second the second class to check.
     *
     * @return if first equals second.
     */
    public static boolean isSameClassOrInterfaceDeclaration(
        ClassOrInterfaceDeclaration first, ClassOrInterfaceDeclaration second) {
        return first.resolve().getQualifiedName().equals(second.resolve().getQualifiedName());
    }

    /**
     * Returns wether or not the given list of ClassOrInterfaceDeclarations contains the given class
     * or not.
     *
     * @param classOrInterfaceList the list to look in.
     * @param lookFor              the class to look for.
     *
     * @return true if the list contains the class, false otherwise.
     */
    public static boolean classOrInterfaceListContains(
        List<ClassOrInterfaceDeclaration> classOrInterfaceList,
        ClassOrInterfaceDeclaration lookFor) {

        String qualLookFor = lookFor.resolve().getQualifiedName();
        for (ClassOrInterfaceDeclaration classOrI : classOrInterfaceList) {
            if (classOrI.resolve().getQualifiedName().equals(qualLookFor)) {
                return true;
            }
        }

        return false;
    }
}
