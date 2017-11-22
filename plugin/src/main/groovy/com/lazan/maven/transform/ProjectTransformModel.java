package com.lazan.maven.transform;

import java.util.function.Function;

import org.apache.maven.model.Model;

/**
 * Created by Lance on 11/11/2017.
 */
public interface ProjectTransformModel {
    void outputPath(Function<Model, CharSequence> outputFileFunction);
    void freemarkerTransform(String templatePath);
    void transform(Transformer transformer);
    void context(String contextKey, Function<ProjectContext, Object> contextFunction);
}
