package com.lazan.maven.transform;

import org.gradle.api.file.FileCollection;

/**
 * Created by Lance on 11/11/2017.
 */
public interface ClassLoaderSource {
    ClassLoader getClassLoader();
}
