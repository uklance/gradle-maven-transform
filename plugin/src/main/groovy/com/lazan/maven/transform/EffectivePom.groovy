package com.lazan.maven.transform

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.io.File

class EffectivePom  extends DefaultTask {
    @InputFiles
    List<FileCollection> sourcePoms = [];

    private File rootPom;
    private File outputFile;

    void rootPom(Object rootPom) {
        this.rootPom = project.file(rootPom)
    }

    void sourcePom(Object sourcePom) {
        sourcePoms << project.files(sourcePom)
    }

    void sourcePoms(FileCollection sourcePoms) {
        this.sourcePoms << sourcePoms
    }

    void outputFile(Object outputFile) {
        this.outputFile = project.file(outputFile);
    }

    @InputFile
    File getRootPom() {
        return rootPom
    }

    @OutputFile
    File getOutputFile() {
        return outputFile ?: project.file("${project.buildDir}/effectivePom/effective-pom.xml");
    }

    @TaskAction
    void generateEffectivePom() {
        project.exec {
            commandLine = "cmd /c mvn -f $rootPom help:effective-pom -Doutput=$outputFile".split(' ')
        }
    }
}
