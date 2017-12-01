ext {
	versions = [
<#list dependencyAggregator.aggregatedDependencies as aggDep>
		${aggDep.variableName}: '${aggDep.version}'<#if aggDep?has_next>,</#if>
</#list>
	]
}