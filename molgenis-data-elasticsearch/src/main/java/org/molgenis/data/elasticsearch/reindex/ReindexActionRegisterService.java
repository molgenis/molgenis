package org.molgenis.data.elasticsearch.reindex;

import static org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMetaData.REINDEX_JOB_EXECUTION;
import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Arrays;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData.DataType;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ReindexActionRegisterService
{
	private static final Logger LOG = LoggerFactory.getLogger(ReindexActionRegisterService.class);

	public static final List<String> EXCLUDED_ENTITIES = Arrays.asList(ReindexActionMetaData.ENTITY_NAME,
			ReindexActionJobMetaData.ENTITY_NAME, REINDEX_JOB_EXECUTION);

	private final DataService dataService;
	private final ReindexActionJobMetaData reindexActionJobMetaData;
	private final ReindexActionMetaData reindexActionMetaData;

	public ReindexActionRegisterService(DataService dataService, ReindexActionJobMetaData reindexActionJobMetaData,
			ReindexActionMetaData reindexActionMetaData)
	{
		this.dataService = dataService;
		this.reindexActionJobMetaData = reindexActionJobMetaData;
		this.reindexActionMetaData = reindexActionMetaData;
	}

	/**
	 * Log and create locks for an add/update/delete operation on a Repository
	 * 
	 * @param entityMetaData
	 * @param cudType
	 */
	public void register(EntityMetaData entityMetaData, CudType cudType, DataType dataType, String entityId)
	{
		if (!ReindexActionRegisterService.EXCLUDED_ENTITIES.contains(entityMetaData.getName()))
		{
			String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
			if (transactionId != null)
			{
				runAsSystem(() -> {
					Entity reindexActionJob = dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, transactionId);

					if (reindexActionJob == null)
					{
						reindexActionJob = this.createReindexActionJob(transactionId);
						dataService.add(ReindexActionJobMetaData.ENTITY_NAME, reindexActionJob);
					}

					int actionOrder = increaseCountReindexActionJob(reindexActionJob);
					Entity reindexAction = this.createReindexAction(reindexActionJob, entityMetaData.getName(),
							cudType, dataType, entityId, actionOrder);
					dataService.add(ReindexActionMetaData.ENTITY_NAME, reindexAction);
				});
			}
			else
			{
				LOG.warn("Transaction id is unknown");
			}
		}
	}

	private int increaseCountReindexActionJob(Entity reindexActionJob)
	{
		int count = reindexActionJob.getInt(ReindexActionJobMetaData.COUNT).intValue() + 1;
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, count);
		dataService.update(ReindexActionJobMetaData.ENTITY_NAME, reindexActionJob);
		return count;
	}

	public DefaultEntity createReindexActionJob(String id)
	{
		DefaultEntity reindexActionJob = new DefaultEntity(reindexActionJobMetaData, dataService);
		reindexActionJob.set(ReindexActionJobMetaData.ID, id);
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, 0);
		return reindexActionJob;
	}

	public DefaultEntity createReindexAction(Entity transLog, String fullName, CudType cudType,
			DataType dataType, String entityId, int actionOrder)
	{
		DefaultEntity reindexAction = new DefaultEntity(this.reindexActionMetaData, this.dataService);
		reindexAction.set(ReindexActionMetaData.REINDEX_ACTION_GROUP, transLog);
		reindexAction.set(ReindexActionMetaData.ENTITY_FULL_NAME, fullName);
		reindexAction.set(ReindexActionMetaData.CUD_TYPE, cudType);
		reindexAction.set(ReindexActionMetaData.DATA_TYPE, dataType);
		reindexAction.set(ReindexActionMetaData.ENTITY_ID, entityId);
		reindexAction.set(ReindexActionMetaData.ACTION_ORDER, actionOrder);
		reindexAction.set(ReindexActionMetaData.REINDEX_STATUS, ReindexActionMetaData.ReindexStatus.PENDING);
		return reindexAction;
	}
}
