package com.lazan.maven.transform.internal;

import java.io.File;

import org.apache.maven.model.Model;

import com.lazan.maven.transform.ProjectContext;

/**
 * Created by Lance on 15/11/2017.
 */
public class ProjectContextImpl implements ProjectContext {
    private final File pomXml;
    private final Model project;
    private final String gav;
    
    public ProjectContextImpl(File pomXml, Model project) {
		super();
		this.pomXml = pomXml;
		this.project = project;
		this.gav = String.format("%s:%s:%s", project.getGroupId(), project.getArtifactId(), project.getVersion());
	}

	@Override
    public Model getProject() {
        return project;
    }

    @Override
    public File getPomXml() {
    	return pomXml;
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
    
    /**
     * TODO: make this relative to the root pom.xml
     */
    @Override
    public String getProjectPath() {
    	return String.format(":%s", getArtifactId());
    }

    @Override
	public String getGav() {
		return gav;
	}
}
