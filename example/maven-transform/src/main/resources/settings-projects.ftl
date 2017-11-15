<#list context.projects as project>
include ':${project.artifactId}'
</#list>