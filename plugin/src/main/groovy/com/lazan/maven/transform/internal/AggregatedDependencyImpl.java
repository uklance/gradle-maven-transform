package com.lazan.maven.transform.internal;

import com.lazan.maven.transform.AggregatedDependency;

public class AggregatedDependencyImpl implements AggregatedDependency {
	private final String groupId;
	private final String artifactId;
	private final String version;
	private final String variableName;
	
	public AggregatedDependencyImpl(String groupId, String artifactId, String version, String variableName) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.variableName = variableName;
	}

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

	@Override
	public String getVariableName() {
		return variableName;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((variableName == null) ? 0 : variableName.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AggregatedDependencyImpl other = (AggregatedDependencyImpl) obj;
		if (artifactId == null) {
			if (other.artifactId != null)
				return false;
		} else if (!artifactId.equals(other.artifactId))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (variableName == null) {
			if (other.variableName != null)
				return false;
		} else if (!variableName.equals(other.variableName))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
}
