package com.lazan.maven.transform.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.maven.model.Model;
import org.gradle.api.Project;

import com.lazan.maven.transform.FreemarkerTransformer;
import com.lazan.maven.transform.ProjectContext;
import com.lazan.maven.transform.ProjectTransformModel;
import com.lazan.maven.transform.Transformer;

public class ProjectTransformModelImpl implements ProjectTransformModel {
    private final Project project;
    private Function<ProjectContext, CharSequence> outputPathFunction;
    private List<Transformer> transformers = new ArrayList<>();
    private Map<String, Function<ProjectContext, Object>> contextFunctions = new LinkedHashMap<>();

    public ProjectTransformModelImpl(Project project) {
        this.project = project;
    }

    @Override
    public void outputPath(Function<ProjectContext, CharSequence> outputPathFunction) {
        this.outputPathFunction = outputPathFunction;
    }

    @Override
    public void freemarkerTransform(String templatePath) {
        transform(new FreemarkerTransformer(project, templatePath));
    }

    @Override
    public void transform(Transformer transformer) {
        transformers.add(transformer);
    }

    @Override
    public void context(String contextKey, Function<ProjectContext, Object> contextFunction) {
        contextFunctions.put(contextKey, contextFunction);
    }

    public Function<ProjectContext, CharSequence> getOutputPathFunction() {
        return outputPathFunction;
    }

    public List<Transformer> getTransformers() {
        return transformers;
    }

    public Map<String, Function<ProjectContext, Object>> getContextFunctions() {
        return contextFunctions;
    }
}
