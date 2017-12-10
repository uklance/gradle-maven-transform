dependencyRecommendations {
   map recommendations: [
<#list dependencyAggregator.aggregatedDependencies as aggDep>
		'${aggDep.groupId}:${aggDep.artifactId}': '${aggDep.version}'<#if aggDep?has_next>,</#if>
</#list>
   ]
}