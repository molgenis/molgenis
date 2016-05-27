package org.molgenis.data.reindex;

import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Set;

import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;
import org.molgenis.data.support.DefaultEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

	public ReindexActionRegisterService()
	{
		addExcludedEntity(ReindexActionJobMetaData.ENTITY_NAME);
		addExcludedEntity(ReindexActionMetaData.ENTITY_NAME);
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
		LOG.debug("register(entityFullName: [{}], cudType [{}], dataType: [{}], entityId: [{}])", entityFullName,
				cudType, dataType, entityId);
		if (!excludedEntities.contains(entityFullName))
		{
			String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
			if (transactionId != null)
			{
				runAsSystem(() -> {
					Entity reindexActionJob = dataService
							.findOneById(ReindexActionJobMetaData.ENTITY_NAME, transactionId);

					if (reindexActionJob == null)
					{
						reindexActionJob = this.createReindexActionJob(transactionId);
						dataService.add(ReindexActionJobMetaData.ENTITY_NAME, reindexActionJob);
					}

					int actionOrder = increaseCountReindexActionJob(reindexActionJob);
					Entity reindexAction = this
							.createReindexAction(reindexActionJob, entityFullName, cudType, dataType, entityId,
									actionOrder);
					dataService.add(ReindexActionMetaData.ENTITY_NAME, reindexAction);
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
		int count = reindexActionJob.getInt(ReindexActionJobMetaData.COUNT) + 1;
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, count);
		dataService.update(ReindexActionJobMetaData.ENTITY_NAME, reindexActionJob);
		return count;
	}

	public DefaultEntity createReindexActionJob(String id)
	{
		DefaultEntity reindexActionJob = new DefaultEntity(new ReindexActionJobMetaData(), dataService);
		reindexActionJob.set(ReindexActionJobMetaData.ID, id);
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, 0);
		return reindexActionJob;
	}

	public DefaultEntity createReindexAction(Entity reindexActionGroup, String entityFullName, CudType cudType,
			DataType dataType, String entityId, int actionOrder)
	{
		DefaultEntity reindexAction = new DefaultEntity(new ReindexActionMetaData(), this.dataService);
		reindexAction.set(ReindexActionMetaData.REINDEX_ACTION_GROUP, reindexActionGroup);
		reindexAction.set(ReindexActionMetaData.ENTITY_FULL_NAME, entityFullName);
		reindexAction.set(ReindexActionMetaData.CUD_TYPE, cudType);
		reindexAction.set(ReindexActionMetaData.DATA_TYPE, dataType);
		reindexAction.set(ReindexActionMetaData.ENTITY_ID, entityId);
		reindexAction.set(ReindexActionMetaData.ACTION_ORDER, actionOrder);
		reindexAction.set(ReindexActionMetaData.REINDEX_STATUS, ReindexActionMetaData.ReindexStatus.PENDING);
		return reindexAction;
	}
}
