package org.molgenis.data.index;

import com.google.common.collect.ImmutableSet;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityKey;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionGroup;
import org.molgenis.data.index.meta.IndexActionGroupFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.*;
import static org.molgenis.data.index.Impact.createSingleEntityImpact;
import static org.molgenis.data.index.Impact.createWholeRepositoryImpact;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetaData.INDEX_ACTION;
import static org.molgenis.data.transaction.TransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { IndexActionRegisterServiceImplTest.Config.class })
public class IndexActionRegisterServiceImplTest extends AbstractMolgenisSpringTest
{
	private IndexActionRegisterServiceImpl indexActionRegisterService;
	@Mock
	private IndexDependencyModel indexDependencyModel;
	@Mock
	private IndexDependencyModelFactory indexDependencyModelFactory;
	@Mock
	private IndexingStrategy indexingStrategy;
	@Autowired
	private EntityTypeFactory entityTypeFactory;
	@Autowired
	private IndexActionFactory indexActionFactory;
	@Autowired
	private IndexActionGroupFactory indexActionGroupFactory;
	@Autowired
	private DataService dataService;
	private EntityType entityType;

	@Captor
	private ArgumentCaptor<IndexActionGroup> indexActionGroupArgumentCaptor;
	@Captor
	private ArgumentCaptor<Stream<IndexAction>> indexActionsCaptor;

	@BeforeClass
	public void beforeClass() throws Exception
	{
		entityType = entityTypeFactory.create("EntityType");
	}

	@BeforeMethod
	public void beforeMethod() throws Exception
	{
		indexActionRegisterService = new IndexActionRegisterServiceImpl(dataService, indexActionFactory,
				indexActionGroupFactory, indexingStrategy, indexDependencyModelFactory);
		when(indexDependencyModelFactory.getDependencyModel()).thenReturn(indexDependencyModel);
		TransactionSynchronizationManager.bindResource(TRANSACTION_ID_RESOURCE_NAME, "ABCDE");
	}

	@AfterMethod
	public void afterMethod()
	{
		TransactionSynchronizationManager.unbindResource(TRANSACTION_ID_RESOURCE_NAME);
	}

	@Test
	public void testStoreIndexActionsNoWork() throws Exception
	{
		assertFalse(indexActionRegisterService.storeIndexActions("ABCDE"));
	}

	@Test
	public void testStoreIndexActionsWork()
	{
		when(indexingStrategy.determineImpact(ImmutableSet.of(createSingleEntityImpact("EntityType", "id1"),
				createSingleEntityImpact("EntityType", "id2")), indexDependencyModel)).thenReturn(
				ImmutableSet.of(createWholeRepositoryImpact("EntityType"), createWholeRepositoryImpact("Dependent")));

		indexActionRegisterService.register(entityType, "id1");
		indexActionRegisterService.register(entityType, "id2");
		assertTrue(indexActionRegisterService.storeIndexActions("ABCDE"));

		verify(dataService).add(eq(INDEX_ACTION_GROUP), indexActionGroupArgumentCaptor.capture());
		verify(dataService).add(eq(INDEX_ACTION), indexActionsCaptor.capture());
		verifyNoMoreInteractions(dataService);

		IndexActionGroup indexActionGroup = indexActionGroupArgumentCaptor.getValue();
		assertEquals(indexActionGroup.getId(), "ABCDE");
		assertEquals(indexActionGroup.getCount(), 2);
		List<IndexAction> actions = indexActionsCaptor.getValue().collect(Collectors.toList());

		assertTrue(actions.stream()
						  .allMatch(action -> action.getIndexActionGroup().getId().equals("ABCDE")
								  && action.getEntityId() == null));
		assertEquals(actions.stream().map(IndexAction::getEntityTypeId).collect(toSet()),
				ImmutableSet.of("EntityType", "Dependent"));
	}

	@Test
	public void testForgetIndexActions() throws Exception
	{
		indexActionRegisterService.forgetIndexActions("ABCDE");
		assertFalse(indexActionRegisterService.storeIndexActions("ABCDE"));
		verifyZeroInteractions(dataService);
	}

	@Test
	public void testIsDirty()
	{
		indexActionRegisterService.register(entityType, "id1");

		assertTrue(indexActionRegisterService.isEntityDirty(EntityKey.create(entityType, "id1")),
				"Registered entity key should be dirty");
		assertFalse(indexActionRegisterService.isEntityDirty(EntityKey.create(entityType, "id2")),
				"Non-registered entity key should not be dirty");
		assertFalse(indexActionRegisterService.isRepositoryCompletelyClean(entityType),
				"If one entity of a repository is registered, the entire repository is no longer completely clean");
	}

	@Configuration
	@ComponentScan(basePackages = { "org.molgenis.data.index.meta" })
	public static class Config
	{

	}

}