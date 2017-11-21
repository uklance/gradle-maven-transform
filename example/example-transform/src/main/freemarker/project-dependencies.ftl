dependencies {
<#list projectContext.project.dependencies as d>
	<#assign configuration='compile'>
	<#if d.scope??>
		<#if d.scope == 'test'>
			<#assign configuration='testCompile'>
		<#elseif d.scope == 'provided'>
			<#assign configuration='compileOnly'>
		<#elseif d.scope == 'runtime'>
			<#assign configuration='runtime'>
		</#if>
	</#if>
	<#if projectsContext.isProject(d)>
		<#assign dPath=projectsContext.getProjectContext(d).projectPath>
		${configuration} project('${dPath}')
	<#else>
	    ${configuration} '${d.groupId}:${d.artifactId}:${d.version}'
	</#if>
</#list> 
}