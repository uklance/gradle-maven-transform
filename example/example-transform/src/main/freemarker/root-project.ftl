ext {
	versions = [
<#list dependencyAggregator.commonDependencyVersions as gav>
	<#assign versionName=dependencyAggregator.getCommonDependencyVersionName(gav.groupId, gav.artifactId)>
		${versionName}: '${gav.version}'<#if gav?has_next>,</#if>
</#list>
	]
}