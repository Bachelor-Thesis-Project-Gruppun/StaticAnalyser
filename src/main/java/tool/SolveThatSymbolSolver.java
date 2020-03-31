package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class SolveThatSymbolSolver {

    public static ParserConfiguration GetConfig(String rootDir) {

        File lookForTypes = new File("src");
        CombinedTypeSolver typeSolver = new CombinedTypeSolver(
            new JavaParserTypeSolver(lookForTypes), new ReflectionTypeSolver(),
            new JavaParserTypeSolver(new File("src/test/java")));

        List<JavaParserTypeSolver> javaParserTypeSolvers = null;
        try {
            javaParserTypeSolvers = getSourceTypeSolvers(new File(rootDir));
            for (JavaParserTypeSolver solver : javaParserTypeSolvers) {
                typeSolver.add(solver);
            }
        } catch (Exception e) {
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
     *
     * @param rootDir the root dir.
     *
     * @return A list of JavaParserTypeSolvers, one for each package root found. Alternatively an
     *     empty list if none could be found.
     */
    private static List<JavaParserTypeSolver> getSourceTypeSolvers(File rootDir) throws Exception {
        List<JavaParserTypeSolver> typeSolvers = new ArrayList<>();

        // It is a directory, let's keep going
        File[] childFiles = rootDir.listFiles();
        for (File child : childFiles) {
            System.out.println("Found child: " + child.toString());
            if (child.getName().endsWith(".java")) {
                // Find the root package for this java file.
                String pkg = getPackage(child);

                int numFolders = pkg.isEmpty() ? 0 : getCharOccurances(pkg, '.') + 1;
                File node = child;
                while (numFolders > 0) {
                    numFolders--;
                    node = node.getParentFile();
                    if (node == null) {
                        throw new Exception(
                            "Unable to find root package directory for " + rootDir.toString());
                    }

                    typeSolvers.add(new JavaParserTypeSolver(node));
                }

            } else if (child.isDirectory()) {
                typeSolvers.addAll(getSourceTypeSolvers(child));
            }
        }

        return typeSolvers;
    }

    /**
     * Counts the number of occurances of the given char in the given string.
     *
     * @param pkgName the string to look in.
     * @param ch      the char to look for.
     *
     * @return the number of occurances of the char in the string.
     */
    private static int getCharOccurances(String pkgName, Character ch) {
        int count = 0;
        for (Character character : pkgName.toCharArray()) {
            if (character.equals(ch)) {
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
    private static String getPackage(File file) {
        StringBuilder pkg = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();

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
                        // Remove the semi-colon and everything after it and return.
                        return theString.substring(0, theString.indexOf(";"));
                    }
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            System.out.println("No package found for file " + file.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            char c = line.charAt(i);
            packageBuilder.append(c);
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
