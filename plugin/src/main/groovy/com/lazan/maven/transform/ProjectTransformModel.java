package com.lazan.maven.transform;

import java.util.function.Function;

import org.apache.maven.model.Model;

/**
 * Created by Lance on 11/11/2017.
 */
public interface ProjectTransformModel {
    void outputPath(Function<Model, String> outputFileFunction);
    void freemarkerTemplate(String templatePath);
    void template(Template template);
    void context(String contextKey, Function<ProjectContext, Object> contextFunction);
}
