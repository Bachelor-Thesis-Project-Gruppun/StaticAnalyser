package base;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

public class ProjectParser {

    private String sourceRoot;
    private List<CompilationUnit> compilationUnits;

    public ProjectParser(String sourceRoot) {
        this.sourceRoot = sourceRoot;
        this.compilationUnits = projectToAst();
    }

    private List<CompilationUnit> projectToAst() {
        Path pathToSource = Paths.get(sourceRoot);
        SourceRoot sourceRoot = new SourceRoot(pathToSource);
        try {
            sourceRoot.tryToParse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sourceRoot.getCompilationUnits();
    }

    public List<CompilationUnit> getCompilationUnits() {
        return compilationUnits;
    }
}


