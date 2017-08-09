package org.molgenis.data.index;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityKey;
import org.molgenis.data.Fetch;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionGroup;
import org.molgenis.data.index.meta.IndexActionGroupFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.index.IndexDependencyModel.ENTITY_TYPE_FETCH;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetaData.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetaData.IndexStatus.PENDING;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
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
	private static final int ENTITY_FETCH_PAGE_SIZE = 1000;

	private final Multimap<String, IndexAction> indexActionsPerTransaction = synchronizedListMultimap(
			ArrayListMultimap.create());

	private final DataService dataService;
	private final IndexActionFactory indexActionFactory;
	private final IndexActionGroupFactory indexActionGroupFactory;
	private final IndexingStrategy indexingStrategy;

	private final Set<String> excludedEntities = Sets.newConcurrentHashSet();

	@Autowired
	IndexActionRegisterServiceImpl(DataService dataService, IndexActionFactory indexActionFactory,
			IndexActionGroupFactory indexActionGroupFactory, IndexingStrategy indexingStrategy)
	{
		this.dataService = requireNonNull(dataService);
		this.indexActionFactory = requireNonNull(indexActionFactory);
		this.indexActionGroupFactory = requireNonNull(indexActionGroupFactory);
		this.indexingStrategy = requireNonNull(indexingStrategy);

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
														.setIndexActionGroup(
																indexActionGroupFactory.create(transactionId))
														.setEntityTypeId(entityType.getId())
														.setEntityId(entityId)
														.setIndexStatus(PENDING);
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
		Collection<IndexAction> indexActionsForCurrentTransaction = getIndexActionsForCurrentTransaction();
		if (indexActionsForCurrentTransaction.isEmpty())
		{
			return;
		}
		IndexActionGroup indexActionGroup = indexActionsForCurrentTransaction.iterator().next().getIndexActionGroup();
		Set<Impact> changes = indexActionsForCurrentTransaction.stream()
															   .map(indexAction -> Impact.createSingleEntityImpact(
																	   indexAction.getEntityTypeId(),
																	   indexAction.getEntityId()))
															   .collect(toSet());
		IndexDependencyModel dependencyModel = new IndexDependencyModel(getEntityTypes());
		List<IndexAction> indexActions = indexingStrategy.determineImpact(changes, dependencyModel)
														 .stream()
														 .filter(key -> !excludedEntities.contains(
																 key.getEntityTypeId()))
														 .map(key -> createIndexAction(indexActionGroup, key))
														 .collect(toList());
		if (indexActions.isEmpty())
		{
			return;
		}
		for (int i = 0; i < indexActions.size(); i++)
		{
			indexActions.get(i).setActionOrder(i);
		}

		LOG.debug("Store index actions for transaction {}", transactionId);
		dataService.add(INDEX_ACTION_GROUP,
				indexActionGroupFactory.create(transactionId).setCount(indexActions.size()));
		dataService.add(INDEX_ACTION, indexActions.stream());
	}

	private IndexAction createIndexAction(IndexActionGroup indexActionGroup, Impact key)
	{
		IndexAction indexAction = indexActionFactory.create();
		indexAction.setIndexStatus(PENDING);
		indexAction.setEntityId((String) key.getId());
		indexAction.setEntityTypeId(key.getEntityTypeId());
		indexAction.setIndexActionGroup(indexActionGroup);
		return indexAction;
	}

	/**
	 * Retrieves all {@link EntityType}s.
	 * Queryies in pages of size ENTITY_FETCH_PAGE_SIZE so that results can be cached.
	 * Uses a {@link Fetch} that specifies all fields needed to determine the necessary index actions.
	 *
	 * @return List containing all {@link EntityType}s.
	 */
	private List<EntityType> getEntityTypes()
	{
		QueryImpl<EntityType> query = new QueryImpl<>();
		query.setPageSize(ENTITY_FETCH_PAGE_SIZE);
		query.setFetch(ENTITY_TYPE_FETCH);

		List<EntityType> result = newArrayList();
		for (int pageNum = 0; result.size() == pageNum * ENTITY_FETCH_PAGE_SIZE; pageNum++)
		{
			query.offset(pageNum * ENTITY_FETCH_PAGE_SIZE);
			dataService.findAll(ENTITY_TYPE_META_DATA, query, EntityType.class).forEach(result::add);
		}
		return result;
	}

	@Override
	public boolean forgetIndexActions(String transactionId)
	{
		LOG.debug("Forget index actions for transaction {}", transactionId);
		return indexActionsPerTransaction.removeAll(transactionId)
										 .stream()
										 .anyMatch(indexAction -> !excludedEntities.contains(
												 indexAction.getEntityTypeId()));
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
		return getIndexActionsForCurrentTransaction().stream()
													 .anyMatch(indexAction -> indexAction.getEntityId() != null
															 && indexAction.getEntityTypeId()
																		   .equals(entityKey.getEntityTypeId())
															 && indexAction.getEntityId()
																		   .equals(entityKey.getId().toString()));
	}

	@Override
	public boolean isEntireRepositoryDirty(EntityType entityType)
	{
		return getIndexActionsForCurrentTransaction().stream()
													 .anyMatch(indexAction -> indexAction.getEntityId() == null
															 && indexAction.getEntityTypeId()
																		   .equals(entityType.getId()));
	}

	@Override
	public boolean isRepositoryCompletelyClean(EntityType entityType)
	{
		return getIndexActionsForCurrentTransaction().stream()
													 .noneMatch(indexAction -> indexAction.getEntityTypeId()
																						  .equals(entityType.getId()));
	}

	@Override
	public Set<EntityKey> getDirtyEntities()
	{
		return getIndexActionsForCurrentTransaction().stream()
													 .filter(indexAction -> indexAction.getEntityId() != null)
													 .map(this::createEntityKey)
													 .collect(toSet());
	}

	@Override
	public Set<String> getEntirelyDirtyRepositories()
	{
		return getIndexActionsForCurrentTransaction().stream()
													 .filter(indexAction -> indexAction.getEntityId() == null)
													 .map(IndexAction::getEntityTypeId)
													 .collect(toSet());
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
		return EntityKey.create(indexAction.getEntityTypeId(),
				indexAction.getEntityId() != null ? EntityUtils.getTypedValue(indexAction.getEntityId(),
						dataService.getEntityType(indexAction.getEntityTypeId()).getIdAttribute()) : null);
	}

}
