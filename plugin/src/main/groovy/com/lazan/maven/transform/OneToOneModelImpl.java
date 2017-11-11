package com.lazan.maven.transform;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.internal.impldep.org.apache.maven.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class OneToOneModelImpl implements OneToOneModel, ClasspathSource {
    private final Project project;
    private FileCollection classpath;
    private Function<Model, Object> outputFileFunction;
    private List<Template> templates = new ArrayList<>();
    private Map<String, Function<Model, Object>> contextFunctions;

    public OneToOneModelImpl(Project project) {
        this.project = project;
    }

    @Override
    public void classpath(FileCollection classpath) {
        this.classpath = classpath;
    }

    @Override
    public void outputFile(Function<Model, Object> outputFileFunction) {
        this.outputFileFunction = outputFileFunction;
    }

    @Override
    public void freemarkerTemplate(String templatePath) {
        template(new FreemarkerTemplate(this, templatePath));
    }

    @Override
    public void template(Template template) {
        templates.add(template);
    }

    @Override
    public void context(String contextKey, Function<Model, Object> contextFunction) {
        contextFunctions.put(contextKey, contextFunction);
    }

    @Override
    public FileCollection getClasspath() {
        return classpath;
    }

    public Function<Model, Object> getOutputFileFunction() {
        return outputFileFunction;
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public Map<String, Function<Model, Object>> getContextFunctions() {
        return contextFunctions;
    }
}
