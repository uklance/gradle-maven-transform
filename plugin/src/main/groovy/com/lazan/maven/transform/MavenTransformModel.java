package com.lazan.maven.transform;

import groovy.lang.Closure;
import org.gradle.api.Action;

public interface MavenTransformModel {
    void rootPom(Object rootPom);
    void transformManyToOne(Closure configureClosure);
    void transformOneToOne(Closure configureClosure);
    void outputDirectory(Object outputDir);
}
