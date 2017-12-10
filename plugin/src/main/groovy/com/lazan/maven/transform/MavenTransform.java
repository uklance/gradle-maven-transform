package com.lazan.maven.transform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.resolution.ModelResolver;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

import com.lazan.maven.transform.internal.DependencyAggregatorImpl;
import com.lazan.maven.transform.internal.ModelResolverImpl;
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
    private FileCollection transformClasspath;
    
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

    public void transformClasspath(FileCollection transformClasspath) {
    	this.transformClasspath = transformClasspath;
    }
    
    @InputFiles
    public FileCollection getPoms() {
        return pomXmlCollection;
    }
    
    @InputFiles
    public FileCollection getTransformerFiles() {
    	Project project = getProject();
    	FileCollection transformerFiles = project.files();
    	for (File file : transformClasspath.getFiles()) {
    		if (file.isDirectory()) {
    			// add all files in the directory so the task is dirty if any transformer files change
    			transformerFiles = transformerFiles.plus(project.fileTree(file));
    		} else {
    			transformerFiles = transformerFiles.plus(project.files(file));
    		}
    	}
		return transformerFiles;
	}

    @OutputDirectory
    public File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Configure a one-to-one transform on each of the effective poms. Each effective
     * pom will create a result file. A {@link ProjectTransformModel} will be passed to the closure
     * @param configureClosure the closure used to configure the transformation
     */
    @SuppressWarnings("rawtypes")
	public void projectTransform(Closure configureClosure) {
        ProjectTransformModelImpl model = new ProjectTransformModelImpl(getProject());
        ConfigureUtil.configure(configureClosure, model);
        projectTransformModels.add(model);
    }

    /**
     * Configure an aggregation transform on all of the effective poms as a group such that a single result file will be 
     * created from the effective poms. A {@link ProjectsTransformModel} will be passed to the closure
     * @param configureClosure the closure used to configure the transformation
     */
    @SuppressWarnings("rawtypes")
    public void projectsTransform(Closure configureClosure) {
        ProjectsTransformModelImpl model = new ProjectsTransformModelImpl(getProject());
        ConfigureUtil.configure(configureClosure, model);
        projectsTransformModels.add(model);
    }
    
    @TaskAction
    public void mavenTransform() throws Exception {
    	AtomicReference<Map<String, Object>> transformContextReference = new AtomicReference<>();
        ProjectsContext projectsContext = createProjectsContext(transformContextReference);
        ClassLoader transformClassLoader = getTransformerClassLoader();
        applyProjectsTransforms(projectsContext, transformClassLoader, transformContextReference);
        applyProjectTransforms(projectsContext, transformClassLoader, transformContextReference);
    }

    protected ProjectsContext createProjectsContext(AtomicReference<Map<String, Object>> transformContextReference) throws Exception {
        DefaultModelBuilderFactory factory = new DefaultModelBuilderFactory();
        DefaultModelBuilder builder = factory.newInstance();
        ModelResolver modelResolver = new ModelResolverImpl(getProject(), pomXmlCollection);
        List<ProjectContextImpl> projectContexts = new ArrayList<>();
        for (File pomXml : pomXmlCollection.getFiles()) {
            ModelBuildingRequest req = new DefaultModelBuildingRequest();
            req.setProcessPlugins(false);
            req.setPomFile(pomXml);
            req.setModelResolver(modelResolver);
            req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);

            Model effectivePom = builder.build(req).getEffectiveModel();
            
            projectContexts.add(new ProjectContextImpl(pomXml, effectivePom, transformContextReference));
        }

        ProjectsContext projectsContext = new ProjectsContextImpl(projectContexts, transformContextReference);
        for (ProjectContextImpl projectContext : projectContexts) {
        	projectContext.setProjectsContext(projectsContext);
        }
        return projectsContext;    	
    }
    
    protected Map<String, Object> defaultProjectsTransformerContext(ProjectsContext projectsContext) {
    	Map<String, Object> context = new LinkedHashMap<>();
    	context.put("projectsContext", projectsContext);
    	context.put("dependencyAggregator", new DependencyAggregatorImpl(projectsContext));
    	return Collections.unmodifiableMap(context);
    }

    protected Map<String, Object> defaultProjectTransformerContext(ProjectContext projectContext) {
    	Map<String, Object> context = new LinkedHashMap<>();
    	context.put("projectContext", projectContext);
    	context.putAll(defaultProjectsTransformerContext(projectContext.getProjectsContext()));
    	return Collections.unmodifiableMap(context);
    }
    
	protected void applyProjectTransforms(ProjectsContext projectsContext, ClassLoader transformClassLoader, AtomicReference<Map<String, Object>> transformContextReference) throws Exception {
		for (ProjectContext projectContext : projectsContext.getProjectContexts()) {
            for (ProjectTransformModelImpl model : projectTransformModels) {
                String path = model.getOutputPathFunction().apply(projectContext).toString();
                File outFile = new File(outputDirectory, path);
                Map<String, Object> transformContext = new LinkedHashMap<>(defaultProjectTransformerContext(projectContext));
                for (Map.Entry<String, Function<ProjectContext, Object>> entry : model.getContextFunctions().entrySet()) {
                    transformContext.put(entry.getKey(), entry.getValue().apply(projectContext));
                }
                try (OutputStream out = new FileOutputStream(outFile)) {
                	transformContextReference.set(Collections.unmodifiableMap(transformContext));
                    for (Transformer transformer : model.getTransformers()) {
                        transformer.transform(transformContext, transformClassLoader, out);
                    }
                    out.flush();
                    getProject().getLogger().lifecycle("Wrote to {}", outFile);
                } finally {
                	transformContextReference.set(null);
                }
            }
        }
	}

	protected void applyProjectsTransforms(ProjectsContext projectsContext, ClassLoader transformClassLoader, AtomicReference<Map<String, Object>> transformContextReference) throws Exception {
		for (ProjectsTransformModelImpl model : projectsTransformModels) {
            Map<String, Object> transformContext = new LinkedHashMap<>(defaultProjectsTransformerContext(projectsContext));
            for (Map.Entry<String, Function<ProjectsContext, Object>> entry : model.getContextFunctions().entrySet()) {
                transformContext.put(entry.getKey(), entry.getValue().apply(projectsContext));
            }
            File outFile = new File(outputDirectory, model.getOutputPath());
            try (OutputStream out = new FileOutputStream(outFile)) {
            	transformContextReference.set(Collections.unmodifiableMap(transformContext));
                for (Transformer transformer : model.getTransformers()) {
                    transformer.transform(transformContext, transformClassLoader, out);
                }
                out.flush();
                getProject().getLogger().lifecycle("Wrote to {}", outFile);
            } finally {
            	transformContextReference.set(null);
            }
        }
	}
    
    protected ClassLoader getTransformerClassLoader() {
        Function<File, URL> toUrl = (File file) -> {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        };
        URL[] urls = transformClasspath.getFiles().stream().map(toUrl).toArray(URL[]::new);
        return new URLClassLoader(urls, null);
    }
}
