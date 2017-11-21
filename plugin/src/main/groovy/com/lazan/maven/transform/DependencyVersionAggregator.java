package com.lazan.maven.transform;

import java.util.Collection;

public interface DependencyVersionAggregator {
	boolean isCommonDependencyVersion(String groupId, String artifactId);
	String getCommonDependencyVersion(String groupId, String artifactId);
	Collection<GroupArtifactVersion> getCommonDependencyVersions();
}
