package org.molgenis.data.elasticsearch.reindex;

import static java.util.Arrays.asList;
import static org.molgenis.data.elasticsearch.reindex.ReindexActionJobMetaData.REINDEX_ACTION_JOB;
import static org.molgenis.data.elasticsearch.reindex.ReindexActionMetaData.REINDEX_ACTION;
import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.reindex.ReindexActionMetaData.CudType;
import org.molgenis.data.elasticsearch.reindex.ReindexActionMetaData.DataType;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ReindexActionRegisterService
{
	private static final Logger LOG = LoggerFactory.getLogger(ReindexActionRegisterService.class);

	public static final List<String> EXCLUDED_ENTITIES = asList(REINDEX_ACTION_JOB, REINDEX_ACTION_JOB);

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
					Entity reindexActionJob = dataService.findOneById(REINDEX_ACTION_JOB, transactionId);

					if (reindexActionJob == null)
					{
						reindexActionJob = this.createReindexActionJob(transactionId);
						dataService.add(REINDEX_ACTION_JOB, reindexActionJob);
					}

					int actionOrder = increaseCountReindexActionJob(reindexActionJob);
					Entity reindexAction = this
							.createReindexAction(reindexActionJob, entityMetaData.getName(), cudType, dataType,
									entityId, actionOrder);
					dataService.add(REINDEX_ACTION, reindexAction);
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
		dataService.update(REINDEX_ACTION_JOB, reindexActionJob);
		return count;
	}

	public DefaultEntity createReindexActionJob(String id)
	{
		DefaultEntity reindexActionJob = new DefaultEntity(reindexActionJobMetaData, dataService);
		reindexActionJob.set(ReindexActionJobMetaData.ID, id);
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, 0);
		return reindexActionJob;
	}

	public DefaultEntity createReindexAction(Entity transLog, String fullName, CudType cudType, DataType dataType,
			String entityId, int actionOrder)
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
