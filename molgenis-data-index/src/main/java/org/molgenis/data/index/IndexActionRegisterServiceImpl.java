package org.molgenis.data.index;

import com.google.common.collect.*;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityKey;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionGroup;
import org.molgenis.data.index.meta.IndexActionGroupFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static com.google.common.collect.Sets.union;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetaData.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetaData.IndexStatus.PENDING;
import static org.molgenis.data.transaction.TransactionManager.TRANSACTION_ID_RESOURCE_NAME;

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

	private final DataService dataService;
	private final IndexActionFactory indexActionFactory;
	private final IndexActionGroupFactory indexActionGroupFactory;

	@Autowired
	IndexActionRegisterServiceImpl(DataService dataService, IndexActionFactory indexActionFactory,
			IndexActionGroupFactory indexActionGroupFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.indexActionFactory = requireNonNull(indexActionFactory);
		this.indexActionGroupFactory = requireNonNull(indexActionGroupFactory);

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
	public synchronized void register(EntityType entityType, String entityId)
	{
		String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
		if (transactionId != null)
		{
			LOG.debug("register(entityFullName: [{}], entityId: [{}])", entityType.getId(), entityId);
			final int actionOrder = indexActionsPerTransaction.get(transactionId).size();
			if (actionOrder >= LOG_EVERY && actionOrder % LOG_EVERY == 0)
			{
				LOG.warn(
						"Transaction {} has caused {} IndexActions to be created. Consider streaming your data manipulations.",
						transactionId, actionOrder);
			}
			IndexAction indexAction = indexActionFactory.create()
					.setIndexActionGroup(indexActionGroupFactory.create(transactionId))
					.setEntityTypeId(entityType.getId()).setEntityId(entityId).setIndexStatus(PENDING);
			indexActionsPerTransaction.put(transactionId, indexAction);
		}
		else
		{
			LOG.error("Transaction id is unknown, register of entityFullName [{}] dataType [{}], entityId [{}]",
					entityType.getId(), entityId);
		}
	}

	@Override
	@RunAsSystem
	public void storeIndexActions(String transactionId)
	{
		List<IndexAction> indexActions = determineNecessaryActions();
		for (int i = 0; i < indexActions.size(); i++)
		{
			indexActions.get(i).setActionOrder(i);
		}
		if (indexActions.isEmpty())
		{
			return;
		}
		LOG.debug("Store index actions for transaction {}", transactionId);
		dataService
				.add(INDEX_ACTION_GROUP, indexActionGroupFactory.create(transactionId).setCount(indexActions.size()));
		dataService.add(INDEX_ACTION, indexActions.stream());
	}

	/**
	 * Determines which IndexActions are necessary to bring the index up to date with the current transaction.
	 *
	 * @return List<IndexAction> List of IndexActions that are necessary
	 */
	private List<IndexAction> determineNecessaryActions()
	{
		ImmutableSet<IndexAction> indexActions = copyOf(getIndexActionsForCurrentTransaction());
		if (indexActions.isEmpty())
		{
			return Collections.emptyList();
		}
		return determineNecessaryActionsInternal(indexActions, indexActions.iterator().next().getIndexActionGroup(),
				new IndexDependencyModel(dataService.getMeta().getEntityTypes().collect(toList())));
	}

	/**
	 * Determines the necessary index actions.
	 *
	 * @param indexActions     The index actions stored for the current transaction, deduplicated
	 * @param indexActionGroup The IndexActionGroup that the created IndexActions will belong to
	 * @param dependencies     {@link IndexDependencyModel} to determine which entities depend on which entities
	 * @return List of {@link IndexAction}s
	 */
	private List<IndexAction> determineNecessaryActionsInternal(ImmutableSet<IndexAction> indexActions,
			IndexActionGroup indexActionGroup, IndexDependencyModel dependencies)
	{
		Map<Boolean, List<IndexAction>> split = indexActions.stream()
				.filter(action -> !excludedEntities.contains(action.getEntityTypeId()))
				.collect(partitioningBy(IndexAction::isWholeRepository));
		ImmutableSet<String> allEntityTypeIds = indexActions.stream().map(IndexAction::getEntityTypeId)
				.collect(toImmutableSet());
		Set<String> dependentEntities = Sets.difference(
				allEntityTypeIds.stream().flatMap(dependencies::getEntityTypesDependentOn).collect(toImmutableSet()),
				excludedEntities);

		return collectResult(indexActionGroup, split.get(false), split.get(true), dependentEntities);
	}

	/**
	 * Collects the results into a List.
	 *
	 * @param indexActionGroup    the IndexGroup that all of these IndexActions will belong to
	 * @param singleEntityActions IndexActions for specific Entity instances. These will only be indexed if their repo will not be reindexed fully
	 * @param wholeRepoActions    IndexActions for the whole Repo
	 * @param dependentEntityIds  Set containing IDs of dependent EntityTypes, we will add IndexActions for the whole of these EntityTypes
	 * @return ImmutableList with the {@link IndexAction}s
	 */
	private List<IndexAction> collectResult(IndexActionGroup indexActionGroup, List<IndexAction> singleEntityActions,
			List<IndexAction> wholeRepoActions, Set<String> dependentEntityIds)
	{
		Set<String> wholeRepoIds = union(
				wholeRepoActions.stream().map(IndexAction::getEntityTypeId).collect(toImmutableSet()),
				dependentEntityIds);

		ImmutableList.Builder<IndexAction> result = ImmutableList.builder();
		result.addAll(wholeRepoActions);
		dependentEntityIds.stream().map(id -> createIndexAction(id, indexActionGroup)).forEach(result::add);
		singleEntityActions.stream().filter(action -> !wholeRepoIds.contains(action.getEntityTypeId()))
				.forEach(result::add);
		return result.build();
	}

	private IndexAction createIndexAction(String referencingEntity, IndexActionGroup indexActionGroup)
	{
		return indexActionFactory.create().setEntityTypeId(referencingEntity).setIndexActionGroup(indexActionGroup).setIndexStatus(PENDING);
	}

	@Override
	public boolean forgetIndexActions(String transactionId)
	{
		LOG.debug("Forget index actions for transaction {}", transactionId);
		return indexActionsPerTransaction.removeAll(transactionId).stream()
				.anyMatch(indexAction -> !excludedEntities.contains(indexAction.getEntityTypeId()));
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
				indexAction -> indexAction.getEntityId() != null && indexAction.getEntityTypeId()
						.equals(entityKey.getEntityTypeId()) && indexAction.getEntityId()
						.equals(entityKey.getId().toString()));
	}

	@Override
	public boolean isEntireRepositoryDirty(EntityType entityType)
	{
		return getIndexActionsForCurrentTransaction().stream().anyMatch(
				indexAction -> indexAction.getEntityId() == null && indexAction.getEntityTypeId()
						.equals(entityType.getId()));
	}

	@Override
	public boolean isRepositoryCompletelyClean(EntityType entityType)
	{
		return getIndexActionsForCurrentTransaction().stream()
				.noneMatch(indexAction -> indexAction.getEntityTypeId().equals(entityType.getId()));
	}

	@Override
	public Set<EntityKey> getDirtyEntities()
	{
		return getIndexActionsForCurrentTransaction().stream().filter(indexAction -> indexAction.getEntityId() != null)
				.map(this::createEntityKey).collect(toSet());
	}

	@Override
	public Set<String> getEntirelyDirtyRepositories()
	{
		return getIndexActionsForCurrentTransaction().stream().filter(indexAction -> indexAction.getEntityId() == null)
				.map(IndexAction::getEntityTypeId).collect(toSet());
	}

	@Override
	public Set<String> getDirtyRepositories()
	{
		return getIndexActionsForCurrentTransaction().stream().map(IndexAction::getEntityTypeId).collect(toSet());
	}

	/**
	 * Create an EntityKey
	 * Attention! MOLGENIS supports multiple id object types and the Entity id from the index registry s always a String
	 *
	 * @return EntityKey
	 */
	private EntityKey createEntityKey(IndexAction indexAction)
	{
		return EntityKey.create(indexAction.getEntityTypeId(), indexAction.getEntityId() != null ? EntityUtils
				.getTypedValue(indexAction.getEntityId(),
						dataService.getEntityType(indexAction.getEntityTypeId()).getIdAttribute()) : null);
	}

}
