package com.lazan.maven.transform;

import org.gradle.api.file.FileCollection;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by Lance on 11/11/2017.
 */
public class FreemarkerTemplate implements Template {
    private final ClasspathSource classpathSource;
    private final String templatePath;
    public FreemarkerTemplate(ClasspathSource classpathSource, String templatePath) {
        this.classpathSource = classpathSource;
        this.templatePath = templatePath;
    }

    @Override
    public void transform(Map<String, Object> context, OutputStream out) throws IOException {
        FileCollection classpath = classpathSource.getClasspath();
        throw new RuntimeException("Not implemented");
    }
}
