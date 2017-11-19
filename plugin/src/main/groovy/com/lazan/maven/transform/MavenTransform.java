package com.lazan.maven.transform;

import groovy.lang.Closure;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.*;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

import com.lazan.maven.transform.internal.ProjectsTransformModelImpl;
import com.lazan.maven.transform.internal.ProjectTransformModelImpl;
import com.lazan.maven.transform.internal.ProjectContextImpl;
import com.lazan.maven.transform.internal.ProjectsContextImpl;

import java.io.*;
import java.util.*;
import java.util.function.Function;


public class MavenTransform extends DefaultTask {
    private FileCollection pomXmlCollection;
    private List<ProjectTransformModelImpl> projectTransformModels = new ArrayList<>();
    private List<ProjectsTransformModelImpl> projectsTransformModels = new ArrayList<>();
    private File outputDirectory;
    private List<FileCollection> classpaths = new ArrayList<>();

    public void outputDirectory(Object outputDirectory) {
        this.outputDirectory = getProject().file(outputDirectory);
    }

    public void pomXml(Object pom) {
        pomXmls(pom);
    }

    public void pomXmls(Object... poms) {
        if (this.pomXmlCollection == null) {
            this.pomXmlCollection = getProject().files(poms);
        } else {
            this.pomXmlCollection = this.pomXmlCollection.plus(getProject().files(poms));
        }
    }

    @InputFiles
    public FileCollection getPoms() {
        return pomXmlCollection;
    }
    
    @InputFiles
    public List<FileCollection> getClasspaths() {
		return classpaths;
	}

    @OutputDirectory
    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void projectTransform(Closure configureClosure) {
        ProjectTransformModelImpl model = new ProjectTransformModelImpl(getProject());
        ConfigureUtil.configure(configureClosure, model);
        projectTransformModels.add(model);
        if (model.getClasspath() != null) {
        	classpaths.add(model.getClasspath());
        }
    }

    public void projectsTransform(Closure configureClosure) {
        ProjectsTransformModelImpl model = new ProjectsTransformModelImpl(getProject());
        ConfigureUtil.configure(configureClosure, model);
        projectsTransformModels.add(model);
        if (model.getClasspath() != null) {
        	classpaths.add(model.getClasspath());
        }
    }

    @TaskAction
    public void pomTransform() throws Exception {
        Project project = getProject();
        DefaultModelBuilderFactory factory = new DefaultModelBuilderFactory();
        DefaultModelBuilder builder = factory.newInstance();

        List<ProjectContext> projectContexts = new ArrayList<>();
        for (File pomXml : pomXmlCollection.getFiles()) {
            ModelBuildingRequest req = new DefaultModelBuildingRequest();
            req.setProcessPlugins(false);
            req.setPomFile(pomXml);
            req.setModelResolver(createModelResolver());
            req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);

            Model effectivePom = builder.build(req).getEffectiveModel();
            
            projectContexts.add(new ProjectContextImpl(pomXml, effectivePom));
        }

        ProjectsContext projectsContext = new ProjectsContextImpl(projectContexts);
        for (ProjectsTransformModelImpl model : projectsTransformModels) {
            File outFile = new File(outputDirectory, model.getOutputPath());
            Map<String, Object> templateContext = new LinkedHashMap<>();
            templateContext.put("projectsContext", projectsContext);
            for (Map.Entry<String, Function<ProjectsContext, Object>> entry : model.getContextFunctions().entrySet()) {
                templateContext.put(entry.getKey(), entry.getValue().apply(projectsContext));
            }
            try (OutputStream out = new FileOutputStream(outFile)) {
                for (Template template : model.getTemplates()) {
                    template.transform(templateContext, model.getClassLoader(), out);
                }
                out.flush();
                project.getLogger().lifecycle("Wrote to {}", outFile);
            }
        }
        for (ProjectContext projectContext : projectContexts) {
            Map<String, Object> templateContext = new LinkedHashMap<>();
            templateContext.put("projectsContext", projectsContext);
            templateContext.put("projectContext", projectContext);
            for (ProjectTransformModelImpl model : projectTransformModels) {
                String path = model.getOutputPathFunction().apply(projectContext.getProject());
                File outFile = new File(outputDirectory, path);
                for (Map.Entry<String, Function<ProjectContext, Object>> entry : model.getContextFunctions().entrySet()) {
                    templateContext.put(entry.getKey(), entry.getValue().apply(projectContext));
                }
                try (OutputStream out = new FileOutputStream(outFile)) {
                    for (Template template : model.getTemplates()) {
                        template.transform(templateContext, model.getClassLoader(), out);
                    }
                    out.flush();
                    project.getLogger().lifecycle("Wrote to {}", outFile);
                }
            }
        }
    }

    protected ModelResolver createModelResolver() {
        return new ModelResolver() {
            @Override
            public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
                String configName = String.format("pomTransform%s", UUID.randomUUID());
                Configuration config = getProject().getConfigurations().create(configName);
                config.setTransitive(false);
                String depNotation = String.format("%s:%s:%s@pom", groupId, artifactId, version);
                org.gradle.api.artifacts.Dependency dependency = getProject().getDependencies().create(depNotation);
                config.getDependencies().add(dependency);

                File pomXml = config.getSingleFile();
                return new ModelSource() {
                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new FileInputStream(pomXml);
                    }

                    @Override
                    public String getLocation() {
                        return pomXml.getAbsolutePath();
                    }
                };
            }

            @Override
            public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
                return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
            }

            @Override
            public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException {
                return resolveModel(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
            }

            @Override
            public void addRepository(Repository repository) throws InvalidRepositoryException {
                // ignore
            }

            @Override
            public void addRepository(Repository repository, boolean replace) throws InvalidRepositoryException {
                // ignore
            }

            @Override
            public ModelResolver newCopy() {
                throw new UnsupportedOperationException("newCopy");
            }
        };
    }
}
