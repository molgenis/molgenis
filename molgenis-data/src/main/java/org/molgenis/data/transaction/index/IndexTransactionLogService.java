package org.molgenis.data.transaction.index;

import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.transaction.MolgenisTransactionListener;
import org.molgenis.data.transaction.index.IndexTransactionLogEntryMetaData.CudType;
import org.molgenis.data.transaction.index.IndexTransactionLogEntryMetaData.DataType;
import org.molgenis.data.transaction.index.IndexTransactionLogMetaData.IndexStatus;
import org.molgenis.data.transaction.index.IndexTransactionLogMetaData.TransactionStatus;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class IndexTransactionLogService implements MolgenisTransactionListener
{
	public static final List<String> EXCLUDED_ENTITIES = Arrays.asList(IndexTransactionLogEntryMetaData.ENTITY_NAME,
			IndexTransactionLogMetaData.ENTITY_NAME);

	private final DataService dataService;
	private final IndexTransactionLogMetaData indexTransactionLogMetaData;
	private final IndexTransactionLogEntryMetaData indexTransactionLogEntryMetaData;

	public IndexTransactionLogService(DataService dataService, IndexTransactionLogMetaData indexTransactionLogMetaData,
			IndexTransactionLogEntryMetaData indexTransactionLogEntryMetaData)
	{
		this.dataService = dataService;
		this.indexTransactionLogMetaData = indexTransactionLogMetaData;
		this.indexTransactionLogEntryMetaData = indexTransactionLogEntryMetaData;
	}

	@Override
	public void transactionStarted(String transactionId)
	{
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.add(IndexTransactionLogMetaData.ENTITY_NAME, this.createLog(transactionId));
			return null;
		});
	}

	@Override
	public void commitTransaction(String transactionId)
	{
		finishTransaction(transactionId, TransactionStatus.COMMITED);
	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
		finishTransaction(transactionId, TransactionStatus.ROLLBACK);
	}

	/**
	 * Log and create locks for an add/update/delete operation on a Repository
	 * 
	 * @param entityMetaData
	 * @param cudType
	 */
	public synchronized void log(EntityMetaData entityMetaData, CudType cudType, DataType dataType, String entityId)
	{
		if (!IndexTransactionLogService.EXCLUDED_ENTITIES.contains(entityMetaData.getName()))
		{
			String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
			if (transactionId != null)
			{
				runAsSystem(() -> {
					Entity transLog = dataService.findOne(IndexTransactionLogMetaData.ENTITY_NAME, transactionId);
					if (transLog != null)
					{
						Entity transLogEntry = this.createLogEntry(transLog, entityMetaData.getName(), cudType,
								dataType,
								entityId);

						dataService.update(IndexTransactionLogMetaData.ENTITY_NAME, transLog);
						dataService.add(IndexTransactionLogEntryMetaData.ENTITY_NAME, transLogEntry);
					}
				});
			}
		}
	}

	private synchronized int increaseCount(Entity transLog)
	{
		int count = transLog.getInt(IndexTransactionLogMetaData.LOG_COUNT).intValue() + 1;
		transLog.set(IndexTransactionLogMetaData.LOG_COUNT, count);
		return count;
	}

	public synchronized Entity createLog(String transactionId)
	{
		Entity entity = new DefaultEntity(indexTransactionLogMetaData, dataService);
		entity.set(IndexTransactionLogMetaData.TRANSACTION_ID, transactionId);
		entity.set(IndexTransactionLogMetaData.USER_NAME, SecurityUtils.getCurrentUsername());
		entity.set(IndexTransactionLogMetaData.TRANSACTION_STATUS, TransactionStatus.STARTED);
		entity.set(IndexTransactionLogMetaData.INDEX_STATUS, IndexStatus.NONE);
		entity.set(IndexTransactionLogMetaData.START_TIME, new Date());
		entity.set(IndexTransactionLogMetaData.LOG_COUNT, 0);
		return entity;
	}

	public synchronized Entity createLogEntry(Entity transLog, String fullName, CudType cudType, DataType dataType,
			String entityId)
	{
		Entity logEntry = new DefaultEntity(this.indexTransactionLogEntryMetaData, this.dataService);
		logEntry.set(IndexTransactionLogEntryMetaData.MOLGENIS_TRANSACTION_LOG, transLog);
		logEntry.set(IndexTransactionLogEntryMetaData.ENTITY_FULL_NAME, fullName);
		logEntry.set(IndexTransactionLogEntryMetaData.CUD_TYPE, cudType);
		logEntry.set(IndexTransactionLogEntryMetaData.DATA_TYPE, dataType);
		logEntry.set(IndexTransactionLogEntryMetaData.ENTITY_ID, entityId);
		logEntry.set(IndexTransactionLogEntryMetaData.LOG_ORDER, this.increaseCount(transLog));
		return logEntry;
	}

	private synchronized void finishTransaction(String transactionId, TransactionStatus transactionStatus)
	{
		RunAsSystemProxy.runAsSystem(() -> {
			Entity transLog = dataService.findOne(IndexTransactionLogMetaData.ENTITY_NAME, transactionId);
			transLog.set(IndexTransactionLogMetaData.END_TIME, new Date());
			transLog.set(IndexTransactionLogMetaData.TRANSACTION_STATUS, transactionStatus);
			dataService.update(IndexTransactionLogMetaData.ENTITY_NAME, transLog);
			return null;
		});
	}
}
