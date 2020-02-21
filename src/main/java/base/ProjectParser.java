package base;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

public class ProjectParser {
    public static List<CompilationUnit> projectToAst(String sourcePath) {
        List<CompilationUnit> compilationUnits;
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


