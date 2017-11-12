package com.lazan.maven.transform

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

/**
 * Created by Lance on 11/11/2017.
 */
class MavenTransformPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.apply(plugin: 'base')
        TaskContainer tasks = project.getTasks()
        EffectivePom effectivePom = tasks.create("effectivePom", EffectivePom.class)
        EffectivePomTransform pomTransform = tasks.create("pomTransform", EffectivePomTransform.class)
        pomTransform.dependsOn(effectivePom)
        project.extensions.create("mavenTransform", MavenTransformModelImpl.class, project, effectivePom, pomTransform)

        Task buildTask = tasks.findByName('build')
        buildTask.dependsOn effectivePom
        buildTask.dependsOn pomTransform
    }
}
