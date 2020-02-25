package base;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

/**
 * Contains methods used to convert a project into its equivalent AST-representation
 */
public final class ProjectParser {

    private ProjectParser() {
    }

    /**
     * @param sourcePath the root of the project
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


