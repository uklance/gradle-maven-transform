package com.lazan.maven.transform;

import java.util.Collection;

public interface DependencyAggregator {
	boolean isCommonDependencyVersion(String groupId, String artifactId);
	String getCommonDependencyVersion(String groupId, String artifactId);
	Collection<GroupArtifactVersion> getCommonDependencyVersions();
}
