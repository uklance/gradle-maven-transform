package com.lazan.maven.transform

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar

class MavenTransformPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
		project.with {
			apply plugin: 'base'
			
			Task mavenTransform = tasks.create('mavenTransform', MavenTransform.class)
			
			Task mavenTransformJar = tasks.create('mavenTransformJar', Jar) {
				dependsOn mavenTransform
				from mavenTransform
			}
			
			assemble.dependsOn mavenTransformJar
			
			artifacts {
				'default' mavenTransformJar
			}
		}
    }
}
