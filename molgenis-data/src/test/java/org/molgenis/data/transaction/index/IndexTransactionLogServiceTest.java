package org.molgenis.data.transaction.index;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Date;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.molgenis.data.transaction.index.IndexTransactionLogEntryMetaData.CudType;
import org.molgenis.data.transaction.index.IndexTransactionLogEntryMetaData.DataType;
import org.molgenis.data.transaction.index.IndexTransactionLogMetaData.IndexStatus;
import org.molgenis.data.transaction.index.IndexTransactionLogMetaData.TransactionStatus;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IndexTransactionLogServiceTest
{
	private IndexTransactionLogService indexTransactionLogService;
	private DataService dataService;
	private IndexTransactionLogMetaData indexTransactionLogMetaData = new IndexTransactionLogMetaData(
			IndexTransactionConfig.INDEX_LOG_BACKEND_NAME);
	private IndexTransactionLogEntryMetaData indexTransactionLogEntryMetaData = new IndexTransactionLogEntryMetaData(
			indexTransactionLogMetaData,
			IndexTransactionConfig.INDEX_LOG_BACKEND_NAME);

	@BeforeMethod
	public void beforeMethod()
	{
		TransactionSynchronizationManager.bindResource(MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME, "1");
		dataService = mock(DataService.class);
		indexTransactionLogService = new IndexTransactionLogService(dataService, indexTransactionLogMetaData,
				indexTransactionLogEntryMetaData);
	}

	@AfterMethod
	public void afterMethod()
	{
		TransactionSynchronizationManager.unbindResource(MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME);
	}

	@Test
	public void createLog()
	{
		Entity entity = indexTransactionLogService.createLog("1");
		assertEquals(entity.get(IndexTransactionLogMetaData.TRANSACTION_ID), "1");
		assertEquals(entity.get(IndexTransactionLogMetaData.USER_NAME), null);
		assertNotNull(entity.get(IndexTransactionLogMetaData.START_TIME));
		assertEquals(entity.get(IndexTransactionLogMetaData.TRANSACTION_STATUS), TransactionStatus.STARTED.name());
		assertEquals(entity.get(IndexTransactionLogMetaData.INDEX_STATUS), IndexStatus.NONE.name());
		assertNull(entity.get(IndexTransactionLogMetaData.END_TIME));
		assertEquals(entity.get(IndexTransactionLogMetaData.LOG_COUNT), 0);
	}

	@Test
	public void createLogEntry()
	{
		Entity transLog = indexTransactionLogService.createLog("1");

		Entity transLogEntry = indexTransactionLogService.createLogEntry(transLog, "full_entity_name", CudType.ADD,
				DataType.DATA, "123");
		assertNotNull(transLogEntry.get(IndexTransactionLogEntryMetaData.MOLGENIS_TRANSACTION_LOG));
		assertEquals(transLogEntry.getInt(IndexTransactionLogEntryMetaData.LOG_ORDER), Integer.valueOf(1));
		assertEquals(transLogEntry.getString(IndexTransactionLogEntryMetaData.ENTITY_FULL_NAME), "full_entity_name");
		assertEquals(transLogEntry.get(IndexTransactionLogEntryMetaData.ENTITY_ID), "123");
		assertEquals(transLogEntry.get(IndexTransactionLogEntryMetaData.CUD_TYPE), CudType.ADD.name());
		assertEquals(transLogEntry.get(IndexTransactionLogEntryMetaData.DATA_TYPE), DataType.DATA.name());

		Entity transLogEntry2 = indexTransactionLogService.createLogEntry(transLog, "full_entity_name", CudType.DELETE,
				DataType.METADATA, null);
		assertNotNull(transLogEntry2.get(IndexTransactionLogEntryMetaData.MOLGENIS_TRANSACTION_LOG));
		assertEquals(transLogEntry2.getInt(IndexTransactionLogEntryMetaData.LOG_ORDER), Integer.valueOf(2));
		assertEquals(transLogEntry2.getString(IndexTransactionLogEntryMetaData.ENTITY_FULL_NAME), "full_entity_name");
		assertEquals(transLogEntry2.getString(IndexTransactionLogEntryMetaData.ENTITY_ID), null);
		assertEquals(transLogEntry2.get(IndexTransactionLogEntryMetaData.CUD_TYPE), CudType.DELETE.name());
		assertEquals(transLogEntry2.get(IndexTransactionLogEntryMetaData.DATA_TYPE), DataType.METADATA.name());
	}

	@Test
	public void testLog()
	{
		DefaultEntity actualTransLog = this.createNewTransLogEntity("1");
		when(dataService.findOneById(IndexTransactionLogMetaData.ENTITY_NAME, "1")).thenReturn(actualTransLog);

		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn("non_log_entity");

		indexTransactionLogService.log(entityMetaData, CudType.ADD, DataType.DATA, "123");
		
		verify(dataService).update(eq(IndexTransactionLogMetaData.ENTITY_NAME), any(Entity.class));
		verify(dataService).add(eq(IndexTransactionLogEntryMetaData.ENTITY_NAME), any(Entity.class));
	}

	@Test
	public void testLogExcludedEntities()
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn(IndexTransactionLogMetaData.ENTITY_NAME);
		indexTransactionLogService.log(entityMetaData, CudType.ADD, DataType.DATA, "123");
		verifyNoMoreInteractions(dataService);

		when(entityMetaData.getName()).thenReturn(IndexTransactionLogMetaData.ENTITY_NAME);
		indexTransactionLogService.log(entityMetaData, CudType.ADD, DataType.DATA, "123");
		verifyNoMoreInteractions(dataService);
	}
	
	@Test
	public void transactionStarted()
	{
		indexTransactionLogService.transactionStarted("2");
		verify(dataService).add(eq(IndexTransactionLogMetaData.ENTITY_NAME), any(Entity.class));
	}
	
	@Test
	public void commitTransaction()
	{
		Entity entity = mock(Entity.class);
		when(dataService.findOneById(IndexTransactionLogMetaData.ENTITY_NAME, "2")).thenReturn(entity);
		indexTransactionLogService.commitTransaction("2");
		verify(entity).set(eq(IndexTransactionLogMetaData.END_TIME), any(Date.class));
		verify(entity).set(IndexTransactionLogMetaData.TRANSACTION_STATUS, TransactionStatus.COMMITED);
		verify(dataService).update(IndexTransactionLogMetaData.ENTITY_NAME, entity);
	}

	@Test
	public void rollbackTransaction()
	{
		Entity entity = mock(Entity.class);
		when(dataService.findOneById(IndexTransactionLogMetaData.ENTITY_NAME, "2")).thenReturn(entity);
		indexTransactionLogService.rollbackTransaction("2");
		verify(entity).set(eq(IndexTransactionLogMetaData.END_TIME), any(Date.class));
		verify(entity).set(IndexTransactionLogMetaData.TRANSACTION_STATUS, TransactionStatus.ROLLBACK);
		verify(dataService).update(IndexTransactionLogMetaData.ENTITY_NAME, entity);
	}

	private DefaultEntity createNewTransLogEntity(String transactionId)
	{
		DefaultEntity transLogEntity = new DefaultEntity(indexTransactionLogMetaData, dataService);
		transLogEntity.set(IndexTransactionLogMetaData.TRANSACTION_ID, transactionId);
		transLogEntity.set(IndexTransactionLogMetaData.USER_NAME, SecurityUtils.getCurrentUsername());
		transLogEntity.set(IndexTransactionLogMetaData.TRANSACTION_STATUS, TransactionStatus.STARTED);
		transLogEntity.set(IndexTransactionLogMetaData.INDEX_STATUS, IndexStatus.FAILED);
		transLogEntity.set(IndexTransactionLogMetaData.START_TIME, new Date());
		transLogEntity.set(IndexTransactionLogMetaData.LOG_COUNT, 0);
		return transLogEntity;
	}
}
