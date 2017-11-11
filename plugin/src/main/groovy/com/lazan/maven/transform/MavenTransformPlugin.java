package com.lazan.maven.transform;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

/**
 * Created by Lance on 11/11/2017.
 */
public class MavenTransformPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        TaskContainer tasks = project.getTasks();
        EffectivePom effectivePom = tasks.create("effectivePom", EffectivePom.class);
        EffectivePomTransform pomTransform = tasks.create("pomTransform", EffectivePomTransform.class);
        pomTransform.dependsOn(effectivePom);

        MavenTransformModel model = createModel(project, effectivePom, pomTransform);
        project.getExtensions().add("mavenTransform", model);
    }

    protected MavenTransformModel createModel(Project project, EffectivePom effectivePom, EffectivePomTransform pomTransform) {
        return new MavenTransformModel() {
            @Override
            public void rootPom(Object rootPom) {
                effectivePom.rootPom(rootPom);
            }

            @Override
            public void transformManyToOne(Action<? super ManyToOneModel> action) {
                pomTransform.manyToOne(action);
            }

            @Override
            public void transformOneToOne(Action<? super OneToOneModel> action) {
                pomTransform.oneToOne(action);
            }
        };
    }
}
