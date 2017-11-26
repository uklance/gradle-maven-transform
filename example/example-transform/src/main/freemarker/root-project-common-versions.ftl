ext {
	versions = [
	<#list dependencyAggregator.commonDependencyVersions as gav>
		'${gav.artifactId}': '${gav.version}'<#if gav?has_next>,</#if>
	</#list>
	]
}