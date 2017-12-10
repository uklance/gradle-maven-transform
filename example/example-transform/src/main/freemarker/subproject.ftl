dependencies {
<#list projectContext.project.dependencies as dep>
	<#assign configuration=dependencyUtil.getConfiguration(dep)>
	<#assign notation=dependencyUtil.getNotation(dep)>
	${configuration} ${notation}
</#list> 
}