package com.lazan.maven.transform;

import org.apache.maven.model.Model;

import java.io.File;
import java.util.Collection;

/**
 * Created by Lance on 15/11/2017.
 */
public interface ProjectsContext {
    Collection<Model> getProjects();
    File getPomXml(Model project);
}
