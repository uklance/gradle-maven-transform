package com.lazan.maven.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelSource;
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

import com.lazan.maven.transform.internal.ProjectContextImpl;
import com.lazan.maven.transform.internal.ProjectTransformModelImpl;
import com.lazan.maven.transform.internal.ProjectsContextImpl;
import com.lazan.maven.transform.internal.ProjectsTransformModelImpl;

import groovy.lang.Closure;


public class MavenTransform extends DefaultTask {
    private FileCollection pomXmlCollection;
    private List<ProjectTransformModelImpl> projectTransformModels = new ArrayList<>();
    private List<ProjectsTransformModelImpl> projectsTransformModels = new ArrayList<>();
    private File outputDirectory;
    private FileCollection templateClasspath;
    
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
    public FileCollection getTemplateClasspath() {
		return templateClasspath;
	}

    @OutputDirectory
    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void projectTransform(Closure configureClosure) {
        ProjectTransformModelImpl model = new ProjectTransformModelImpl(getProject());
        ConfigureUtil.configure(configureClosure, model);
        projectTransformModels.add(model);
    }

    public void projectsTransform(Closure configureClosure) {
        ProjectsTransformModelImpl model = new ProjectsTransformModelImpl(getProject());
        ConfigureUtil.configure(configureClosure, model);
        projectsTransformModels.add(model);
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
        ClassLoader templateClassLoader = getTemplateClassLoader();
        for (ProjectsTransformModelImpl model : projectsTransformModels) {
            File outFile = new File(outputDirectory, model.getOutputPath());
            Map<String, Object> templateContext = new LinkedHashMap<>();
            templateContext.put("projectsContext", projectsContext);
            for (Map.Entry<String, Function<ProjectsContext, Object>> entry : model.getContextFunctions().entrySet()) {
                templateContext.put(entry.getKey(), entry.getValue().apply(projectsContext));
            }
            try (OutputStream out = new FileOutputStream(outFile)) {
                for (Template template : model.getTemplates()) {
                    template.transform(templateContext, templateClassLoader, out);
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
                        template.transform(templateContext, templateClassLoader, out);
                    }
                    out.flush();
                    project.getLogger().lifecycle("Wrote to {}", outFile);
                }
            }
        }
    }
    
    public void templateClasspath(FileCollection templateClasspath) {
    	this.templateClasspath = templateClasspath;
    }
    
    
    protected ClassLoader getTemplateClassLoader() {
        Function<File, URL> toUrl = (File file) -> {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        };
        URL[] urls = templateClasspath.getFiles().stream().map(toUrl).toArray(URL[]::new);
        return new URLClassLoader(urls, null);
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
