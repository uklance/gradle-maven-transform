package com.lazan.maven.transform;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.apache.maven.model.Model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class OneToOneModelImpl implements OneToOneModel, ClassLoaderSource {
    private final Project project;
    private FileCollection classpath;
    private Function<Model, String> outputPathFunction;
    private List<Template> templates = new ArrayList<>();
    private Map<String, Function<ProjectContext, Object>> contextFunctions = new LinkedHashMap<>();

    public OneToOneModelImpl(Project project) {
        this.project = project;
    }

    @Override
    public void classpath(FileCollection classpath) {
        this.classpath = classpath;
    }

    @Override
    public void outputPath(Function<Model, String> outputPathFunction) {
        this.outputPathFunction = outputPathFunction;
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
    public void context(String contextKey, Function<ProjectContext, Object> contextFunction) {
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

    public Function<Model, String> getOutputPathFunction() {
        return outputPathFunction;
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public Map<String, Function<ProjectContext, Object>> getContextFunctions() {
        return contextFunctions;
    }
    
    public FileCollection getClasspath() {
		return classpath;
	}
}
