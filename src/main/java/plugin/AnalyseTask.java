package plugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import tool.MainProgram;
import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

/**
 * A gradle task that will check verify classes against design patterns as described by certain
 * annotations.
 */
@DesignPattern(pattern = {Pattern.IMMUTABLE})
public class AnalyseTask extends DefaultTask {

    public AnalyseTask() {
        super();
    }

    /**
     * Check task that will run the analyse program.
     *
     * @exception GradleException thrown when violations are found during analysation.
     */
    @TaskAction
    public void check() throws GradleException {
        Project project = this.getProject();
        List<Path> paths = new ArrayList<>();

        SourceSet mainSourceSet = project.getConvention().getPlugin(JavaPluginConvention.class)
                                         .getSourceSets().getByName("main");

        mainSourceSet.getAllJava().getSrcDirs().forEach(file -> {
            paths.add(Paths.get(file.getAbsolutePath()));
        });

        List<String> pathStrings = new ArrayList<>();
        paths.forEach(path -> {
            pathStrings.add(path.toString());
        });

        MainProgram.startAnalyse(pathStrings.toArray(new String[0]));
    }
}