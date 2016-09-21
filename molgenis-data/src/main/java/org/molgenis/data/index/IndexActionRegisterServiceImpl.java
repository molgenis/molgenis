package org.molgenis.data.index;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityKey;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionGroupFactory;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.util.EntityUtils;
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
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetaData.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetaData.IndexStatus.PENDING;
import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;

/**
 * Registers changes made to an indexed repository that need to be fixed by indexing
 * the relevant data.
 */
@Component
public class IndexActionRegisterServiceImpl implements TransactionInformation, IndexActionRegisterService
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexActionRegisterServiceImpl.class);
	private static final int LOG_EVERY = 1000;

	private final Set<String> excludedEntities = Sets.newConcurrentHashSet();

	private final Multimap<String, IndexAction> indexActionsPerTransaction = synchronizedListMultimap(
			ArrayListMultimap.create());

	@Autowired
	private DataService dataService;

	@Autowired
	private IndexActionFactory indexActionFactory;

	@Autowired
	private IndexActionGroupFactory indexActionGroupFactory;

	public IndexActionRegisterServiceImpl()
	{
		addExcludedEntity(INDEX_ACTION_GROUP);
		addExcludedEntity(INDEX_ACTION);
	}

	@Override
	public void addExcludedEntity(String entityFullName)
	{
		excludedEntities.add(entityFullName);
	}

	@Transactional
	@Override
	public synchronized void register(String entityFullName, String entityId)
	{
		String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
		if (transactionId != null)
		{
			LOG.debug("register(entityFullName: [{}], entityId: [{}])", entityFullName, entityId);
			final int actionOrder = indexActionsPerTransaction.get(transactionId).size();
			if (actionOrder >= LOG_EVERY && actionOrder % LOG_EVERY == 0)
			{
				LOG.warn(
						"Transaction {} has caused {} IndexActions to be created. Consider streaming your data manipulations.",
						transactionId, actionOrder);
			}
			IndexAction indexAction = indexActionFactory.create()
					.setIndexActionGroup(indexActionGroupFactory.create(transactionId))
					.setEntityFullName(entityFullName).setEntityId(entityId).setIndexStatus(PENDING);
			indexActionsPerTransaction.put(transactionId, indexAction);
		}
		else
		{
			LOG.error("Transaction id is unknown, register of entityFullName [{}] dataType [{}], entityId [{}]",
					entityFullName, entityId);

		}
	}

	@Override
	@RunAsSystem
	public void storeIndexActions(String transactionId)
	{
		Set<IndexAction> indexActionSet = getIndexActionsForCurrentTransaction().stream()
				.filter(indexAction -> !excludedEntities.contains(indexAction.getEntityFullName()))
				.flatMap(this::addReferencingEntities).collect(toSet());

		List<IndexAction> indexActions1 = newArrayList(indexActionSet);
		for (int i = 0; i < indexActions1.size(); i++)
		{
			indexActions1.get(i).setActionOrder(i);
		}
		List<IndexAction> indexActions = indexActions1;
		if (!indexActions.isEmpty())
		{
			LOG.debug("Store index actions for transaction {}", transactionId);
			dataService.add(INDEX_ACTION_GROUP,
					indexActionGroupFactory.create(transactionId).setCount(indexActions.size()));
			dataService.add(INDEX_ACTION, indexActions.stream());
		}
	}

	/**
	 * Add for all referencing entities an index action
	 *
	 * @param indexAction
	 * @return Stream<IndexAction>
	 */
	private Stream<IndexAction> addReferencingEntities(IndexAction indexAction)
	{
		if (indexAction.getEntityId() != null || !dataService.hasRepository(
				indexAction.getEntityFullName())) // When entity is deleted the entityMetaData cannot be retrieved
		{
			return Stream.of(indexAction);
		}
		return Stream.concat(Stream.of(indexAction), EntityUtils
				.getReferencingEntityMetaData(dataService.getEntityMetaData(indexAction.getEntityFullName()),
						dataService).stream()
				.map(pair -> indexActionFactory.create().setEntityFullName(pair.getA().getName())
						.setIndexActionGroup(indexAction.getIndexActionGroup()).setIndexStatus(PENDING)));
	}

	@Override
	public boolean forgetIndexActions(String transactionId)
	{
		LOG.debug("Forget index actions for transaction {}", transactionId);
		return indexActionsPerTransaction.removeAll(transactionId).stream()
				.anyMatch(indexAction -> !excludedEntities.contains(indexAction.getEntityFullName()));
	}

	private Collection<IndexAction> getIndexActionsForCurrentTransaction()
	{
		String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
		return Optional.of(indexActionsPerTransaction.get(transactionId)).orElse(emptyList());
	}

	/* TransactionInformation implementation */

	@Override
	public boolean isEntityDirty(EntityKey entityKey)
	{
		return getIndexActionsForCurrentTransaction().stream().anyMatch(
				indexAction -> indexAction.getEntityId() != null && entityKey
						.equals(EntityKey.create(indexAction.getEntityFullName(), indexAction.getEntityId())));
	}

	@Override
	public boolean isEntireRepositoryDirty(String entityName)
	{
		return getIndexActionsForCurrentTransaction().stream().anyMatch(
				indexAction -> indexAction.getEntityId() == null && indexAction.getEntityFullName().equals(entityName));
	}

	@Override
	public boolean isRepositoryCompletelyClean(String entityName)
	{
		return getIndexActionsForCurrentTransaction().stream()
				.noneMatch(indexAction -> indexAction.getEntityFullName().equals(entityName));
	}

	@Override
	public Set<EntityKey> getDirtyEntities()
	{
		return getIndexActionsForCurrentTransaction().stream().filter(indexAction -> indexAction.getEntityId() != null)
				.map(indexAction -> EntityKey.create(indexAction.getEntityFullName(), indexAction.getEntityId()))
				.collect(toSet());
	}

	@Override
	public Set<String> getEntirelyDirtyRepositories()
	{
		return getIndexActionsForCurrentTransaction().stream().filter(indexAction -> indexAction.getEntityId() == null)
				.map(IndexAction::getEntityFullName).collect(toSet());
	}

	@Override
	public Set<String> getDirtyRepositories()
	{
		return getIndexActionsForCurrentTransaction().stream().map(IndexAction::getEntityFullName).collect(toSet());
	}

}
