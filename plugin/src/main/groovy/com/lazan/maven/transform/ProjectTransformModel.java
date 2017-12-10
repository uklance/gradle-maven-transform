package com.lazan.maven.transform;

import java.util.function.Function;

/**
 * Created by Lance on 11/11/2017.
 */
public interface ProjectTransformModel {
    void outputPath(Function<ProjectContext, CharSequence> outputFileFunction);
    void freemarkerTransform(String templatePath);
    void transform(Transformer transformer);
    void context(String contextKey, Function<ProjectContext, Object> contextFunction);
}
