package org.molgenis.data.index;

import com.google.common.collect.ImmutableSet;
import org.mockito.Mock;
import org.molgenis.data.index.config.IndexTestConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.index.Impact.createSingleEntityImpact;
import static org.molgenis.data.index.Impact.createWholeRepositoryImpact;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { IndexTestConfig.class })
public class IndexingStrategyTest
{
	@Mock
	private IndexDependencyModel dependencyModel;

	IndexingStrategy indexingStrategy;

	@BeforeClass
	private void beforeClass()
	{
		initMocks(this);
		indexingStrategy = new IndexingStrategy();
	}

	@BeforeMethod
	private void beforeMethod()
	{
		reset(dependencyModel);
	}

	@Test
	public void testDetermineNecessaryActionsEmptySet()
	{
		assertEquals(indexingStrategy.determineImpact(emptySet(), dependencyModel), emptySet());
	}

	@Test
	public void testDetermineImpact()
	{
		Impact indexAction = createSingleEntityImpact("A", "id");
		Set<Impact> registeredIndexActions = singleton(indexAction);
		when(dependencyModel.getEntityTypesDependentOn("A")).thenReturn(Stream.of("A", "B", "C"));
		Set<Impact> actual = indexingStrategy.determineImpact(registeredIndexActions, dependencyModel);
		assertEquals(actual, ImmutableSet.of(createWholeRepositoryImpact("A"), createWholeRepositoryImpact("B"),
				createWholeRepositoryImpact("C")));
	}

	@Test
	public void testDetermineImpact2()
	{
		Impact change = createSingleEntityImpact("A", null);
		Set<Impact> registeredIndexActions = ImmutableSet.of(change);

		when(dependencyModel.getEntityTypesDependentOn("A")).thenReturn(Stream.of("A", "B", "C"));

		Set<Impact> actual = indexingStrategy.determineImpact(registeredIndexActions, dependencyModel);
		assertEquals(actual, ImmutableSet.of(createWholeRepositoryImpact("A"), createWholeRepositoryImpact("B"),
				createWholeRepositoryImpact("C")));
	}

	@Test
	public void testDetermineImpactNoDependencies()
	{
		Impact change = createSingleEntityImpact("A", "6");
		Set<Impact> registeredIndexActions = ImmutableSet.of(change);

		when(dependencyModel.getEntityTypesDependentOn("A")).thenReturn(Stream.empty());

		Set<Impact> actual = indexingStrategy.determineImpact(registeredIndexActions, dependencyModel);
		assertEquals(actual, ImmutableSet.of(createSingleEntityImpact("A", "6")));
	}
}
