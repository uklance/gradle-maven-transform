package com.lazan.maven.transform;

import org.gradle.api.file.FileCollection;
import org.gradle.internal.impldep.org.apache.maven.model.Model;

import java.util.Collection;
import java.util.function.Function;

public interface ManyToOneModel {
    void classpath(FileCollection classpath);
    void outputFile(Object outputFile);
    void freemarkerTemplate(String templatePath);
    void template(Template template);
    void context(String contextKey, Function<Collection<Model>, Object> contextFunction);
}
