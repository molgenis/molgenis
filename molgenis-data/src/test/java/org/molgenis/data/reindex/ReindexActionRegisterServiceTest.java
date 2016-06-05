package org.molgenis.data.reindex;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

public class ReindexActionRegisterServiceTest
{
	@InjectMocks
	private ReindexActionRegisterService reindexActionRegisterService = new ReindexActionRegisterService();
	@Mock
	private DataService dataService;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		TransactionSynchronizationManager.bindResource(MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME, "1");
	}

	@AfterMethod
	public void afterMethod()
	{
		TransactionSynchronizationManager.unbindResource(MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME);
	}

	@Test
	public void createLog()
	{
		Entity entity = reindexActionRegisterService.createReindexActionJob("1", 5);
		assertEquals(entity.get(ReindexActionJobMetaData.ID), "1");
		assertEquals(entity.get(ReindexActionJobMetaData.COUNT), 5);
	}

	@Test
	public void createReindexAction()
	{
		Entity reindexActionJob = reindexActionRegisterService.createReindexActionJob("1", 2);

		when(dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, "1")).thenReturn(reindexActionJob);

		Entity reindexAction = reindexActionRegisterService
				.createReindexAction("1", "full_entity_name", CudType.CREATE, DataType.DATA, "123", 1);
		assertNotNull(reindexAction.get(ReindexActionMetaData.REINDEX_ACTION_GROUP));
		assertEquals(reindexAction.getInt(ReindexActionMetaData.ACTION_ORDER), Integer.valueOf(1));
		assertEquals(reindexAction.getString(ReindexActionMetaData.ENTITY_FULL_NAME), "full_entity_name");
		assertEquals(reindexAction.get(ReindexActionMetaData.ENTITY_ID), "123");
		assertEquals(reindexAction.get(ReindexActionMetaData.CUD_TYPE), CudType.CREATE.name());
		assertEquals(reindexAction.get(ReindexActionMetaData.DATA_TYPE), DataType.DATA.name());
		assertEquals(reindexAction.get(ReindexActionMetaData.REINDEX_STATUS), ReindexStatus.PENDING.name());

		Entity reindexAction2 = reindexActionRegisterService
				.createReindexAction("1", "full_entity_name", CudType.DELETE, DataType.METADATA, null, 2);
		assertNotNull(reindexAction2.get(ReindexActionMetaData.REINDEX_ACTION_GROUP));
		assertEquals(reindexAction2.getInt(ReindexActionMetaData.ACTION_ORDER), Integer.valueOf(2));
		assertEquals(reindexAction2.getString(ReindexActionMetaData.ENTITY_FULL_NAME), "full_entity_name");
		assertEquals(reindexAction2.getString(ReindexActionMetaData.ENTITY_ID), null);
		assertEquals(reindexAction2.get(ReindexActionMetaData.CUD_TYPE), CudType.DELETE.name());
		assertEquals(reindexAction2.get(ReindexActionMetaData.DATA_TYPE), DataType.METADATA.name());
		assertEquals(reindexAction2.get(ReindexActionMetaData.REINDEX_STATUS), ReindexStatus.PENDING.name());
	}

	@Test
	public void testLogAndStore()
	{
		DefaultEntity reindexActionJob = reindexActionRegisterService.createReindexActionJob("1", 1);
		when(dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, "1")).thenReturn(reindexActionJob);

		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn("non_log_entity");

		reindexActionRegisterService.register(entityMetaData.getName(), CudType.CREATE, DataType.DATA, "123");

		verifyZeroInteractions(dataService);

		reindexActionRegisterService.storeReindexActions("1");

		verify(dataService).add(eq(ReindexActionJobMetaData.ENTITY_NAME), any(Entity.class));
		verify(dataService).add(eq(ReindexActionMetaData.ENTITY_NAME), any(Stream.class));
	}

	@Test
	public void testLogAndForget()
	{
		DefaultEntity reindexActionJob = reindexActionRegisterService.createReindexActionJob("1", 1);
		when(dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, "1")).thenReturn(reindexActionJob);

		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn("non_log_entity");

		reindexActionRegisterService.register(entityMetaData.getName(), CudType.CREATE, DataType.DATA, "123");

		verifyZeroInteractions(dataService);

		reindexActionRegisterService.forgetReindexActions("1");

		verifyZeroInteractions(dataService);

		reindexActionRegisterService.storeReindexActions("1");

		verifyZeroInteractions(dataService);
	}

	@Test
	public void testLogExcludedEntities()
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn("ABC");
		reindexActionRegisterService.addExcludedEntity("ABC");

		reindexActionRegisterService.register(entityMetaData.getName(), CudType.CREATE, DataType.DATA, "123");
		verifyNoMoreInteractions(dataService);

		when(entityMetaData.getName()).thenReturn(ReindexActionJobMetaData.ENTITY_NAME);

		reindexActionRegisterService.register(entityMetaData.getName(), CudType.CREATE, DataType.DATA, "123");
		verifyNoMoreInteractions(dataService);
	}
}
