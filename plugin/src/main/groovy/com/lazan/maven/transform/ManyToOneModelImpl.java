package com.lazan.maven.transform;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.internal.impldep.org.apache.maven.model.Model;

import java.io.File;
import java.util.*;
import java.util.function.Function;

/**
 * Created by Lance on 11/11/2017.
 */
public class ManyToOneModelImpl implements ManyToOneModel, ClasspathSource {
    private final Project project;
    private FileCollection classpath;
    private File outputFile;
    private List<Template> templates = new ArrayList<>();
    private Map<String, Function<Collection<Model>, Object>> contextFunctions = new LinkedHashMap<>();

    public ManyToOneModelImpl(Project project) {
        this.project = project;
    }

    @Override
    public void classpath(FileCollection classpath) {
        this.classpath = classpath;
    }

    @Override
    public void outputFile(Object outputFile) {
        this.outputFile = project.file(outputFile);
    }

    @Override
    public void freemarkerTemplate(String templatePath) {
        template(new FreemarkerTemplate(this, templatePath));
    }

    @Override
    public void template(Template template) {

    }

    @Override
    public void context(String contextKey, Function<Collection<Model>, Object> contextFunction) {

    }

    @Override
    public FileCollection getClasspath() {
        return classpath;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public Map<String, Function<Collection<Model>, Object>> getContextFunctions() {
        return contextFunctions;
    }
}
