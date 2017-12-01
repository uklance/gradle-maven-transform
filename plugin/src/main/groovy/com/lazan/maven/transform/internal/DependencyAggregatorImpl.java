package com.lazan.maven.transform.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import com.lazan.maven.transform.AggregatedDependency;
import com.lazan.maven.transform.DependencyAggregator;
import com.lazan.maven.transform.ProjectsContext;

public class DependencyAggregatorImpl implements DependencyAggregator {
	private final Map<String, String> groupVersions;
	private final Map<List<String>, String> groupArtifactVersions;
	
	public DependencyAggregatorImpl(ProjectsContext projectsContext) {
		Map<String, Set<String>> tempGroupVersions = new LinkedHashMap<>();
		Map<List<String>, Set<String>> tempGroupArtifactVersions = new LinkedHashMap<>();
		
		for (Model project : projectsContext.getProjects()) {
			for (Dependency dep : project.getDependencies()) {
				multiMapAdd(tempGroupVersions, dep.getGroupId(), dep.getVersion());
				multiMapAdd(tempGroupArtifactVersions, Arrays.asList(dep.getGroupId(), dep.getArtifactId()), dep.getVersion());
			}
		}
		
		groupVersions = new LinkedHashMap<>();
		groupArtifactVersions = new LinkedHashMap<>();
		
		for (Map.Entry<String, Set<String>> entry : tempGroupVersions.entrySet()) {
			Set<String> versions = entry.getValue();
			if (versions.size() == 1) {
				groupVersions.put(entry.getKey(), versions.iterator().next());
			}
		}
		for (Map.Entry<List<String>, Set<String>> entry : tempGroupArtifactVersions.entrySet()) {
			List<String> key = entry.getKey();
			Set<String> versions = entry.getValue();
			String groupId = key.get(0);
			if (versions.size() == 1 && !groupVersions.containsKey(groupId)) {
				groupArtifactVersions.put(key, versions.iterator().next());
			}
		}
	}
	
	protected <T> void multiMapAdd(Map<T, Set<String>> map, T key, String value) {
		Set<String> values = map.get(key);
		if (values == null) {
			values = new LinkedHashSet<>();
			map.put(key,  values);
		}
		values.add(value);
	}

	@Override
	public boolean isAggregatedDependency(String groupId, String artifactId) {
		return groupVersions.containsKey(groupId) || groupArtifactVersions.containsKey(createKey(groupId, artifactId));
	}
	
	@Override
	public AggregatedDependency getAggregatedDependency(String groupId, String artifactId) {
		if (groupVersions.containsKey(groupId)) {
			String version = groupVersions.get(groupId);
			String variableName = toVariableName(groupId);
			return new AggregatedDependencyImpl(groupId, null, version, variableName);
		}
		List<String> key = createKey(groupId, artifactId);
		if (groupArtifactVersions.containsKey(key)) {
			String version = groupArtifactVersions.get(key);
			String variableName = toVariableName(artifactId);
			return new AggregatedDependencyImpl(groupId, artifactId, version, variableName);
		}
		throw new RuntimeException(String.format("%s:%s is not an aggregated dependency", groupId, artifactId));
	}

	private List<String> createKey(String groupId, String artifactId) {
		return Arrays.asList(groupId, artifactId);
	}
	
	@Override
	public Collection<AggregatedDependency> getAggregatedDependencies() {
		List<AggregatedDependency> aggregated = new ArrayList<>();
		for (Map.Entry<String, String> entry : groupVersions.entrySet()) {
			String groupId = entry.getKey();
			String variableName = toVariableName(groupId);
			String version = entry.getValue();
			aggregated.add(new AggregatedDependencyImpl(groupId, null, version, variableName));
		}
		for (Map.Entry<List<String>, String> entry : groupArtifactVersions.entrySet()) {
			String groupId = entry.getKey().get(0);
			String artifactId = entry.getKey().get(1);
			String variableName = toVariableName(artifactId);
			String version = entry.getValue();
			aggregated.add(new AggregatedDependencyImpl(groupId, artifactId, version, variableName));
		}
		return aggregated;
	}
	
	protected String toVariableName(String value) {
		StringBuilder builder = new StringBuilder();
		boolean nextUpper = false;
		for (int i = 0; i < value.length(); ++i) {
			char ch = value.charAt(i);
			boolean validChar = Character.isLetter(ch);
			if (!validChar && builder.length() > 0) {
				validChar = Character.isDigit(ch);
			}
			if (!validChar) {
				if (builder.length() > 0) {
					nextUpper = true;
				}
				continue;
			}
			if (nextUpper) {
				builder.append(Character.toUpperCase(ch));
				nextUpper = false;
			} else {
				builder.append(ch);
			}
		}
		return builder.toString();
	}
}
