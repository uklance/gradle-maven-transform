package com.lazan.maven.transform;

import org.gradle.api.file.FileCollection;
import org.apache.maven.model.Model;
import java.util.Collection;
import java.util.function.Function;

public interface ProjectsTransformModel {
    void outputPath(String outputPath);
    void freemarkerTemplate(String templatePath);
    void template(Template template);
    void context(String contextKey, Function<ProjectsContext, Object> contextFunction);
}
