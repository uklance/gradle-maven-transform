package com.lazan.maven.transform;

import org.apache.maven.model.Model;

import java.io.File;
import java.util.Collection;

/**
 * Created by Lance on 15/11/2017.
 */
public interface ProjectContext extends ProjectsContext {
    Model getProject();
    File getPomXml();
    String getGroupId();
    String getArtifactId();
    String getVersion();
}
