package com.lazan.maven.transform;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by Lance on 12/11/2017.
 */
public class MavenTransformModelImpl implements MavenTransformModel {
    private final Project project;
    private final EffectivePom effectivePom;
    private final EffectivePomTransform pomTransform;
    private File outputDirectory;

    public MavenTransformModelImpl(Project project, EffectivePom effectivePom, EffectivePomTransform pomTransform) {
        this.project = project;
        this.effectivePom = effectivePom;
        this.pomTransform = pomTransform;
    }

    @Override
    public void rootPom(Object rootPom) {
        effectivePom.rootPom(rootPom);
    }

    @Override
    public void transformManyToOne(Closure configureClosure) {
        pomTransform.manyToOne(configureClosure);
    }

    @Override
    public void transformOneToOne(Closure configureClosure) {
        pomTransform.oneToOne(configureClosure);
    }

    public Project getProject() {
        return project;
    }

    public EffectivePom getEffectivePom() {
        return effectivePom;
    }

    public EffectivePomTransform getPomTransform() {
        return pomTransform;
    }

    public ClassLoader getClassLoader(FileCollection classpath) {
        Function<File, URL> toUrl = (File file) -> {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        };
        Set<File> files = classpath.getFiles();
        URL[] urls = classpath.getFiles().stream().map(toUrl).toArray(URL[]::new);
        return new URLClassLoader(urls, null);
    }

    @Override
    public void outputDirectory(Object outputDir) {
        pomTransform.outputDirectory(outputDir);
    }
}
