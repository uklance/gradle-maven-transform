dependencies {
<#list projectContext.project.dependencies as d>
	<#if projectsContext.isProject(d)>
		<#assign dPath=projectsContext.getProjectContext(d).projectPath>
		compile project('${dPath}')
	<#else>
	    compile '${d.groupId}:${d.artifactId}:${d.version}'
	</#if>
</#list> 
}