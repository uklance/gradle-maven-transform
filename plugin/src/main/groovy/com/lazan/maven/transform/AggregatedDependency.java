package com.lazan.maven.transform;

public interface AggregatedDependency {
	String getGroupId();
	String getArtifactId();
	String getVersion();
	String getVariableName();
}
