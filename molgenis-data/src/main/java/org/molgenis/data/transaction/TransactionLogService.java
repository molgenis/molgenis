package org.molgenis.data.transaction;

import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionLogService implements MolgenisTransactionListener
{
	public static final List<String> EXCLUDED_ENTITIES = Arrays.asList(MolgenisTransactionLogEntryMetaData.ENTITY_NAME,
			MolgenisTransactionLogMetaData.ENTITY_NAME);

	private final DataService dataService;
	private final MolgenisTransactionLogMetaData molgenisTransactionLogMetaData;
	private final MolgenisTransactionLogEntryMetaData molgenisTransactionLogEntryMetaData;
	private final AsyncTransactionLog asyncTransactionLog;

	public TransactionLogService(DataService dataService,
			MolgenisTransactionLogMetaData molgenisTransactionLogMetaData,
			MolgenisTransactionLogEntryMetaData molgenisTransactionLogEntryMetaData,
			AsyncTransactionLog asyncTransactionLog)
	{
		this.dataService = dataService;
		this.molgenisTransactionLogMetaData = molgenisTransactionLogMetaData;
		this.molgenisTransactionLogEntryMetaData = molgenisTransactionLogEntryMetaData;
		this.asyncTransactionLog = asyncTransactionLog;
	}

	@Override
	public void transactionStarted(String transactionId)
	{
		Entity trans = new DefaultEntity(molgenisTransactionLogMetaData, dataService);
		trans.set(MolgenisTransactionLogMetaData.TRANSACTION_ID, transactionId);
		trans.set(MolgenisTransactionLogMetaData.USER_NAME, SecurityUtils.getCurrentUsername());
		trans.set(MolgenisTransactionLogMetaData.STATUS, MolgenisTransactionLogMetaData.Status.STARTED.name());
		trans.set(MolgenisTransactionLogMetaData.START_TIME, new Date());

		RunAsSystemProxy.runAsSystem(() -> {
			dataService.add(MolgenisTransactionLogMetaData.ENTITY_NAME, trans);
			return null;
		});
	}

	@Override
	public void commitTransaction(String transactionId)
	{
		finishTransaction(transactionId, MolgenisTransactionLogMetaData.Status.COMMITED);
	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
		finishTransaction(transactionId, MolgenisTransactionLogMetaData.Status.ROLLBACK);
	}

	/**
	 * Log and create locks for an add/update/delete operation on a Repository
	 * 
	 * @param entityMetaData
	 * @param type
	 */
	public synchronized void log(EntityMetaData entityMetaData, MolgenisTransactionLogEntryMetaData.Type type)
	{
		String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
		if (transactionId != null)
		{
			runAsSystem(() -> {
				Entity log = dataService.findOne(MolgenisTransactionLogMetaData.ENTITY_NAME, transactionId);
				if (log != null)
				{
					Entity logEntry = new DefaultEntity(molgenisTransactionLogEntryMetaData, dataService);
					logEntry.set(MolgenisTransactionLogEntryMetaData.MOLGENIS_TRANSACTION_LOG, log);
					logEntry.set(MolgenisTransactionLogEntryMetaData.ENTITY, entityMetaData.getName());
					logEntry.set(MolgenisTransactionLogEntryMetaData.TYPE, type);

					asyncTransactionLog.addLogEntry(logEntry);
				}
			});
		}
	}

	private synchronized void finishTransaction(String transactionId, MolgenisTransactionLogMetaData.Status status)
	{
		RunAsSystemProxy.runAsSystem(() -> {
			Entity log = dataService.findOne(MolgenisTransactionLogMetaData.ENTITY_NAME, transactionId);
			log.set(MolgenisTransactionLogMetaData.END_TIME, new Date());
			log.set(MolgenisTransactionLogMetaData.STATUS, status.name());

			asyncTransactionLog.logTransactionFinished(log);

			return null;
		});
	}
}
