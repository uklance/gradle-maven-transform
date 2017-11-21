<#assign dva=dependencyVersionAggregator>
ext {
	versions = [
	<#list dva.commonDependencyVersions as gav>
		'${gav.artifactId}': '${gav.version}'<#if gav?has_next>,</#if>
	</#list>
	]
}