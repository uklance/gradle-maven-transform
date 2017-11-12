package com.lazan.maven.transform;

import org.gradle.api.file.FileCollection;
import org.gradle.internal.impldep.org.apache.maven.model.Model;

import java.util.*;
import java.util.function.Function;

/**
 * Created by Lance on 11/11/2017.
 */
public class ManyToOneModelImpl implements ManyToOneModel, ClassLoaderSource {
    private final MavenTransformModelImpl mavenTransformModel;
    private FileCollection classpath;
    private String outputPath;
    private List<Template> templates = new ArrayList<>();
    private Map<String, Function<Collection<Model>, Object>> contextFunctions = new LinkedHashMap<>();

    public ManyToOneModelImpl(MavenTransformModelImpl mavenTransformModel) {
        this.mavenTransformModel = mavenTransformModel;
    }

    @Override
    public void classpath(FileCollection classpath) {
        this.classpath = classpath;
    }

    @Override
    public void outputPath(String outputPath) {
        this.outputPath = outputPath;
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
    public void context(String contextKey, Function<Collection<Model>, Object> contextFunction) {
        contextFunctions.put(contextKey, contextFunction);
    }

    @Override
    public ClassLoader getClassLoader() {
        return mavenTransformModel.getClassLoader(classpath);
    }

    public String getOutputPath() {
        return outputPath;
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public Map<String, Function<Collection<Model>, Object>> getContextFunctions() {
        return contextFunctions;
    }
}
