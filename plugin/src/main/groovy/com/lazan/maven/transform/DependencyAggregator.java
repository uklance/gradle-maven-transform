package com.lazan.maven.transform;

import java.util.Collection;

public interface DependencyAggregator {
	boolean isAggregatedDependency(String groupId, String artifactId);
	AggregatedDependency getAggregatedDependency(String groupId, String artifactId);
	Collection<AggregatedDependency> getAggregatedDependencies();
}
