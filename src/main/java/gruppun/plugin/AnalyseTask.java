package gruppun.plugin;

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

/**
 * A gradle task that will check verify classes against design patterns as
 * described by certain annotations.
 */
public class AnalyseTask extends DefaultTask {
    public AnalyseTask() {
        super();
    }

    @TaskAction
    public void check() throws GradleException {
        Project project = this.getProject();
        List<Path> paths = new ArrayList<>();

        SourceSet mainSourceSet = project.getConvention().getPlugin(
            JavaPluginConvention.class).getSourceSets().getByName("main");


        mainSourceSet.getAllJava().getSrcDirs().forEach(file -> {
            paths.add(Paths.get(file.getAbsolutePath()));
        });

        paths.forEach(path -> {
            // The path we want to pass to JavaParser
            System.out.println("Path:: " + path.toString());
        });

        // Example on how we can fail a build.
        // throw new GradleException("Class A does not fullfill pattern B!");
    }
}