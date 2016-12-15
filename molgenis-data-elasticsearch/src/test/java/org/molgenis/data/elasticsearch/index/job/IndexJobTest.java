package org.molgenis.data.elasticsearch.index.job;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.index.meta.*;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.test.data.EntityTestHarness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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

	private final String transactionId = "aabbcc";

	private IndexJob indexJob;
	private IndexActionGroup indexActionGroup;
	private EntityType testEntityType;
	private Entity toIndexEntity;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);
		config.resetMocks();
		indexJob = new IndexJob(progress, authentication, transactionId, dataService, searchService);
		indexActionGroup = indexActionGroupFactory.create(transactionId).setCount(0);
		when(dataService.findOneById(INDEX_ACTION_GROUP, transactionId, IndexActionGroup.class))
				.thenReturn(indexActionGroup);
		when(dataService.getMeta()).thenReturn(mds);
		testEntityType = harness.createDynamicRefEntityType();
		when(mds.getEntityType("test")).thenReturn(testEntityType);
		toIndexEntity = harness.createTestRefEntities(testEntityType, 1).get(0);
		when(dataService.getEntityType("test")).thenReturn(testEntityType);
		when(dataService.findOneById("test", "entityId")).thenReturn(toIndexEntity);
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
		when(dataService.findOneById("test", "entityId")).thenReturn(null);

		IndexAction indexAction = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityFullName("test").setEntityId("entityId").setActionOrder(0)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);
		mockGetAllIndexActions(of(indexAction));
		indexActionGroup.setCount(1);

		when(dataService.hasRepository("test")).thenReturn(true);

		indexJob.call(progress);
		assertEquals(indexAction.getIndexStatus(), FINISHED);

		verify(searchService).deleteById("entityId", testEntityType);

		// verify progress messages
		verify(progress).status("Start indexing for transaction id: [aabbcc]");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Indexing test.entityId");
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
		Entity actualEntity = dataService.findOneById("test", "entityId");
		EntityType emd = actualEntity.getEntityType();
		Query q = new QueryImpl();
		q.eq(emd.getIdAttribute().getName(), "entityId");

		when(searchService.findOne(q, emd)).thenReturn(actualEntity);
		this.rebuildIndexSingleEntityTest(IndexingMode.UPDATE);
	}

	private void rebuildIndexSingleEntityTest(IndexingMode indexingMode)
	{
		IndexAction indexAction = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityFullName("test").setEntityId("entityId").setActionOrder(0)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);
		mockGetAllIndexActions(of(indexAction));
		when(dataService.hasRepository("test")).thenReturn(true);
		indexActionGroup.setCount(1);

		indexJob.call(this.progress);
		assertEquals(indexAction.getIndexStatus(), FINISHED);

		verify(this.searchService).index(toIndexEntity, testEntityType, indexingMode);

		verify(progress).status("Start indexing for transaction id: [aabbcc]");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Indexing test.entityId");
		verify(progress).progress(1, "Executed all index actions, cleaning up the actions...");
		verify(progress).status("Refresh index start");
		verify(progress).status("Refresh index done");
		verify(progress).status("Finished indexing for transaction id: [aabbcc]");

		verify(dataService, times(2)).update(INDEX_ACTION, indexAction);
	}

	@Test
	private void rebuildIndexMetaUpdateDataTest()
	{
		when(dataService.hasRepository("test")).thenReturn(true);
		EntityType entityType = dataService.getEntityType("test");
		when(searchService.hasMapping(entityType)).thenReturn(true);

		IndexAction indexAction = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityFullName("test").setEntityId(null).setActionOrder(0)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);
		mockGetAllIndexActions(of(indexAction));
		indexActionGroup.setCount(1);

		indexJob.call(this.progress);
		assertEquals(indexAction.getIndexStatus(), FINISHED);
		verify(this.searchService).rebuildIndex(this.dataService.getRepository("any"));
		verify(progress).status("Start indexing for transaction id: [aabbcc]");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Indexing test");
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
		when(dataService.hasRepository("test")).thenReturn(true);

		IndexAction indexAction = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityFullName("test").setEntityId(null).setActionOrder(0)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);
		mockGetAllIndexActions(of(indexAction));
		indexActionGroup.setCount(1);

		indexJob.call(this.progress);
		assertEquals(indexAction.getIndexStatus(), FINISHED);
		verify(this.searchService).rebuildIndex(this.dataService.getRepository("any"));
		verify(progress).status("Start indexing for transaction id: [aabbcc]");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Indexing test");
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
		IndexAction indexAction = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityFullName("test").setEntityId(null).setActionOrder(0)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);
		mockGetAllIndexActions(of(indexAction));
		indexActionGroup.setCount(1);

		when(dataService.hasRepository("test")).thenReturn(false);
		when(searchService.hasMapping("test")).thenReturn(true);

		indexJob.call(this.progress);
		assertEquals(indexAction.getIndexStatus(), FINISHED);

		verify(this.searchService).delete("test");

		verify(progress).status("Start indexing for transaction id: [aabbcc]");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Dropping test");
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
				.setEntityFullName("test").setEntityId("entityId1").setActionOrder(0)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);

		IndexAction indexAction2 = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityFullName("test").setEntityId("entityId2").setActionOrder(1)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);

		IndexAction indexAction3 = indexActionFactory.create().setIndexActionGroup(indexActionGroup)
				.setEntityFullName("test").setEntityId("entityId3").setActionOrder(2)
				.setIndexStatus(IndexActionMetaData.IndexStatus.PENDING);

		mockGetAllIndexActions(of(indexAction1, indexAction2, indexAction3));
		indexActionGroup.setCount(3);

		MolgenisDataException mde = new MolgenisDataException("Random unrecoverable exception");
		doThrow(mde).when(searchService).deleteById("entityId2", testEntityType);

		when(dataService.hasRepository("test")).thenReturn(true);

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
	@ComponentScan({ "org.molgenis.data.index", "org.molgenis.test.data" })
	public static class Config
	{
		@Mock
		private Progress progress;
		@Mock
		private Authentication authentication;
		@Mock
		private SearchService searchService;
		@Mock
		private MetaDataService mds;
		@Mock
		private DataService dataService;

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

		@Bean
		public DataService dataService()
		{
			return dataService;
		}

		void resetMocks()
		{
			reset(progress, authentication, searchService, mds, dataService);
		}

	}
}