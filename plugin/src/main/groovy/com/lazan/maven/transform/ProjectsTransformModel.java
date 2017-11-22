package com.lazan.maven.transform;

import java.util.function.Function;

public interface ProjectsTransformModel {
    void outputPath(String outputPath);
    void freemarkerTransform(String templatePath);
    void transform(Transformer transformer);
    void context(String contextKey, Function<ProjectsContext, Object> contextFunction);
}
