package com.lazan.maven.transform.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.lazan.maven.transform.DependencyAggregator;
import com.lazan.maven.transform.GroupArtifactVersion;
import com.lazan.maven.transform.ProjectsContext;

@RunWith(MockitoJUnitRunner.class)
public class DependencyAggregatorImplTest {
	@Mock
	private ProjectsContext projectsContext;
	
	@Test
	public void test() {
		Model model1 = new Model();
		List<Dependency> dependencies1 = new ArrayList<>();
		dependencies1.add(createDependency("group", "artifact1", "1"));
		dependencies1.add(createDependency("group", "artifact2", "2"));
		dependencies1.add(createDependency("group", "artifact3", "3"));
		model1.setDependencies(dependencies1);
		
		Model model2 = new Model();
		List<Dependency> dependencies2 = new ArrayList<>();
		dependencies2.add(createDependency("group", "artifact1", "1"));
		dependencies2.add(createDependency("group", "artifact2", "2"));
		dependencies2.add(createDependency("group", "artifact3", "3"));
		model2.setDependencies(dependencies2);

		Model model3 = new Model();
		List<Dependency> dependencies3 = new ArrayList<>();
		dependencies3.add(createDependency("group", "artifact2", "2.2"));
		model3.setDependencies(dependencies3);
		
		when(projectsContext.getProjects()).thenReturn(Arrays.asList(model1, model2, model3));
		DependencyAggregator aggregator = new DependencyAggregatorImpl(projectsContext);

		assertTrue(aggregator.isCommonDependencyVersion("group", "artifact1"));
		assertFalse(aggregator.isCommonDependencyVersion("group", "artifact2"));
		assertTrue(aggregator.isCommonDependencyVersion("group", "artifact3"));
		
		assertEquals("1", aggregator.getCommonDependencyVersion("group", "artifact1"));
		assertEquals("3", aggregator.getCommonDependencyVersion("group", "artifact3"));
		
		Set<String> gavs = aggregator.getCommonDependencyVersions()
				.stream()
				.map((GroupArtifactVersion gav) -> String.format("%s:%s:%s", gav.getGroupId(), gav.getArtifactId(), gav.getVersion()))
				.collect(Collectors.toSet());
		
		Set<String> expectedGavs = new LinkedHashSet<>(Arrays.asList("group:artifact1:1", "group:artifact3:3"));
		assertEquals(expectedGavs, gavs);
	}

	private Dependency createDependency(String group, String artifact, String version) {
		Dependency dep = new Dependency();
		dep.setGroupId(group);
		dep.setArtifactId(artifact);
		dep.setVersion(version);
		return dep;
	}
}
