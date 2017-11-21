package com.lazan.maven.transform.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import com.lazan.maven.transform.DependencyVersionAggregator;
import com.lazan.maven.transform.GroupArtifactVersion;
import com.lazan.maven.transform.ProjectsContext;

public class DependencyVersionAggregatorImpl implements DependencyVersionAggregator {
	private final Map<String, Set<String>> gavMap;
	
	public DependencyVersionAggregatorImpl(ProjectsContext projectsContext) {
		gavMap = new LinkedHashMap<>();
		for (Model project : projectsContext.getProjects()) {
			for (Dependency dep : project.getDependencies()) {
				if (!projectsContext.isProject(dep)) {
					String key = createKey(dep.getGroupId(), dep.getArtifactId());
					Set<String> versions = gavMap.get(key);
					if (versions == null) {
						versions = new LinkedHashSet<>();
						gavMap.put(key, versions);
					}
					versions.add(dep.getVersion());
				}
			}
		}
	}

	private String createKey(String groupId, String artifactId) {
		return String.format("%s:%s", groupId, artifactId);
	}
	
	@Override
	public boolean isCommonDependencyVersion(String groupId, String artifactId) {
		Set<String> versions = gavMap.get(createKey(groupId, artifactId));
		return versions != null && versions.size() == 1;
	}
	
	@Override
	public String getCommonDependencyVersion(String groupId, String artifactId) {
		Set<String> versions = gavMap.get(createKey(groupId, artifactId));
		if (versions == null || versions.size() != 1) {
			throw new RuntimeException(
					String.format("%s:%s does not have a common version %s", groupId, artifactId, versions));
		}
		return versions.iterator().next();
	}
	
	@Override
	public Collection<GroupArtifactVersion> getCommonDependencyVersions() {
		List<GroupArtifactVersion> gavs = new ArrayList<>(gavMap.size());
		for (Map.Entry<String, Set<String>> entry : gavMap.entrySet()) {
			if (entry.getValue().size() == 1) {
				String key = entry.getKey();
				int colonIndex = key.indexOf(':');
				String groupId = key.substring(0, colonIndex);
				String artifactId = key.substring(colonIndex + 1);
				String version = entry.getValue().iterator().next();
				
				gavs.add(new GroupArtifactVersion() {
					@Override
					public String getGroupId() {
						return groupId;
					}
					
					@Override
					public String getArtifactId() {
						return artifactId;
					}
					
					@Override
					public String getVersion() {
						return version;
					}
				});
			}
		}
		return gavs;
	}
}
