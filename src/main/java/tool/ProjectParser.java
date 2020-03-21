package tool;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

/**
 * The class responsible for parsing a project, currently only converting to AST.
 */
public final class ProjectParser {

    private ProjectParser() {
    }

    /**
     * Turns a project, given by the path to it's source root, to an AST (compilationUnit).
     *
     * @param sourcePath the root of the project
     *
     * @return a list of {@link CompilationUnit} that represent the entire project in AST form
     */
    public static List<CompilationUnit> projectToAst(String sourcePath) {
        Path pathToSource = Paths.get(sourcePath);
        SourceRoot sourceRoot = new SourceRoot(pathToSource);
        try {
            sourceRoot.tryToParse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sourceRoot.getCompilationUnits();
    }
}


