package org.molgenis.data.reindex;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityKey;
import org.molgenis.data.reindex.meta.ReindexAction;
import org.molgenis.data.reindex.meta.ReindexActionFactory;
import org.molgenis.data.reindex.meta.ReindexActionGroupFactory;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.reindex.meta.ReindexActionGroupMetaData.REINDEX_ACTION_GROUP;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_ACTION;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.PENDING;
import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;

/**
 * Registers changes made to an indexed repository that need to be fixed by reindexing
 * the relevant data.
 */
@Component
public class ReindexActionRegisterServiceImpl implements TransactionInformation, ReindexActionRegisterService
{
	private static final Logger LOG = LoggerFactory.getLogger(ReindexActionRegisterServiceImpl.class);
	private static final int LOG_EVERY = 1000;

	private final Set<String> excludedEntities = Sets.newConcurrentHashSet();

	private final Multimap<String, ReindexAction> reindexActionsPerTransaction = synchronizedListMultimap(
			ArrayListMultimap.create());

	@Autowired
	private DataService dataService;

	@Autowired
	private ReindexActionFactory reindexActionFactory;

	@Autowired
	private ReindexActionGroupFactory reindexActionGroupFactory;

	public ReindexActionRegisterServiceImpl()
	{
		addExcludedEntity(REINDEX_ACTION_GROUP);
		addExcludedEntity(REINDEX_ACTION);
	}

	@Override
	public void addExcludedEntity(String entityFullName)
	{
		excludedEntities.add(entityFullName);
	}

	@Transactional
	@Override
	public synchronized void register(String entityFullName, CudType cudType, DataType dataType, String entityId)
	{
		String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
		if (transactionId != null)
		{
			LOG.debug("register(entityFullName: [{}], cudType [{}], dataType: [{}], entityId: [{}])", entityFullName,
					cudType, dataType, entityId);
			final int actionOrder = reindexActionsPerTransaction.get(transactionId).size();
			if (actionOrder >= LOG_EVERY && actionOrder % LOG_EVERY == 0)
			{
				LOG.warn(
						"Transaction {} has caused {} ReindexActions to be created. Consider streaming your data manipulations.",
						transactionId, actionOrder);
			}
			ReindexAction reindexAction = reindexActionFactory.create()
					.setReindexActionGroup(reindexActionGroupFactory.create(transactionId))
					.setEntityFullName(entityFullName).setCudType(cudType).setDataType(dataType).setEntityId(entityId)
					.setReindexStatus(PENDING);
			reindexActionsPerTransaction.put(transactionId, reindexAction);
		}
		else
		{
			LOG.error(
					"Transaction id is unknown, register of entityFullName [{}], cudType [{}], dataType [{}], entityId [{}]",
					entityFullName, cudType, dataType, entityId);

		}
	}

	@Override
	@RunAsSystem
	public void storeReindexActions(String transactionId)
	{
		List<ReindexAction> reindexActions = filterExcludedEntities(getReindexActionsForCurrentTransaction());
		if (!reindexActions.isEmpty())
		{
			LOG.debug("Store reindex actions for transaction {}", transactionId);
			dataService.add(REINDEX_ACTION_GROUP,
					reindexActionGroupFactory.create(transactionId).setCount(reindexActions.size()));
			dataService.add(REINDEX_ACTION, reindexActions.stream());
		}
	}

	private List<ReindexAction> filterExcludedEntities(Collection<ReindexAction> reindexActionsForCurrentTransaction)
	{
		List<ReindexAction> reindexActions = reindexActionsForCurrentTransaction.stream()
				.filter(reindexAction -> !excludedEntities.contains(reindexAction.getEntityFullName()))
				.collect(toList());
		for (int i = 0; i < reindexActions.size(); i++)
		{
			reindexActions.get(i).setActionOrder(i);
		}
		return reindexActions;
	}

	@Override
	public boolean forgetReindexActions(String transactionId)
	{
		LOG.debug("Forget reindex actions for transaction {}", transactionId);
		return !filterExcludedEntities(reindexActionsPerTransaction.removeAll(transactionId)).isEmpty();
	}

	private Collection<ReindexAction> getReindexActionsForCurrentTransaction()
	{
		String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
		return Optional.of(reindexActionsPerTransaction.get(transactionId)).orElse(emptyList());
	}

	/* TransactionInformation implementation */

	@Override
	public boolean isEntityDirty(EntityKey entityKey)
	{
		return getReindexActionsForCurrentTransaction().stream().anyMatch(
				reindexAction -> reindexAction.getEntityId() != null && entityKey
						.equals(EntityKey.create(reindexAction.getEntityFullName(), reindexAction.getEntityId())));
	}

	@Override
	public boolean isRepositoryDirty(String entityName)
	{
		return getReindexActionsForCurrentTransaction().stream().anyMatch(
				reindexAction -> reindexAction.getEntityId() == null && reindexAction.getEntityFullName()
						.equals(entityName));
	}

	@Override
	public Set<EntityKey> getDirtyEntities()
	{
		return getReindexActionsForCurrentTransaction().stream()
				.filter(reindexAction -> reindexAction.getEntityId() != null)
				.map(reindexAction -> EntityKey.create(reindexAction.getEntityFullName(), reindexAction.getEntityId()))
				.collect(toSet());
	}

	@Override
	public Set<String> getDirtyRepositories()
	{
		return getReindexActionsForCurrentTransaction().stream()
				.filter(reindexAction -> reindexAction.getEntityId() == null).map(ReindexAction::getEntityFullName)
				.collect(toSet());
	}
}
