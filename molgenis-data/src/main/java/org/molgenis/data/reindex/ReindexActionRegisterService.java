package org.molgenis.data.reindex;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.security.core.runas.RunAsSystem;
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
	private static final int LOG_EVERY = 1000;

	private final Set<String> excludedEntities = Sets.newConcurrentHashSet();

	private final Multimap<String, Entity> reindexActionsPerTransaction = synchronizedListMultimap(
			ArrayListMultimap.create());

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
		if (!excludedEntities.contains(entityFullName))
		{
			String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
			if (transactionId != null)
			{
				LOG.debug("register(entityFullName: [{}], cudType [{}], dataType: [{}], entityId: [{}])",
						entityFullName, cudType, dataType, entityId);
				final int actionOrder = reindexActionsPerTransaction.get(transactionId).size();
				if (actionOrder % LOG_EVERY == 0 && actionOrder / LOG_EVERY > 0)
				{
					LOG.warn(
							"Transaction {} has caused {} ReindexActions to be created. Consider streaming your data manipulations.",
							transactionId, actionOrder);
				}
				reindexActionsPerTransaction.put(transactionId,
						createReindexAction(transactionId, entityFullName, cudType, dataType, entityId, actionOrder));
			}
			else
			{
				LOG.warn("Transaction id is unknown");
			}
		}
	}

	/**
	 * Stores the reindex actions in the repository.
	 * Creates a ReindesActionJob to group them by.
	 *
	 * @param transactionId ID for the transaction the reindex actions were registered under
	 */
	@RunAsSystem
	public void storeReindexActions(String transactionId)
	{
		Collection<Entity> entities = reindexActionsPerTransaction.removeAll(transactionId);
		if (!entities.isEmpty())
		{
			LOG.debug("Store reindex actions for transaction {}", transactionId);
			dataService
					.add(ReindexActionJobMetaData.ENTITY_NAME, createReindexActionJob(transactionId, entities.size()));
			dataService.add(ReindexActionMetaData.ENTITY_NAME, entities.stream());
		}
	}

	/**
	 * Removes all reindex actions registered for a transaction.
	 *
	 * @param transactionId ID for the transaction the reindex actions were registered under
	 */
	public void forgetReindexActions(String transactionId)
	{
		LOG.debug("Forget reindex actions for transaction {}", transactionId);
		reindexActionsPerTransaction.removeAll(transactionId);
	}

	public DefaultEntity createReindexActionJob(String id, int count)
	{
		DefaultEntity reindexActionJob = new DefaultEntity(new ReindexActionJobMetaData(), dataService);
		reindexActionJob.set(ReindexActionJobMetaData.ID, id);
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, count);
		return reindexActionJob;
	}

	public DefaultEntity createReindexAction(String reindexActionGroup, String entityFullName, CudType cudType,
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
