package gruppun.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AnalyserPlugin implements Plugin<Project> {
     @Override
        public void apply(Project project) {
         project.getTasks().create("Analyse", AnalyseTask.class);
         project.getTasksByName("check", true).forEach(task -> {
             task.dependsOn("Analyse");
         });
     }
}
