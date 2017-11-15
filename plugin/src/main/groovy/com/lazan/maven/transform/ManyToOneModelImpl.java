package com.lazan.maven.transform;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.apache.maven.model.Model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Function;

/**
 * Created by Lance on 11/11/2017.
 */
public class ManyToOneModelImpl implements ManyToOneModel, ClassLoaderSource {
    private final Project project;
    private FileCollection classpath;
    private String outputPath;
    private List<Template> templates = new ArrayList<>();
    private Map<String, Function<ProjectsContext, Object>> contextFunctions = new LinkedHashMap<>();

    public ManyToOneModelImpl(Project project) {
        this.project = project;
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
    public void context(String contextKey, Function<ProjectsContext, Object> contextFunction) {
        contextFunctions.put(contextKey, contextFunction);
    }

    @Override
    public ClassLoader getClassLoader() {
        Function<File, URL> toUrl = (File file) -> {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        };
        URL[] urls = classpath.getFiles().stream().map(toUrl).toArray(URL[]::new);
        return new URLClassLoader(urls, null);
    }

    public String getOutputPath() {
        return outputPath;
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public Map<String, Function<ProjectsContext, Object>> getContextFunctions() {
        return contextFunctions;
    }
}
