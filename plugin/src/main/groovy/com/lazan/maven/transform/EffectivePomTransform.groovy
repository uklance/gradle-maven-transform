package com.lazan.maven.transform

import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ConfigureUtil
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import java.util.function.Function

class EffectivePomTransform extends DefaultTask {
    private File effectivePom
    private List<OneToOneModelImpl> oneToOneModels = []
    private List<ManyToOneModelImpl> manyToOneModels = []
    private File outputDirectory

    void effectivePom(Object effectivePom) {
        this.effectivePom = project.file(effectivePom)
    }

    void outputDirectory(Object outputDirectory) {
        this.outputDirectory = project.file(outputDirectory)
    }

    @InputFile
    File getEffectivePom() {
        return effectivePom
    }

    @OutputDirectory
    File getOutputDirectory() {
        return outputDirectory
    }

    void oneToOne(Closure configureClosure) {
        OneToOneModelImpl model = new OneToOneModelImpl(project)
        ConfigureUtil.configure(configureClosure, model)
        oneToOneModels.add(model)
    }

    void manyToOne(Closure configureClosure) {
        ManyToOneModelImpl model = new ManyToOneModelImpl(project)
        ConfigureUtil.configure(configureClosure, model)
        manyToOneModels.add(model)
    }

    @TaskAction
    void transformEffectivePom() {
        List<Model> mavenModels = []
        new XmlSlurper().parse(effectivePom).projects.project.each { element ->
            String projectXml = XmlUtil.serialize(element)
            MavenXpp3Reader reader = MavenXpp3Reader()
            Model mavenModel = reader.read(new StringReader(projectXml))
            mavenModels << mavenModel
        }

        for (ManyToOneModelImpl manyToOneModel : manyToOneModels) {
            File outFile = manyToOneModel.outputFile
            outFile.withOutputStream { OutputStream out ->
                Map<String, Object> context = []
                manyToOneModel.contextFunctions.each { String key, Function<Collection<Model>, Object> contextFunction ->
                    context[key] = contextFunction.apply(mavenModels)
                }
                for (Template template : manyToOneModel.templates) {
                    template.transform(context, out)
                }
            }
        }
        for (OneToOneModelImpl oneToOneModel : oneToOneModels) {
            for (Model mavenModel : mavenModels) {
                File outFile = oneToOneModel.outputFile
                outFile.withOutputStream { OutputStream out ->
                    Map<String, Object> context = []
                    oneToOneModel.contextFunctions.each { String key, Function<Model, Object> contextFunction ->
                        context[key] = contextFunction.apply(mavenModel)
                    }
                    for (Template template : oneToOneModel.templates) {
                        template.transform(context, out)
                    }
                }
            }
        }
    }
}
