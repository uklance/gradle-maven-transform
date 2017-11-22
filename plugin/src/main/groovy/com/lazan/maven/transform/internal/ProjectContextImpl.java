package com.lazan.maven.transform.internal;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.maven.model.Model;

import com.lazan.maven.transform.ProjectContext;
import com.lazan.maven.transform.ProjectsContext;

/**
 * Created by Lance on 15/11/2017.
 */
public class ProjectContextImpl implements ProjectContext {
    private final File pomXml;
    private final Model project;
    private final String gav;
	private final AtomicReference<Map<String, Object>> transformContextReference;
	private ProjectsContext projectsContext;
    
    public ProjectContextImpl(File pomXml, Model project, AtomicReference<Map<String, Object>> transformContextReference) {
		super();
		this.pomXml = pomXml;
		this.project = project;
		this.gav = String.format("%s:%s:%s", project.getGroupId(), project.getArtifactId(), project.getVersion());
		this.transformContextReference = transformContextReference;
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
    
    public void setProjectsContext(ProjectsContext projectsContext) {
		this.projectsContext = projectsContext;
	}

    @Override
	public ProjectsContext getProjectsContext() {
		return projectsContext;
	}
    
    @Override
    public Map<String, Object> getTransformContext() {
    	Map<String, Object> transformContext = transformContextReference.get();
    	if (transformContext == null) {
    		throw new IllegalStateException("transformContext not ready, please wait until all context entries initialized");
    	}
    	return transformContext;
    }
}
