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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.lazan.maven.transform.AggregatedDependency;
import com.lazan.maven.transform.DependencyAggregator;
import com.lazan.maven.transform.ProjectsContext;

@RunWith(MockitoJUnitRunner.class)
public class DependencyAggregatorImplTest {
	@Mock
	private ProjectsContext projectsContext;
	
	@Test
	public void testAggregator() {
		Model model1 = createModel("group:artifact1:1", "group:artifact2:2", "group:artifact3:3");
		Model model2 = createModel("group:artifact1:1", "group:artifact2:2", "group:artifact3:3");
		Model model3 = createModel("group:artifact2:2.2");

		when(projectsContext.getProjects()).thenReturn(Arrays.asList(model1, model2, model3));
		DependencyAggregator aggregator = new DependencyAggregatorImpl(projectsContext);

		assertTrue(aggregator.isAggregatedDependency("group", "artifact1"));
		assertFalse(aggregator.isAggregatedDependency("group", "artifact2"));
		assertTrue(aggregator.isAggregatedDependency("group", "artifact3"));
		
		assertEquals("1", aggregator.getAggregatedDependency("group", "artifact1").getVersion());
		assertEquals("3", aggregator.getAggregatedDependency("group", "artifact3").getVersion());
		
		Set<String> gavs = aggregator.getAggregatedDependencies()
				.stream()
				.map((AggregatedDependency gav) -> String.format("%s:%s:%s", gav.getGroupId(), gav.getArtifactId(), gav.getVersion()))
				.collect(Collectors.toSet());
		
		Set<String> expectedGavs = new LinkedHashSet<>(Arrays.asList("group:artifact1:1", "group:artifact3:3"));
		assertEquals(expectedGavs, gavs);
	}
	
	@Test
	public void testVariableName() {
		Model model = createModel("group:foo-bar:1", "group:2number-prefix:2");
		when(projectsContext.getProjects()).thenReturn(Arrays.asList(model));
		DependencyAggregator aggregator = new DependencyAggregatorImpl(projectsContext);
		
		assertEquals("fooBar", aggregator.getAggregatedDependency("group", "foo-bar").getVariableName());
		assertEquals("numberPrefix", aggregator.getAggregatedDependency("group", "2number-prefix").getVariableName());
	}	

	protected Model createModel(String... gavs) {
		Pattern pattern = Pattern.compile("(.*):(.*):(.*)");
		Model model = new Model();
		List<Dependency> deps = new ArrayList<>();
		for (String gav : gavs) {
			Matcher matcher = pattern.matcher(gav);
			assertTrue(gav, matcher.matches());
			Dependency dep = new Dependency();
			dep.setGroupId(matcher.group(1));
			dep.setArtifactId(matcher.group(2));
			dep.setVersion(matcher.group(3));
			deps.add(dep);
		}
		model.setDependencies(deps);
		return model;
	}
}
