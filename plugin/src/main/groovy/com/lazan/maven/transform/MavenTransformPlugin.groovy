package com.lazan.maven.transform

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by Lance on 11/11/2017.
 */
class MavenTransformPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
		project.with {
			apply plugin: 'base'
			
			Task mavenTransform = tasks.create('mavenTransform', MavenTransform.class)
			
			Task jar = tasks.create('jar', Jar) {
				dependsOn mavenTransform
				from mavenTransform
			}
			
			assemble.dependsOn jar
			
			artifacts {
				'default' file: jar.archivePath, builtBy: jar
			}
		}
    }
}
