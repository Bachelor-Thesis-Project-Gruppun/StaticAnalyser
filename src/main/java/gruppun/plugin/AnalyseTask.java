package gruppun.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

public class AnalyseTask extends DefaultTask {
    @TaskAction
    public void check() throws GradleException {
        System.out.println("TEST");
    }
}