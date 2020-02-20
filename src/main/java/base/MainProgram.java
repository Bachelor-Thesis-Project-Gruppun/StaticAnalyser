package base;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

public final class MainProgram {
    private static final String FILE_PATH = "src/main/java/base/MainProgram" +
                                            ".java";
    private MainProgram() {
        System.out.println("No idea");
    }

    public static void main(String[] args) throws IOException {
        //System.out.println("Hello world!");

        ProjectParser pp = new ProjectParser("src"); // Just use this project for
        // now, will have to change to the target project with the gradle stuff
        pp.verify();
    }
}
