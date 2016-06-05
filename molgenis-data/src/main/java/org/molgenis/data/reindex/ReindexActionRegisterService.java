package org.molgenis.data.reindex;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static org.molgenis.data.reindex.meta.ReindexActionJobMetaData.REINDEX_ACTION_JOB;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_ACTION;
import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;

import java.util.Collection;
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
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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
						createReindexAction(reindexActionJobFactory.create(transactionId), entityFullName, cudType,
								dataType, entityId, actionOrder));
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
			dataService.add(REINDEX_ACTION_JOB, createReindexActionJob(transactionId, entities.size()));
			dataService.add(REINDEX_ACTION, entities.stream());
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

	public ReindexActionJob createReindexActionJob(String id, int count)
	{
		ReindexActionJob reindexActionJob = reindexActionJobFactory.create(id);
		reindexActionJob.setCount(count);
		return reindexActionJob;
	}

	public ReindexAction createReindexAction(ReindexActionJob reindexActionGroup, String entityFullName,
			CudType cudType, DataType dataType, String entityId, int actionOrder)
	{
		ReindexAction reindexAction = reindexActionFactory.create();
		reindexAction.setReindexActionGroup(reindexActionGroup);
		reindexAction.setEntityFullName(entityFullName);
		reindexAction.setCudType(cudType);
		reindexAction.setDataType(dataType);
		reindexAction.setEntityId(entityId);
		reindexAction.setActionOrder(String.valueOf(actionOrder));
		reindexAction.setReindexStatus(ReindexActionMetaData.ReindexStatus.PENDING);
		return reindexAction;
	}
}
