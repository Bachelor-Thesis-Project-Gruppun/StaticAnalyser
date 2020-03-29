package tool;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.utils.SourceRoot;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

/**
 * The class responsible for parsing a project, currently only converting to AST.
 */
@DesignPattern(pattern = {Pattern.IMMUTABLE})
public final class ProjectParser {

    private ProjectParser() {
    }

    /**
     * Find all classes and Interfaces in a project found under a certain path.
     *
     * @param sourcePath the root of the project
     *
     * @return a list of {@link ClassOrInterfaceDeclaration}
     */
    public static List<ClassOrInterfaceDeclaration> findAllClassesAndInterfaces(String sourcePath) {
        // Enable symbolsolving.
        TypeSolver typeSolver = new CombinedTypeSolver();
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration config = StaticJavaParser.getConfiguration();
        config.setSymbolResolver(symbolSolver);

        Path pathToSource = Paths.get(sourcePath);
        SourceRoot sourceRoot = new SourceRoot(pathToSource, config);

        try {
            sourceRoot.tryToParse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<ClassOrInterfaceDeclaration> classes = new ArrayList<>();

        sourceRoot.getCompilationUnits().forEach(compilationUnit -> classes
            .addAll(compilationUnit.findAll(ClassOrInterfaceDeclaration.class)));

        return classes;
    }
}


