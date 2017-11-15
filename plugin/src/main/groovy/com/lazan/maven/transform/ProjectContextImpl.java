package com.lazan.maven.transform;

import org.apache.maven.model.Model;

import java.io.File;
import java.util.Map;

/**
 * Created by Lance on 15/11/2017.
 */
public class ProjectContextImpl extends ProjectsContextImpl implements ProjectContext {
    private final Model project;

    public ProjectContextImpl(Map<File, Model> modelMap, Model project) {
        super(modelMap);
        this.project = project;
    }

    @Override
    public Model getProject() {
        return project;
    }

    @Override
    public File getPomXml() {
        return getPomXml(project);
    }

    @Override
    public String getGroupId() {
        return project.getGroupId();
    }

    @Override
    public String getArtifactId() {
        return project.getArtifactId();
    }

    @Override
    public String getVersion() {
        return project.getVersion();
    }
}
