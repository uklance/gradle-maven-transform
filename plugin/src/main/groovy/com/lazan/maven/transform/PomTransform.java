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

import java.io.*;
import java.util.*;
import java.util.function.Function;


public class PomTransform extends DefaultTask {
    private FileCollection poms;
    private List<OneToOneModelImpl> oneToOneModels = new ArrayList<>();
    private List<ManyToOneModelImpl> manyToOneModels = new ArrayList<>();
    private File outputDirectory;

    public void outputDirectory(Object outputDirectory) {
        this.outputDirectory = getProject().file(outputDirectory);
    }

    public void pom(Object pom) {
        poms(pom);
    }

    public void poms(Object... poms) {
        if (this.poms == null) {
            this.poms = getProject().files(poms);
        } else {
            this.poms = this.poms.plus(getProject().files(poms));
        }
    }

    @InputFiles
    public FileCollection getPoms() {
        return poms;
    }

    @OutputDirectory
    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void oneToOne(Closure configureClosure) {
        OneToOneModelImpl model = new OneToOneModelImpl(getProject());
        ConfigureUtil.configure(configureClosure, model);
        oneToOneModels.add(model);
    }

    public void manyToOne(Closure configureClosure) {
        ManyToOneModelImpl model = new ManyToOneModelImpl(getProject());
        ConfigureUtil.configure(configureClosure, model);
        manyToOneModels.add(model);
    }

    @TaskAction
    public void pomTransform() throws Exception {
        Project project = getProject();
        DefaultModelBuilderFactory factory = new DefaultModelBuilderFactory();
        DefaultModelBuilder builder = factory.newInstance();

        Map<File, Model> pomModelMap = new LinkedHashMap<>();
        for (File pomFile : poms.getFiles()) {
            ModelBuildingRequest req = new DefaultModelBuildingRequest();
            req.setProcessPlugins(false);
            req.setPomFile(pomFile);
            req.setModelResolver(createModelResolver());
            req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);

            Model effectivePom = builder.build(req).getEffectiveModel();
            pomModelMap.put(pomFile, effectivePom);
        }

        ProjectsContext projectsContext = new ProjectsContextImpl(pomModelMap);
        Collection<Model> pomModels = pomModelMap.values();
        for (ManyToOneModelImpl model : manyToOneModels) {
            File outFile = new File(outputDirectory, model.getOutputPath());
            Map<String, Object> templateContext = new LinkedHashMap<>();
            templateContext.put("context", projectsContext);
            for (Map.Entry<String, Function<ProjectsContext, Object>> entry : model.getContextFunctions().entrySet()) {
                templateContext.put(entry.getKey(), entry.getValue().apply(projectsContext));
            }
            try (OutputStream out = new FileOutputStream(outFile)) {
                for (Template template : model.getTemplates()) {
                    template.transform(templateContext, out);
                }
                out.flush();
                project.getLogger().lifecycle("Wrote to {}", outFile);
            }
        }
        for (Model pomModel : pomModelMap.values()) {
            ProjectContext projectContext = new ProjectContextImpl(pomModelMap, pomModel);
            Map<String, Object> templateContext = new LinkedHashMap<>();
            templateContext.put("context", projectContext);
            for (OneToOneModelImpl model : oneToOneModels) {
                String path = model.getOutputPathFunction().apply(pomModel).toString();
                File outFile = new File(outputDirectory, path);
                for (Map.Entry<String, Function<ProjectContext, Object>> entry : model.getContextFunctions().entrySet()) {
                    templateContext.put(entry.getKey(), entry.getValue().apply(projectContext));
                }
                try (OutputStream out = new FileOutputStream(outFile)) {
                    for (Template template : model.getTemplates()) {
                        template.transform(templateContext, out);
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

                File pomFile = config.getSingleFile();
                return new ModelSource() {
                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new FileInputStream(pomFile);
                    }

                    @Override
                    public String getLocation() {
                        return pomFile.getAbsolutePath();
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
