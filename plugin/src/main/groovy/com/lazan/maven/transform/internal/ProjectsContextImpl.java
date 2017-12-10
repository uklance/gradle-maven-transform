package com.lazan.maven.transform.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import com.lazan.maven.transform.ProjectContext;
import com.lazan.maven.transform.ProjectsContext;

/**
 * Created by Lance on 15/11/2017.
 */
public class ProjectsContextImpl implements ProjectsContext {
	private final Map<String, ProjectContext> projectContextMap;
	private final List<Model> projects;
	private final AtomicReference<Map<String, Object>> transformContextReference;

	public ProjectsContextImpl(Collection<? extends ProjectContext> projectContexts, AtomicReference<Map<String, Object>> transformContextReference) {
		super();
		Map<String, ProjectContext> map = new LinkedHashMap<>();
		for (ProjectContext projectContext : projectContexts) {
			String gav = projectContext.getGav();
			if (map.containsKey(gav)) {
				throw new RuntimeException("Duplicate GAV " + gav);
			}
			map.put(gav,  projectContext);
		}
		projectContextMap = Collections.unmodifiableMap(map);
		projects = projectContexts.stream().map(ProjectContext::getProject).collect(Collectors.toList());
		this.transformContextReference = transformContextReference;
	}
	
	@Override
	public ProjectContext getProjectContext(Dependency dependency) {
		String gav = getGav(dependency);
		if (!projectContextMap.containsKey(gav)) {
			throw new RuntimeException(String.format("%s is not a project. Found %s", gav, projectContextMap.keySet()));
		}
		return projectContextMap.get(gav);
	}
	
	@Override
	public Model getProject(Dependency dependency) {
		return getProjectContext(dependency).getProject();
	}
	
	@Override
	public boolean isProject(Dependency dependency) {
		return projectContextMap.containsKey(getGav(dependency));
	}
	
	@Override
	public Collection<ProjectContext> getProjectContexts() {
		return projectContextMap.values();
	}
	
	@Override
	public Collection<Model> getProjects() {
		return projects;
	}
	
	protected String getGav(Dependency dependency) {
		return String.format("%s:%s:%s", dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
	}
	
    @Override
    public Map<String, Object> getTransformContext() {
    	Map<String, Object> transformContext = transformContextReference.get();
    	if (transformContext == null) {
    		throw new IllegalStateException("transformContext not ready, please wait until all context entries initialized");
    	}
    	return transformContext;
    }
    
    @Override
    public Object getTransformContext(String name) {
    	Map<String, Object> contextMap = getTransformContext();
    	if (!contextMap.containsKey(name)) {
    		throw new IllegalArgumentException("Invalid transform context name " + name);
    	}
    	return contextMap.get(name);
    }

    @Override
    public <T> T getTransformContext(String name, Class<T> type) {
    	return type.cast(getTransformContext(name));
    }
}
