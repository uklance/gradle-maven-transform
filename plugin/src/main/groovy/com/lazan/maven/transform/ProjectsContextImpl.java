package com.lazan.maven.transform;

import org.apache.maven.model.Model;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Lance on 15/11/2017.
 */
public class ProjectsContextImpl implements ProjectsContext {
    private final Collection<Model> models;
    private final Map<String, File> pomXmlByGav;

    public ProjectsContextImpl(Map<File, Model> modelMap) {
        this.models = modelMap.values();
        this.pomXmlByGav = new LinkedHashMap<>();
        for (Map.Entry<File, Model> entry : modelMap.entrySet()) {
            Model project = entry.getValue();
            String gav = getGav(project);
            if (pomXmlByGav.containsKey(gav)) {
                throw new RuntimeException("Duplicate GAV " + gav);
            }
            pomXmlByGav.put(gav, entry.getKey());
        }
    }

    @Override
    public Collection<Model> getProjects() {
        return models;
    }

    @Override
    public File getPomXml(Model project) {
        return pomXmlByGav.get(getGav(project));
    }

    protected String getGav(Model project) {
        return String.format("%s:%s:%s", project.getGroupId(), project.getArtifactId(), project.getVersion());
    }
}
