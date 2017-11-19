package com.lazan.maven.transform.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;

import com.lazan.maven.transform.FreemarkerTemplate;
import com.lazan.maven.transform.ProjectsContext;
import com.lazan.maven.transform.ProjectsTransformModel;
import com.lazan.maven.transform.Template;

/**
 * Created by Lance on 11/11/2017.
 */
public class ProjectsTransformModelImpl implements ProjectsTransformModel {
    private final Project project;
    private FileCollection classpath;
    private String outputPath;
    private List<Template> templates = new ArrayList<>();
    private Map<String, Function<ProjectsContext, Object>> contextFunctions = new LinkedHashMap<>();

    public ProjectsTransformModelImpl(Project project) {
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
        template(new FreemarkerTemplate(templatePath));
    }

    @Override
    public void template(Template template) {
        templates.add(template);
    }

    @Override
    public void context(String contextKey, Function<ProjectsContext, Object> contextFunction) {
        contextFunctions.put(contextKey, contextFunction);
    }

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
    
    public FileCollection getClasspath() {
		return classpath;
	}
}
