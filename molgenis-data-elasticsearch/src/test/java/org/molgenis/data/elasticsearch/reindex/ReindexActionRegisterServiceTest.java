package org.molgenis.data.elasticsearch.reindex;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData.DataType;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData.ReindexStatus;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionRegisterConfig;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReindexActionRegisterServiceTest
{
	private ReindexActionRegisterService reindexActionRegisterService;
	private DataService dataService;
	private ReindexActionJobMetaData reindexActionJobMetaData = new ReindexActionJobMetaData(
			ReindexActionRegisterConfig.BACKEND);
	private ReindexActionMetaData reindexActionMetaData = new ReindexActionMetaData(
			reindexActionJobMetaData,
			ReindexActionRegisterConfig.BACKEND);

	@BeforeMethod
	public void beforeMethod()
	{
		TransactionSynchronizationManager.bindResource(MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME, "1");
		dataService = mock(DataService.class);
		reindexActionRegisterService = new ReindexActionRegisterService(dataService, reindexActionJobMetaData,
				reindexActionMetaData);
	}

	@AfterMethod
	public void afterMethod()
	{
		TransactionSynchronizationManager.unbindResource(MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME);
	}

	@Test
	public void createLog()
	{
		Entity entity = reindexActionRegisterService.createReindexActionJob("1");
		assertEquals(entity.get(ReindexActionJobMetaData.ID), "1");
		assertEquals(entity.get(ReindexActionJobMetaData.COUNT), 0);
	}

	@Test
	public void createReindexAction()
	{
		Entity reindexActionJob = reindexActionRegisterService.createReindexActionJob("1");

		Entity reindexAction = reindexActionRegisterService.createReindexAction(reindexActionJob, "full_entity_name",
				CudType.ADD, DataType.DATA, "123", 1);
		assertNotNull(reindexAction.get(ReindexActionMetaData.REINDEX_ACTION_GROUP));
		assertEquals(reindexAction.getInt(ReindexActionMetaData.ACTION_ORDER), Integer.valueOf(1));
		assertEquals(reindexAction.getString(ReindexActionMetaData.ENTITY_FULL_NAME), "full_entity_name");
		assertEquals(reindexAction.get(ReindexActionMetaData.ENTITY_ID), "123");
		assertEquals(reindexAction.get(ReindexActionMetaData.CUD_TYPE), CudType.ADD.name());
		assertEquals(reindexAction.get(ReindexActionMetaData.DATA_TYPE), DataType.DATA.name());
		assertEquals(reindexAction.get(ReindexActionMetaData.REINDEX_STATUS), ReindexStatus.PENDING.name());

		Entity reindexAction2 = reindexActionRegisterService.createReindexAction(reindexActionJob, "full_entity_name",
				CudType.DELETE, DataType.METADATA, null, 2);
		assertNotNull(reindexAction2.get(ReindexActionMetaData.REINDEX_ACTION_GROUP));
		assertEquals(reindexAction2.getInt(ReindexActionMetaData.ACTION_ORDER), Integer.valueOf(2));
		assertEquals(reindexAction2.getString(ReindexActionMetaData.ENTITY_FULL_NAME), "full_entity_name");
		assertEquals(reindexAction2.getString(ReindexActionMetaData.ENTITY_ID), null);
		assertEquals(reindexAction2.get(ReindexActionMetaData.CUD_TYPE), CudType.DELETE.name());
		assertEquals(reindexAction2.get(ReindexActionMetaData.DATA_TYPE), DataType.METADATA.name());
		assertEquals(reindexAction2.get(ReindexActionMetaData.REINDEX_STATUS), ReindexStatus.PENDING.name());
	}

	@Test
	public void testLog()
	{
		DefaultEntity reindexActionJob = reindexActionRegisterService.createReindexActionJob("1");
		when(dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, "1")).thenReturn(reindexActionJob);

		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn("non_log_entity");

		reindexActionRegisterService.register(entityMetaData, CudType.ADD, DataType.DATA, "123");
		
		verify(dataService).update(eq(ReindexActionJobMetaData.ENTITY_NAME), any(Entity.class));
		verify(dataService).add(eq(ReindexActionMetaData.ENTITY_NAME), any(Entity.class));
	}

	@Test
	public void testLogExcludedEntities()
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn(ReindexActionJobMetaData.ENTITY_NAME);

		reindexActionRegisterService.register(entityMetaData, CudType.ADD, DataType.DATA, "123");
		verifyNoMoreInteractions(dataService);

		when(entityMetaData.getName()).thenReturn(ReindexActionJobMetaData.ENTITY_NAME);

		reindexActionRegisterService.register(entityMetaData, CudType.ADD, DataType.DATA, "123");
		verifyNoMoreInteractions(dataService);
	}
}
