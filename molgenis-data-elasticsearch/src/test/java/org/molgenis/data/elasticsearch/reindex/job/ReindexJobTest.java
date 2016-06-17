package org.molgenis.data.elasticsearch.reindex.job;

import com.google.common.collect.Lists;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.security.core.Authentication;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_STATUS;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.FAILED;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.FINISHED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

public class ReindexJobTest
{
	@Mock
	private Progress progress;
	@Mock
	private Authentication authentication;
	@Mock
	private DataService dataService;
	@Mock
	private SearchService searchService;
	@Mock
	private MetaDataService mds;
	@Captor
	private ArgumentCaptor<Stream<Entity>> streamCaptor;

	private final String transactionId = "aabbcc";

	@InjectMocks
	private ReindexActionRegisterService reindexActionRegisterService = new ReindexActionRegisterService();
	private ReindexJob reindexJob;
	private Entity reindexActionJob;
	private EntityMetaData testEntityMetaData;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);
		reindexJob = new ReindexJob(progress, authentication, transactionId, dataService, searchService);
		reindexActionJob = reindexActionRegisterService.createReindexActionJob(transactionId, 0);
		when(dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, transactionId)).thenReturn(reindexActionJob);
		when(dataService.getMeta()).thenReturn(mds);
		testEntityMetaData = new DefaultEntityMetaData("test");
		when(mds.getEntityMetaData("test")).thenReturn(testEntityMetaData);
	}

	@Test
	public void testNoReindexActionJobForTransaction()
	{
		when(dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, this.transactionId)).thenReturn(null);
		mockGetAllReindexActions(this.transactionId, Stream.empty());

		reindexJob.call(this.progress);

		verify(progress).status("No reindex actions found for transaction id: [aabbcc]");
		verify(searchService, never()).refreshIndex();
	}

	@Test
	public void testNoReindexActionsForTransaction()
	{
		mockGetAllReindexActions(this.transactionId, Lists.<Entity>newArrayList().stream());

		reindexJob.call(this.progress);

		verify(progress).status("No reindex actions found for transaction id: [aabbcc]");
		verify(searchService, never()).refreshIndex();
	}

	private void mockGetAllReindexActions(String transactionId, Stream<Entity> entities)
	{
		Query<Entity> q = ReindexJob.createQueryGetAllReindexActions(transactionId);
		when(dataService.findAll(ReindexActionMetaData.ENTITY_NAME, q)).thenReturn(entities);
	}

	@Test
	public void testCreateQueryGetAllReindexActions()
	{
		Query<Entity> q = ReindexJob.createQueryGetAllReindexActions("testme");
		assertEquals(q.toString(),
				"rules=['reindexActionGroup' = 'testme'], sort=Sort [orders=[Order [attr=actionOrder, direction=ASC]]]");
	}

	@Test
	public void rebuildIndexDeleteSingleEntityTest()
	{
		Entity reindexAction = reindexActionRegisterService
				.createReindexAction(transactionId, "test", CudType.DELETE, DataType.DATA, "entityId", 0);
		mockGetAllReindexActions(this.transactionId, Stream.of(reindexAction));
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, 1);

		Entity toReindexEntity = new DefaultEntity(testEntityMetaData, dataService);
		when(dataService.findOneById("test", "entityId")).thenReturn(toReindexEntity);

		reindexJob.call(progress);
		assertEquals(reindexAction.get(REINDEX_STATUS), FINISHED.name());

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
		verify(dataService, times(2)).update(ReindexActionMetaData.ENTITY_NAME, reindexAction);
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
		Entity reindexAction = reindexActionRegisterService
				.createReindexAction(transactionId, "test", cudType, DataType.DATA, "entityId", 0);
		mockGetAllReindexActions(this.transactionId, Stream.of(reindexAction));
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, 1);

		MetaDataService mds = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(mds);
		EntityMetaData emd = new DefaultEntityMetaData("test");
		when(mds.getEntityMetaData("test")).thenReturn(emd);

		Entity toReindexEntity = new DefaultEntity(emd, dataService);
		when(dataService.findOneById("test", "entityId")).thenReturn(toReindexEntity);

		reindexJob.call(this.progress);
		assertEquals(reindexAction.get(REINDEX_STATUS), FINISHED.name());

		verify(this.searchService).index(toReindexEntity, emd, indexingMode);

		verify(progress).status("######## START Reindex transaction id: [aabbcc] ########");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Reindexing test.entityId, CUDType = " + cudType.name());
		verify(progress).progress(1, "Executed all reindex actions, cleaning up the actions...");
		verify(progress).status("refreshIndex...");
		verify(progress).status("refreshIndex done.");
		verify(progress).status("######## END Reindex transaction id: [aabbcc] ########");

		verify(dataService, times(2)).update(ReindexActionMetaData.ENTITY_NAME, reindexAction);
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
		Entity reindexAction = reindexActionRegisterService
				.createReindexAction(transactionId, "test", cudType, DataType.DATA, null, 0);
		mockGetAllReindexActions(this.transactionId, Stream.of(reindexAction));
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, 1);

		MetaDataService mds = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(mds);
		EntityMetaData emd = new DefaultEntityMetaData("test");
		when(mds.getEntityMetaData("test")).thenReturn(emd);

		reindexJob.call(this.progress);
		assertEquals(reindexAction.get(REINDEX_STATUS), FINISHED.name());

		verify(this.searchService).rebuildIndex(this.dataService.getRepository("any"));

		verify(progress).status("######## START Reindex transaction id: [aabbcc] ########");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Reindexing repository test. CUDType = " + cudType.name());
		verify(progress).progress(1, "Executed all reindex actions, cleaning up the actions...");
		verify(progress).status("refreshIndex...");
		verify(progress).status("refreshIndex done.");
		verify(progress).status("######## END Reindex transaction id: [aabbcc] ########");

		verify(dataService, times(2)).update(ReindexActionMetaData.ENTITY_NAME, reindexAction);
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
		Entity reindexAction = reindexActionRegisterService
				.createReindexAction(transactionId, "test", cudType, DataType.METADATA, null, 0);
		mockGetAllReindexActions(this.transactionId, Stream.of(reindexAction));
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, 1);

		reindexJob.call(this.progress);
		assertEquals(reindexAction.get(REINDEX_STATUS), FINISHED.name());

		verify(this.searchService).rebuildIndex(this.dataService.getRepository("any"));

		verify(progress).status("######## START Reindex transaction id: [aabbcc] ########");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Reindexing repository test. CUDType = " + cudType.name());
		verify(progress).progress(1, "Executed all reindex actions, cleaning up the actions...");
		verify(progress).status("refreshIndex...");
		verify(progress).status("refreshIndex done.");
		verify(progress).status("######## END Reindex transaction id: [aabbcc] ########");

		verify(dataService, times(2)).update(ReindexActionMetaData.ENTITY_NAME, reindexAction);

		// make sure both the actions and the action job got deleted
		verify(dataService).delete(eq(ReindexActionMetaData.ENTITY_NAME), streamCaptor.capture());
		assertEquals(streamCaptor.getValue().collect(toList()), newArrayList(reindexAction));
		verify(dataService).deleteById(ReindexActionJobMetaData.ENTITY_NAME, transactionId);

		verify(dataService).deleteById(ReindexActionJobMetaData.ENTITY_NAME, transactionId);
	}

	@Test
	public void rebuildIndexDeleteMetaDataEntityTest()
	{
		Entity reindexAction = reindexActionRegisterService
				.createReindexAction(transactionId, "test", CudType.DELETE, DataType.METADATA, null, 0);
		mockGetAllReindexActions(this.transactionId, Stream.of(reindexAction));
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, 1);

		MetaDataService mds = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(mds);
		EntityMetaData emd = new DefaultEntityMetaData("test");
		when(mds.getEntityMetaData("test")).thenReturn(emd);

		reindexJob.call(this.progress);
		assertEquals(reindexAction.get(REINDEX_STATUS), FINISHED.name());

		verify(this.searchService).delete("test");

		verify(progress).status("######## START Reindex transaction id: [aabbcc] ########");
		verify(progress).setProgressMax(1);
		verify(progress).progress(0, "Dropping index of repository test.");
		verify(progress).progress(1, "Executed all reindex actions, cleaning up the actions...");
		verify(progress).status("refreshIndex...");
		verify(progress).status("refreshIndex done.");
		verify(progress).status("######## END Reindex transaction id: [aabbcc] ########");

		verify(dataService, times(2)).update(ReindexActionMetaData.ENTITY_NAME, reindexAction);
	}

	@Test
	public void reindexSingleEntitySearchServiceThrowsExceptionOnSecondEntityId()
	{
		Entity reindexAction1 = reindexActionRegisterService
				.createReindexAction(transactionId, "test", CudType.DELETE, DataType.DATA, "entityId1", 0);

		Entity reindexAction2 = reindexActionRegisterService
				.createReindexAction(transactionId, "test", CudType.DELETE, DataType.DATA, "entityId2", 1);

		Entity reindexAction3 = reindexActionRegisterService
				.createReindexAction(transactionId, "test", CudType.DELETE, DataType.DATA, "entityId3", 2);

		mockGetAllReindexActions(this.transactionId, Stream.of(reindexAction1, reindexAction2, reindexAction3));
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, 3);

		//TODO: move to beforeMethod block
		MetaDataService mds = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(mds);
		EntityMetaData emd = new DefaultEntityMetaData("test");
		when(mds.getEntityMetaData("test")).thenReturn(emd);

		MolgenisDataException mde = new MolgenisDataException("Random unrecoverable exception");
		doThrow(mde).when(searchService).deleteById("entityId2", emd);

		try
		{
			reindexJob.call(progress);
		}
		catch (Exception expected)
		{
			assertSame(expected, mde);
		}

		verify(searchService).deleteById("entityId1", emd);
		verify(searchService).deleteById("entityId2", emd);
		verify(searchService).deleteById("entityId3", emd);

		verify(searchService).refreshIndex();

		// Make sure the action status got updated and that the actionJob didn't get deleted
		assertEquals(reindexAction1.get(REINDEX_STATUS), FINISHED.name());
		assertEquals(reindexAction2.get(REINDEX_STATUS), FAILED.name());
		verify(dataService, atLeast(1)).update(ReindexActionMetaData.ENTITY_NAME, reindexAction1);
		verify(dataService, atLeast(1)).update(ReindexActionMetaData.ENTITY_NAME, reindexAction2);
		verify(dataService, never()).delete(ReindexActionJobMetaData.ENTITY_NAME, reindexActionJob);
	}
}