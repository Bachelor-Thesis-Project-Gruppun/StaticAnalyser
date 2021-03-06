package plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

/**
 * A gradle plugin -- Responsible for creating the Analyse task and making it a dependency for the
 * check task.
 */
@DesignPattern(pattern = {Pattern.IMMUTABLE})
public class AnalyserPlugin implements Plugin<Project> {

    public AnalyserPlugin() {
        super();
    }

    @Override
    public void apply(Project project) {
        project.getTasks().create("Analyse", AnalyseTask.class);
        // Make the Analyse task a dependency for the check task so that it
        // is run on every build.
        project.getTasksByName("check", true).forEach(task -> {
            task.dependsOn("Analyse");
        });
    }
}
