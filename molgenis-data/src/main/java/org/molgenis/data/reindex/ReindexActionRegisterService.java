package org.molgenis.data.reindex;

import static org.molgenis.data.reindex.meta.ReindexActionJobMetaData.COUNT;
import static org.molgenis.data.reindex.meta.ReindexActionJobMetaData.REINDEX_ACTION_JOB;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_ACTION;
import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Set;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.reindex.meta.ReindexAction;
import org.molgenis.data.reindex.meta.ReindexActionFactory;
import org.molgenis.data.reindex.meta.ReindexActionJob;
import org.molgenis.data.reindex.meta.ReindexActionJobFactory;
import org.molgenis.data.reindex.meta.ReindexActionMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.collect.Sets;

/**
 * Registers changes made to an indexed repository that need to be fixed by reindexing
 * the relevant data.
 */
@Service
public class ReindexActionRegisterService
{
	private static final Logger LOG = LoggerFactory.getLogger(ReindexActionRegisterService.class);

	private final Set<String> excludedEntities = Sets.newConcurrentHashSet();

	@Autowired
	private DataService dataService;

	@Autowired
	private ReindexActionFactory reindexActionFactory;

	@Autowired
	private ReindexActionJobFactory reindexActionJobFactory;

	public ReindexActionRegisterService()
	{
		addExcludedEntity(REINDEX_ACTION_JOB);
		addExcludedEntity(REINDEX_ACTION);
	}

	/**
	 * Excludes an entity from being reindexed.
	 *
	 * @param entityFullName fully qualified name of the entity to exclude
	 */
	public void addExcludedEntity(String entityFullName)
	{
		excludedEntities.add(entityFullName);
	}

	/**
	 * Log and create locks for an add/update/delete operation on a Repository
	 *
	 * @param entityFullName the fully qualified name of the {@link org.molgenis.data.Repository}
	 * @param cudType        the {@link CudType} of the action
	 * @param dataType       the {@link DataType} of the action
	 * @param entityId       the ID of the entity, may be null to indicate change to entire repository
	 */
	public synchronized void register(String entityFullName, CudType cudType, DataType dataType, String entityId)
	{
		if (!excludedEntities.contains(entityFullName))
		{
			String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
			if (transactionId != null)
			{
				LOG.debug("bootstrap(entityFullName: [{}], cudType [{}], dataType: [{}], entityId: [{}])",
						entityFullName, cudType, dataType, entityId);
				runAsSystem(() -> {
					ReindexActionJob reindexActionJob = dataService
							.findOneById(REINDEX_ACTION_JOB, transactionId, ReindexActionJob.class);

					if (reindexActionJob == null)
					{
						reindexActionJob = this.createReindexActionJob(transactionId);
						dataService.add(REINDEX_ACTION_JOB, reindexActionJob);
					}

					int actionOrder = increaseCountReindexActionJob(reindexActionJob);
					Entity reindexAction = this
							.createReindexAction(reindexActionJob, entityFullName, cudType, dataType, entityId,
									actionOrder);
					dataService.add(REINDEX_ACTION, reindexAction);
				});
			}
			else
			{
				LOG.warn("Transaction id is unknown");
			}
		}
	}

	public int increaseCountReindexActionJob(Entity reindexActionJob)
	{
		int count = reindexActionJob.getInt(COUNT) + 1;
		reindexActionJob.set(COUNT, count);
		dataService.update(REINDEX_ACTION_JOB, reindexActionJob);
		return count;
	}

	public ReindexActionJob createReindexActionJob(String id)
	{
		ReindexActionJob reindexActionJob = reindexActionJobFactory.create(id);
		reindexActionJob.setCount(0);
		return reindexActionJob;
	}

	public ReindexAction createReindexAction(ReindexActionJob reindexActionGroup, String entityFullName,
			CudType cudType, DataType dataType, String entityId, int actionOrder)
	{
		ReindexAction reindexAction = reindexActionFactory.create();
		reindexAction.setReindexActionGroup(reindexActionGroup).setEntityFullName(entityFullName).setCudType(cudType)
				.setDataType(dataType).setEntityId(entityId).setActionOrder(String.valueOf(actionOrder))
				.setReindexStatus(ReindexActionMetaData.ReindexStatus.PENDING);
		return reindexAction;
	}
}
