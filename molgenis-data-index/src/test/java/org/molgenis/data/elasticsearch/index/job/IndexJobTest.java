package org.molgenis.data.elasticsearch.index.job;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.*;
import org.molgenis.data.index.IndexingMode;
import org.molgenis.data.index.SearchService;
import org.molgenis.data.index.config.IndexTestConfig;
import org.molgenis.data.index.meta.*;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetaData.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetaData.IndexStatus.FAILED;
import static org.molgenis.data.index.meta.IndexActionMetaData.IndexStatus.FINISHED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

@ContextConfiguration(classes = { IndexJobTest.Config.class })
public class IndexJobTest extends AbstractMolgenisSpringTest
{
	@Captor
	private ArgumentCaptor<Stream<Entity>> streamCaptor;

	@Autowired
	private Progress progress;
	@Autowired
	private Authentication authentication;
	@Autowired
	private SearchService searchService;
	@Autowired
	private MetaDataService mds;
	@Autowired
	private Config config;
	@Autowired
	private DataService dataService;
	@Autowired
	private EntityTestHarness harness;
	@Autowired
	private IndexActionFactory indexActionFactory;
	@Autowired
	private IndexActionGroupFactory indexActionGroupFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;
	private final String transactionId = "aabbcc";
	private IndexJob indexJob;
	private IndexActionGroup indexActionGroup;
	private EntityType testEntityType;
	private Entity toIndexEntity;

	@BeforeMethod
	public void beforeMethod()
	{
		config.resetMocks();
		indexJob = new IndexJob(progress, authentication, transactionId, dataService, searchService, entityTypeFactory);
		indexActionGroup = indexActionGroupFactory.create(transactionId).setCount(0);
		when(dataService.findOneById(INDEX_ACTION_GROUP, transactionId, IndexActionGroup.class))
				.thenReturn(indexActionGroup);
		when(dataService.getMeta()).thenReturn(mds);
		testEntityType = harness.createDynamicRefEntityType();
		when(mds.getEntityType("TypeTestRefDynamic")).thenReturn(testEntityType);
		toIndexEntity = harness.createTestRefEntities(testEntityType, 1).get(0);
		when(dataService.getEntityType("TypeTestRefDynamic")).thenReturn(testEntityType);
		when(dataService.findOneById("TypeTestRefDynamic", "entityId")).thenReturn(toIndexEntity);
		when(dataService.getEntityType("entityType")).thenReturn(testEntityType);
	}

	@Test
	public void testNoIndexActionJobForTransaction()
	{
		when(dataService.findOneById(INDEX_ACTION_GROUP, this.transactionId)).thenReturn(null);
		mockGetAllIndexActions(empty());

		indexJob.call(this.progress);

		verify(progress).status("No index actions found for transaction id: [aabbcc]");
		verify(searchService, never()).refreshIndex();
	}

	@Test
	public void testNoIndexActionsForTransaction()
	{
		mockGetAllIndexActions(empty());

		indexJob.call(this.progress);

		verify(progress).status("No index actions found for transaction id: [aabbcc]");
		verify(searchService, never()).refreshIndex();
	}

	private void mockGetAllIndexActions(Stream<IndexAction> entities)
	{
		Query<IndexAction> q = IndexJob.createQueryGetAllIndexActions(transactionId);
		when(dataService.findAll(INDEX_ACTION, q, IndexAction.class)).thenReturn(entities);
	}

	@Test
	public void testCreateQueryGetAllIndexActions()
	{
		Query<IndexAction> q = IndexJob.createQueryGetAllIndexActions("testme");
		assertEquals(q.toString(),
				"rules=['indexActionGroup' = 'testme'], sort=Sort [orders=[Order [attr=actionOrder, direction=ASC]]]");
	}

	@Test
	public void rebuildIndexDeleteSingleEntityTest()
	{
		when(dataService.findOneById("TypeTestRefDynamic", "entityId")).thenReturn(null);

		IndexAction indexAction = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityTypeId("entityType").setEntityId("entityId").setActionOrder(0)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);
		mockGetAllIndexActions(of(indexAction));
		indexActionGroup.setCount(1);

		when(dataService.hasRepository("TypeTestRefDynamic")).thenReturn(true);

		indexJob.call(progress);
		assertEquals(indexAction.getIndexStatus(), FINISHED);

		verify(searchService).deleteById("entityId", testEntityType);

		// verify progress messages
		verify(progress).status("Start indexing for transaction id: [aabbcc]");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Indexing TypeTestRefDynamic.entityId");
		verify(progress).progress(1, "Executed all index actions, cleaning up the actions...");
		verify(progress).status("Refresh index start");
		verify(progress).status("Refresh index done");
		verify(progress).status("Finished indexing for transaction id: [aabbcc]");
		verify(searchService).refreshIndex();
		verify(dataService, times(2)).update(INDEX_ACTION, indexAction);
	}

	@Test
	public void rebuildIndexCreateSingleEntityTest()
	{
		this.rebuildIndexSingleEntityTest(IndexingMode.ADD);
	}

	@Test
	public void rebuildIndexUpdateSingleEntityTest()
	{
		Entity actualEntity = dataService.findOneById("TypeTestRefDynamic", "entityId");
		EntityType emd = actualEntity.getEntityType();
		Query<Entity> q = new QueryImpl<>();
		q.eq(emd.getIdAttribute().getName(), "entityId");

		when(searchService.findOne(q, emd)).thenReturn(actualEntity);
		this.rebuildIndexSingleEntityTest(IndexingMode.UPDATE);
	}

	private void rebuildIndexSingleEntityTest(IndexingMode indexingMode)
	{
		IndexAction indexAction = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityTypeId("entityType").setEntityId("entityId").setActionOrder(0)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);
		mockGetAllIndexActions(of(indexAction));
		when(dataService.hasRepository("TypeTestRefDynamic")).thenReturn(true);
		indexActionGroup.setCount(1);

		indexJob.call(this.progress);
		assertEquals(indexAction.getIndexStatus(), FINISHED);

		verify(this.searchService).index(toIndexEntity, testEntityType, indexingMode);

		verify(progress).status("Start indexing for transaction id: [aabbcc]");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Indexing TypeTestRefDynamic.entityId");
		verify(progress).progress(1, "Executed all index actions, cleaning up the actions...");
		verify(progress).status("Refresh index start");
		verify(progress).status("Refresh index done");
		verify(progress).status("Finished indexing for transaction id: [aabbcc]");

		verify(dataService, times(2)).update(INDEX_ACTION, indexAction);
	}

	@Test
	private void rebuildIndexMetaUpdateDataTest()
	{
		when(dataService.hasRepository("TypeTestRefDynamic")).thenReturn(true);
		EntityType entityType = dataService.getEntityType("TypeTestRefDynamic");
		when(searchService.hasIndex(entityType)).thenReturn(true);

		IndexAction indexAction = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityTypeId("entityType").setEntityId(null).setActionOrder(0)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);
		mockGetAllIndexActions(of(indexAction));
		indexActionGroup.setCount(1);

		indexJob.call(this.progress);
		assertEquals(indexAction.getIndexStatus(), FINISHED);
		verify(this.searchService).rebuildIndex(this.dataService.getRepository("any"));
		verify(progress).status("Start indexing for transaction id: [aabbcc]");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Indexing TypeTestRefDynamic");
		verify(progress).progress(1, "Executed all index actions, cleaning up the actions...");
		verify(progress).status("Refresh index start");
		verify(progress).status("Refresh index done");
		verify(progress).status("Finished indexing for transaction id: [aabbcc]");

		verify(dataService, times(2)).update(INDEX_ACTION, indexAction);

		// make sure both the actions and the action job got deleted
		verify(dataService).delete(eq(INDEX_ACTION), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toList()), newArrayList(indexAction));
		verify(dataService).deleteById(INDEX_ACTION_GROUP, transactionId);
	}

	@Test
	private void rebuildIndexMetaCreateDataTest()
	{
		when(dataService.hasRepository("TypeTestRefDynamic")).thenReturn(true);

		IndexAction indexAction = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityTypeId("entityType").setEntityId(null).setActionOrder(0)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);
		mockGetAllIndexActions(of(indexAction));
		indexActionGroup.setCount(1);

		indexJob.call(this.progress);
		assertEquals(indexAction.getIndexStatus(), FINISHED);
		verify(this.searchService).rebuildIndex(this.dataService.getRepository("any"));
		verify(progress).status("Start indexing for transaction id: [aabbcc]");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Indexing TypeTestRefDynamic");
		verify(progress).progress(1, "Executed all index actions, cleaning up the actions...");
		verify(progress).status("Refresh index start");
		verify(progress).status("Refresh index done");
		verify(progress).status("Finished indexing for transaction id: [aabbcc]");

		verify(dataService, times(2)).update(INDEX_ACTION, indexAction);

		// make sure both the actions and the action job got deleted
		verify(dataService).delete(eq(INDEX_ACTION), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toList()), newArrayList(indexAction));
		verify(dataService).deleteById(INDEX_ACTION_GROUP, transactionId);

		verify(dataService).deleteById(INDEX_ACTION_GROUP, transactionId);
	}

	@Test
	public void rebuildIndexDeleteMetaDataEntityTest()
	{
		String entityTypeId = "entityTypeId";
		String entityTypeLabel = "entityTypeLabel";
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		when(entityType.getLabel()).thenReturn(entityTypeLabel);
		IndexAction indexAction = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityTypeId(entityTypeId)
				.setEntityId(null).setActionOrder(0).setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);
		mockGetAllIndexActions(of(indexAction));
		indexActionGroup.setCount(1);

		when(dataService.hasRepository("TypeTestRefDynamic")).thenReturn(false);
		when(dataService.getEntityType("entityTypeName")).thenReturn(null);

		when(searchService.hasIndex(any(EntityType.class))).thenReturn(true);

		indexJob.call(this.progress);
		assertEquals(indexAction.getIndexStatus(), FINISHED);

		ArgumentCaptor<EntityType> entityTypeCaptor = ArgumentCaptor.forClass(EntityType.class);
		verify(this.searchService).deleteIndex(entityTypeCaptor.capture());
		EntityType actualEntityType = entityTypeCaptor.getValue();
		assertEquals(actualEntityType.getId(), entityTypeId);

		verify(progress).status("Start indexing for transaction id: [aabbcc]");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Dropping entityType with id: entityTypeId");
		verify(progress).progress(1, "Executed all index actions, cleaning up the actions...");
		verify(progress).status("Refresh index start");
		verify(progress).status("Refresh index done");
		verify(progress).status("Finished indexing for transaction id: [aabbcc]");

		verify(dataService, times(2)).update(INDEX_ACTION, indexAction);
	}

	@Test
	public void indexSingleEntitySearchServiceThrowsExceptionOnSecondEntityId()
	{
		IndexAction indexAction1 = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityTypeId("entityType").setEntityId("entityId1").setActionOrder(0)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);

		IndexAction indexAction2 = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityTypeId("entityType").setEntityId("entityId2").setActionOrder(1)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);

		IndexAction indexAction3 = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityTypeId("entityType").setEntityId("entityId3").setActionOrder(2)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);

		mockGetAllIndexActions(of(indexAction1, indexAction2, indexAction3));
		indexActionGroup.setCount(3);

		MolgenisDataException mde = new MolgenisDataException("Random unrecoverable exception");
		doThrow(mde).when(searchService).deleteById("entityId2", testEntityType);

		when(dataService.hasRepository("TypeTestRefDynamic")).thenReturn(true);

		try
		{
			indexJob.call(progress);
		}
		catch (Exception expected)
		{
			assertSame(expected, mde);
		}

		verify(searchService).deleteById("entityId1", testEntityType);
		verify(searchService).deleteById("entityId2", testEntityType);
		verify(searchService).deleteById("entityId3", testEntityType);

		verify(searchService).refreshIndex();

		// Make sure the action status got updated and that the actionJob didn't get deleted
		assertEquals(indexAction1.getIndexStatus(), FINISHED);
		assertEquals(indexAction2.getIndexStatus(), FAILED);
		verify(dataService, atLeast(1)).update(INDEX_ACTION, indexAction1);
		verify(dataService, atLeast(1)).update(INDEX_ACTION, indexAction2);
		verify(dataService, never()).delete(INDEX_ACTION_GROUP, indexActionGroup);
	}

	@Configuration
	@Import({ IndexTestConfig.class, TestHarnessConfig.class })
	public static class Config
	{
		@Autowired
		private EntityTypeFactory entityTypeFactory;

		@Mock
		private Progress progress;
		@Mock
		private Authentication authentication;
		@Mock
		private SearchService searchService;
		@Mock
		private MetaDataService mds;

		public Config()
		{
			initMocks(this);
		}

		@Bean
		public Progress progress()
		{
			return progress;
		}

		@Bean
		public Authentication authentication()
		{
			return authentication;
		}

		@Bean
		public SearchService searchService()
		{
			return searchService;
		}

		@Bean
		public MetaDataService metaDataService()
		{
			return mds;
		}

		void resetMocks()
		{
			reset(progress, authentication, searchService, mds);
		}
	}
}