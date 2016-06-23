package org.molgenis.data.elasticsearch.reindex.job;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.reindex.meta.ReindexAction;
import org.molgenis.data.reindex.meta.ReindexActionGroup;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.test.data.DynamicEntityTestHarness;
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
import static org.molgenis.data.reindex.meta.ReindexActionGroupMetaData.REINDEX_ACTION_GROUP;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_ACTION;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.FAILED;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.FINISHED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

@ContextConfiguration(classes = { ReindexJobTest.Config.class })
public class ReindexJobTest extends AbstractMolgenisSpringTest
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
	private ReindexActionRegisterService reindexActionRegisterService;
	@Autowired
	private DataService dataService;
	@Autowired
	private DynamicEntityTestHarness harness;

	private final String transactionId = "aabbcc";

	private ReindexJob reindexJob;
	private ReindexActionGroup reindexActionGroup;
	private EntityMetaData testEntityMetaData;
	private Entity toReindexEntity;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);
		config.resetMocks();
		reindexJob = new ReindexJob(progress, authentication, transactionId, dataService, searchService);
		reindexActionGroup = reindexActionRegisterService.createReindexActionGroup(transactionId, 0);
		when(dataService.findOneById(REINDEX_ACTION_GROUP, transactionId, ReindexActionGroup.class))
				.thenReturn(reindexActionGroup);
		when(dataService.getMeta()).thenReturn(mds);
		testEntityMetaData = harness.createRefEntityMetaData();
		when(mds.getEntityMetaData("test")).thenReturn(testEntityMetaData);
		toReindexEntity = harness.createTestRefEntities(testEntityMetaData, 1).get(0);
		when(dataService.findOneById("test", "entityId")).thenReturn(toReindexEntity);
	}

	@Test
	public void testNoReindexActionJobForTransaction()
	{
		when(dataService.findOneById(REINDEX_ACTION_GROUP, this.transactionId)).thenReturn(null);
		mockGetAllReindexActions(empty());

		reindexJob.call(this.progress);

		verify(progress).status("No reindex actions found for transaction id: [aabbcc]");
		verify(searchService, never()).refreshIndex();
	}

	@Test
	public void testNoReindexActionsForTransaction()
	{
		mockGetAllReindexActions(empty());

		reindexJob.call(this.progress);

		verify(progress).status("No reindex actions found for transaction id: [aabbcc]");
		verify(searchService, never()).refreshIndex();
	}

	private void mockGetAllReindexActions(Stream<ReindexAction> entities)
	{
		Query<ReindexAction> q = ReindexJob.createQueryGetAllReindexActions(transactionId);
		when(dataService.findAll(REINDEX_ACTION, q, ReindexAction.class)).thenReturn(entities);
	}

	@Test
	public void testCreateQueryGetAllReindexActions()
	{
		Query<ReindexAction> q = ReindexJob.createQueryGetAllReindexActions("testme");
		assertEquals(q.toString(),
				"rules=['sys_idx_ReindexActionGroup' = 'testme'], sort=Sort [orders=[Order [attr=actionOrder, direction=ASC]]]");
	}

	@Test
	public void rebuildIndexDeleteSingleEntityTest()
	{
		ReindexAction reindexAction = reindexActionRegisterService
				.createReindexAction(reindexActionGroup, "test", CudType.DELETE, DataType.DATA, "entityId", 0);
		mockGetAllReindexActions(of(reindexAction));
		reindexActionGroup.setCount(1);

		reindexJob.call(progress);
		assertEquals(reindexAction.getReindexStatus(), FINISHED);

		verify(searchService).deleteById("entityId", testEntityMetaData);

		// verify progress messages
		verify(progress).status("######## START Reindex transaction id: [aabbcc] ########");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Reindexing test.entityId, CUDType = " + CudType.DELETE);
		verify(progress).progress(1, "Executed all reindex actions, cleaning up the actions...");
		verify(progress).status("refreshIndex...");
		verify(progress).status("refreshIndex done.");
		verify(progress).status("######## END Reindex transaction id: [aabbcc] ########");

		verify(searchService).refreshIndex();
		verify(dataService, times(2)).update(REINDEX_ACTION, reindexAction);
	}

	@Test
	public void rebuildIndexCreateSingleEntityTest()
	{
		this.rebuildIndexSingleEntityTest(CudType.CREATE, IndexingMode.ADD);
	}

	@Test
	public void rebuildIndexUpdateSingleEntityTest()
	{
		this.rebuildIndexSingleEntityTest(CudType.UPDATE, IndexingMode.UPDATE);
	}

	private void rebuildIndexSingleEntityTest(CudType cudType, IndexingMode indexingMode)
	{
		ReindexAction reindexAction = reindexActionRegisterService
				.createReindexAction(reindexActionGroup, "test", cudType, DataType.DATA, "entityId", 0);
		mockGetAllReindexActions(of(reindexAction));
		reindexActionGroup.setCount(1);

		reindexJob.call(this.progress);
		assertEquals(reindexAction.getReindexStatus(), FINISHED);

		verify(this.searchService).index(toReindexEntity, testEntityMetaData, indexingMode);

		verify(progress).status("######## START Reindex transaction id: [aabbcc] ########");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Reindexing test.entityId, CUDType = " + cudType.name());
		verify(progress).progress(1, "Executed all reindex actions, cleaning up the actions...");
		verify(progress).status("refreshIndex...");
		verify(progress).status("refreshIndex done.");
		verify(progress).status("######## END Reindex transaction id: [aabbcc] ########");

		verify(dataService, times(2)).update(REINDEX_ACTION, reindexAction);
	}

	@Test
	public void rebuildIndexCreateBatchEntitiesTest()
	{
		this.rebuildIndexBatchEntitiesTest(CudType.CREATE);
	}

	@Test
	public void rebuildIndexDeleteBatchEntitiesTest()
	{
		this.rebuildIndexBatchEntitiesTest(CudType.DELETE);
	}

	@Test
	public void rebuildIndexUpdateBatchEntitiesTest()
	{
		this.rebuildIndexBatchEntitiesTest(CudType.UPDATE);
	}

	private void rebuildIndexBatchEntitiesTest(CudType cudType)
	{
		ReindexAction reindexAction = reindexActionRegisterService
				.createReindexAction(reindexActionGroup, "test", cudType, DataType.DATA, null, 0);
		mockGetAllReindexActions(of(reindexAction));
		reindexActionGroup.setCount(1);

		reindexJob.call(this.progress);
		assertEquals(reindexAction.getReindexStatus(), FINISHED);

		verify(this.searchService).rebuildIndex(this.dataService.getRepository("any"));

		verify(progress).status("######## START Reindex transaction id: [aabbcc] ########");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Reindexing repository test. CUDType = " + cudType.name());
		verify(progress).progress(1, "Executed all reindex actions, cleaning up the actions...");
		verify(progress).status("refreshIndex...");
		verify(progress).status("refreshIndex done.");
		verify(progress).status("######## END Reindex transaction id: [aabbcc] ########");

		verify(dataService, times(2)).update(REINDEX_ACTION, reindexAction);
	}

	@Test
	public void rebuildIndexCreateMetaDataTest()
	{
		this.rebuildIndexMetaDataTest(CudType.CREATE);
	}

	@Test
	public void rebuildIndexUpdateMetaDataTest()
	{
		this.rebuildIndexMetaDataTest(CudType.UPDATE);
	}

	private void rebuildIndexMetaDataTest(CudType cudType)
	{
		ReindexAction reindexAction = reindexActionRegisterService
				.createReindexAction(reindexActionGroup, "test", cudType, DataType.METADATA, null, 0);
		mockGetAllReindexActions(of(reindexAction));
		reindexActionGroup.setCount(1);

		reindexJob.call(this.progress);
		assertEquals(reindexAction.getReindexStatus(), FINISHED);

		verify(this.searchService).rebuildIndex(this.dataService.getRepository("any"));

		verify(progress).status("######## START Reindex transaction id: [aabbcc] ########");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Reindexing repository test. CUDType = " + cudType.name());
		verify(progress).progress(1, "Executed all reindex actions, cleaning up the actions...");
		verify(progress).status("refreshIndex...");
		verify(progress).status("refreshIndex done.");
		verify(progress).status("######## END Reindex transaction id: [aabbcc] ########");

		verify(dataService, times(2)).update(REINDEX_ACTION, reindexAction);

		// make sure both the actions and the action job got deleted
		verify(dataService).delete(eq(REINDEX_ACTION), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toList()), newArrayList(reindexAction));
		verify(dataService).deleteById(REINDEX_ACTION_GROUP, transactionId);

		verify(dataService).deleteById(REINDEX_ACTION_GROUP, transactionId);
	}

	@Test
	public void rebuildIndexDeleteMetaDataEntityTest()
	{
		ReindexAction reindexAction = reindexActionRegisterService
				.createReindexAction(reindexActionGroup, "test", CudType.DELETE, DataType.METADATA, null, 0);
		mockGetAllReindexActions(of(reindexAction));
		reindexActionGroup.setCount(1);

		reindexJob.call(this.progress);
		assertEquals(reindexAction.getReindexStatus(), FINISHED);

		verify(this.searchService).delete("test");

		verify(progress).status("######## START Reindex transaction id: [aabbcc] ########");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Dropping index of repository test.");
		verify(progress).progress(1, "Executed all reindex actions, cleaning up the actions...");
		verify(progress).status("refreshIndex...");
		verify(progress).status("refreshIndex done.");
		verify(progress).status("######## END Reindex transaction id: [aabbcc] ########");

		verify(dataService, times(2)).update(REINDEX_ACTION, reindexAction);
	}

	@Test
	public void reindexSingleEntitySearchServiceThrowsExceptionOnSecondEntityId()
	{
		ReindexAction reindexAction1 = reindexActionRegisterService
				.createReindexAction(reindexActionGroup, "test", CudType.DELETE, DataType.DATA, "entityId1", 0);

		ReindexAction reindexAction2 = reindexActionRegisterService
				.createReindexAction(reindexActionGroup, "test", CudType.DELETE, DataType.DATA, "entityId2", 1);

		ReindexAction reindexAction3 = reindexActionRegisterService
				.createReindexAction(reindexActionGroup, "test", CudType.DELETE, DataType.DATA, "entityId3", 2);

		mockGetAllReindexActions(of(reindexAction1, reindexAction2, reindexAction3));
		reindexActionGroup.setCount(3);

		MolgenisDataException mde = new MolgenisDataException("Random unrecoverable exception");
		doThrow(mde).when(searchService).deleteById("entityId2", testEntityMetaData);

		try
		{
			reindexJob.call(progress);
		}
		catch (Exception expected)
		{
			assertSame(expected, mde);
		}

		verify(searchService).deleteById("entityId1", testEntityMetaData);
		verify(searchService).deleteById("entityId2", testEntityMetaData);
		verify(searchService).deleteById("entityId3", testEntityMetaData);

		verify(searchService).refreshIndex();

		// Make sure the action status got updated and that the actionJob didn't get deleted
		assertEquals(reindexAction1.getReindexStatus(), FINISHED);
		assertEquals(reindexAction2.getReindexStatus(), FAILED);
		verify(dataService, atLeast(1)).update(REINDEX_ACTION, reindexAction1);
		verify(dataService, atLeast(1)).update(REINDEX_ACTION, reindexAction2);
		verify(dataService, never()).delete(REINDEX_ACTION_GROUP, reindexActionGroup);
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.reindex", "org.molgenis.test.data" })
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