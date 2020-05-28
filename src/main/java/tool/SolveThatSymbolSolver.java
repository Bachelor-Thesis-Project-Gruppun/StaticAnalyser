package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

/**
 * Class to create a config including a symbol solver for the project parser.
 */
public final class SolveThatSymbolSolver {

    private SolveThatSymbolSolver() {

    }

    /**
     * Get a config with a symbol solver.
     *
     * @param rootDir the root directory of the project.
     *
     * @return the new config.
     */
    @SuppressWarnings({"PMD.SystemPrintln", "PMD.AvoidInstantiatingObjectsInLoops"})
    // Catch errors that I don't quite know what to do with. Used for debugging purposes mostly.
    public static ParserConfiguration getConfig(String rootDir) {

        CombinedTypeSolver typeSolver = new CombinedTypeSolver(
            new JavaParserTypeSolver(rootDir), new ReflectionTypeSolver());

        List<File> packageRoots = null;
        try {
            packageRoots = getPackageRoots(new File(rootDir));
            List<File> noDuplicates = new ArrayList<>();
            for (File root : packageRoots) {
                if (!noDuplicates.contains(root)) {
                    noDuplicates.add(root);
                }
            }

            for (File pkgRoot : noDuplicates) {
                typeSolver.add(new JavaParserTypeSolver(pkgRoot));
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Unable to get config for " + rootDir);
            System.err.println(e);
        }
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration config = StaticJavaParser.getConfiguration();
        config.setSymbolResolver(symbolSolver);
        StaticJavaParser.setConfiguration(config);

        return config;
    }

    /**
     * Goes through and tries to find each ''package root'' in the subdirectories of the given dir.
     * May contain duplicates!
     *
     * @param rootDir the root dir.
     *
     * @return A list of Files, one for each package root found. Alternatively an empty list if none
     *     could be found.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    // Would be less performant to cache nodes in a list and then creating TypeSolvers from
    // them (regarding the AvoidInstantiatingObjectsInLoops).
    private static List<File> getPackageRoots(File rootDir) throws IllegalArgumentException {
        List<File> packageRoots = new ArrayList<>();

        // It is a directory, let's keep going
        File[] childFiles = rootDir.listFiles();
        if (childFiles == null) {
            return packageRoots;
        }

        for (File child : childFiles) {
            if (child.getName().endsWith(".java")) {
                // Find the root package for this java file.
                String pkg = "";
                try {
                    pkg = getPackage(child);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                File node = getPackageRootNode(pkg, child);
                packageRoots.add(node);
            } else if (child.isDirectory()) {
                packageRoots.addAll(getPackageRoots(child));
            }
        }

        return packageRoots;
    }

    private static File getPackageRootNode(String pkg, File base) {
        int numFolders = pkg.isEmpty() ? 1 : getCharOccurances(pkg, '.') + 2;

        File node = base;
        while (numFolders > 0) {
            numFolders--;
            node = node.getParentFile();
            if (node == null) {
                throw new IllegalArgumentException(
                    "Unable to find root package directory for " + base.toString());
            }
        }
        return node;
    }

    /**
     * Counts the number of occurances of the given char in the given string.
     *
     * @param pkgName   the string to look in.
     * @param character the char to look for.
     *
     * @return the number of occurances of the char in the string.
     */
    private static int getCharOccurances(String pkgName, Character character) {
        int count = 0;
        for (Character arrChar : pkgName.toCharArray()) {
            if (arrChar.equals(character)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the package (if found) for the given .java file.
     *
     * @param file a .java file to find a package for.
     *
     * @return the package or an empty string if not found.
     */
    @SuppressWarnings("PMD.CloseResource")
    private static String getPackage(File file) throws IOException {
        StringBuilder pkg = new StringBuilder();
        BufferedReader bufferedReader = Files.newBufferedReader(file.toPath());
        String line = bufferedReader.readLine();

        // Used as a package can be declared on multiple lines.
        boolean foundPackage = false;
        while (line != null) {
            String toAppend = line;
            if (!foundPackage) {
                toAppend = getCleanedPackageString(line);
                if (toAppend != null) {
                    foundPackage = true;
                }
            }

            if (foundPackage) {
                pkg.append(toAppend);
                String theString = pkg.toString();
                if (theString.contains(";")) {
                    bufferedReader.close();
                    // Remove the semi-colon and everything after it and return.
                    return theString.substring(0, theString.indexOf(';'));
                }
            }
            line = bufferedReader.readLine();
        }

        bufferedReader.close();
        return "";
    }

    /**
     * First tries to find the matching package word in the line, if found returns the rest of the
     * line (what is left after the match).
     *
     * @param line The line to read through.
     *
     * @return the rest of the line or null if it does not contain the match.
     */
    private static String getCleanedPackageString(String line) {
        String packageMatch = "package ";
        StringBuilder packageBuilder = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char charAtI = line.charAt(i);
            packageBuilder.append(charAtI);
            if (packageBuilder.toString().equals(packageMatch)) {
                if (i == line.length() - 1) {
                    // the line ends after the matching string.
                    return "";
                }

                return line.substring(i + 1);
            }
        }

        return null;
    }
}
