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
	private final IndexTransactionLogMetaData molgenisTransactionLogMetaData;
	private final IndexTransactionLogEntryMetaData molgenisTransactionLogEntryMetaData;

	public IndexTransactionLogService(DataService dataService, IndexTransactionLogMetaData molgenisTransactionLogMetaData,
			IndexTransactionLogEntryMetaData molgenisTransactionLogEntryMetaData)
	{
		this.dataService = dataService;
		this.molgenisTransactionLogMetaData = molgenisTransactionLogMetaData;
		this.molgenisTransactionLogEntryMetaData = molgenisTransactionLogEntryMetaData;
	}

	@Override
	public void transactionStarted(String transactionId)
	{
		Entity transLog = new DefaultEntity(molgenisTransactionLogMetaData, dataService);
		transLog.set(IndexTransactionLogMetaData.TRANSACTION_ID, transactionId);
		transLog.set(IndexTransactionLogMetaData.USER_NAME, SecurityUtils.getCurrentUsername());
		transLog.set(IndexTransactionLogMetaData.TRANSACTION_STATUS, TransactionStatus.STARTED);
		transLog.set(IndexTransactionLogMetaData.INDEX_STATUS, IndexStatus.NONE);
		transLog.set(IndexTransactionLogMetaData.START_TIME, new Date());
		transLog.set(IndexTransactionLogMetaData.LOG_COUNT, 0);
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.add(IndexTransactionLogMetaData.ENTITY_NAME, transLog);
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
						int order = transLog.getInt(IndexTransactionLogMetaData.LOG_COUNT).intValue() + 1;
						transLog.set(IndexTransactionLogMetaData.LOG_COUNT, order);
						dataService.update(IndexTransactionLogMetaData.ENTITY_NAME, transLog);

						Entity logEntry = new DefaultEntity(molgenisTransactionLogEntryMetaData, dataService);
						logEntry.set(IndexTransactionLogEntryMetaData.MOLGENIS_TRANSACTION_LOG, transLog);
						logEntry.set(IndexTransactionLogEntryMetaData.ENTITY_FULL_NAME, entityMetaData.getName());
						logEntry.set(IndexTransactionLogEntryMetaData.CUD_TYPE, cudType);
						logEntry.set(IndexTransactionLogEntryMetaData.DATA_TYPE, dataType);
						logEntry.set(IndexTransactionLogEntryMetaData.ENTITY_ID, entityId);
						logEntry.set(IndexTransactionLogEntryMetaData.LOG_ORDER, order);
						dataService.add(IndexTransactionLogEntryMetaData.ENTITY_NAME, logEntry);
					}
				});
			}
		}
	}

	private synchronized void finishTransaction(String transactionId, TransactionStatus transactionStatus)
	{
		RunAsSystemProxy.runAsSystem(() -> {
			Entity transLog = dataService.findOne(IndexTransactionLogMetaData.ENTITY_NAME, transactionId);
			transLog.set(IndexTransactionLogMetaData.END_TIME, new Date());
			transLog.set(IndexTransactionLogMetaData.TRANSACTION_STATUS, TransactionStatus.COMMITED);
			dataService.update(IndexTransactionLogMetaData.ENTITY_NAME, transLog);
			return null;
		});
	}
}
