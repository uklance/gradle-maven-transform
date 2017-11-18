package com.lazan.maven.transform.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

	public ProjectsContextImpl(Collection<ProjectContext> projectContexts) {
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
}
