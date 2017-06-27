package org.molgenis.data.index;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.index.config.IndexTestConfig;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { IndexTestConfig.class })
public class IndexingStrategyTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private IndexActionFactory indexActionFactory;

	@Mock
	private IndexDependencyModel dependencyModel;

	IndexingStrategy indexingStrategy;

	@BeforeClass
	private void beforeClass()
	{
		indexingStrategy = new IndexingStrategy(indexActionFactory);
	}

	@BeforeMethod
	private void beforeMethod()
	{
		reset(dependencyModel);
	}

	@Test
	public void testDetermineNecessaryActionsEmptySet()
	{
		assertEquals(indexingStrategy.determineNecessaryActions(emptyList(), dependencyModel), emptySet());
	}

	@Test
	public void testDetermineNecessaryActions()
	{
		IndexAction indexAction = createSingleEntityIndexAction("A", "id");
		Collection<IndexAction> registeredIndexActions = ImmutableList.of(indexAction);

		when(dependencyModel.getEntityTypesDependentOn("A")).thenReturn(Stream.of("A", "B", "C"));

		Set<IndexAction> actual = indexingStrategy.determineNecessaryActions(registeredIndexActions, dependencyModel);
		assertEquals(actual, ImmutableSet.of(createCompleteEntityIndexAction("A"), createCompleteEntityIndexAction("B"),
				createCompleteEntityIndexAction("C")));
	}

	@Test
	public void testDetermineNecessaryActions2()
	{
		IndexAction indexAction = createCompleteEntityIndexAction("A");
		Collection<IndexAction> registeredIndexActions = ImmutableList.of(indexAction);

		when(dependencyModel.getEntityTypesDependentOn("A")).thenReturn(Stream.of("A", "B", "C"));

		Set<IndexAction> actual = indexingStrategy.determineNecessaryActions(registeredIndexActions, dependencyModel);
		assertEquals(actual, ImmutableSet.of(createCompleteEntityIndexAction("A"), createCompleteEntityIndexAction("B"),
				createCompleteEntityIndexAction("C")));
	}

	@Test
	public void testDetermineNecessaryActionsNoDependencies()
	{
		IndexAction indexAction = createCompleteEntityIndexAction("A");
		Collection<IndexAction> registeredIndexActions = ImmutableList.of(indexAction);

		when(dependencyModel.getEntityTypesDependentOn("A")).thenReturn(Stream.empty());

		Set<IndexAction> actual = indexingStrategy.determineNecessaryActions(registeredIndexActions, dependencyModel);
		assertEquals(actual, ImmutableSet.of(createCompleteEntityIndexAction("A")));
	}

	private IndexAction createCompleteEntityIndexAction(String entityTypeId)
	{
		IndexAction indexAction = indexActionFactory.create();
		indexAction.setEntityTypeId(entityTypeId);
		indexAction.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);
		return indexAction;
	}

	private IndexAction createSingleEntityIndexAction(String entityTypeId, String entityId)
	{
		IndexAction indexAction = createCompleteEntityIndexAction(entityTypeId);
		indexAction.setEntityId(entityId);
		return indexAction;
	}
}
