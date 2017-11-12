package com.lazan.maven.transform;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Created by Lance on 11/11/2017.
 */
public class FreemarkerTemplate implements Template {
    private final ClassLoaderSource classLoaderSource;
    private final String templatePath;
    public FreemarkerTemplate(ClassLoaderSource classLoaderSource, String templatePath) {
        this.classLoaderSource = classLoaderSource;
        this.templatePath = templatePath;
    }

    @Override
    public void transform(Map<String, Object> context, OutputStream out) throws IOException {
        ClassLoader classLoader = classLoaderSource.getClassLoader();
        Configuration fmConfig = new Configuration(Configuration.VERSION_2_3_23);
        fmConfig.setTemplateLoader(new ClassTemplateLoader(classLoader, ""));

        freemarker.template.Template template = fmConfig.getTemplate(templatePath);
        try (Writer writer = new OutputStreamWriter(out)) {
            template.process(context, writer);
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Error processing " + templatePath, e);
        }
    }
}
