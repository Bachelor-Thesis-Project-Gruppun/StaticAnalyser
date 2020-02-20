package base;

import java.io.IOException;

public final class MainProgram {

    private static final String FILE_PATH =
        "src/main/java/base/MainProgram" + ".java";

    private MainProgram() {
        System.out.println("No idea");
    }

    public static void main(String[] args) throws IOException {
        ProjectParser pp = new ProjectParser(
            "src"); // Just use this project for
        // now, will have to change to the target project with the gradle stuff
        pp.verify();
    }
}
