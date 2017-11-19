package com.lazan.maven.transform.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.gradle.api.Project;

import com.lazan.maven.transform.FreemarkerTemplate;
import com.lazan.maven.transform.ProjectsContext;
import com.lazan.maven.transform.ProjectsTransformModel;
import com.lazan.maven.transform.Template;

/**
 * Created by Lance on 11/11/2017.
 */
public class ProjectsTransformModelImpl implements ProjectsTransformModel {
    private final Project project;
    private String outputPath;
    private List<Template> templates = new ArrayList<>();
    private Map<String, Function<ProjectsContext, Object>> contextFunctions = new LinkedHashMap<>();

    public ProjectsTransformModelImpl(Project project) {
        this.project = project;
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
