package org.molgenis.data.index;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Multimaps.synchronizedSetMultimap;
import static com.google.common.collect.Streams.mapWithIndex;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.index.Impact.createSingleEntityImpact;
import static org.molgenis.data.index.IndexDependencyModel.ENTITY_TYPE_FETCH;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetaData.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetaData.IndexStatus.PENDING;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetadata.REF_ENTITY_TYPE;
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

	private final SetMultimap<String, Impact> changesPerTransaction = synchronizedSetMultimap(HashMultimap.create());

	private final DataService dataService;
	private final IndexActionFactory indexActionFactory;
	private final IndexActionGroupFactory indexActionGroupFactory;
	private final IndexingStrategy indexingStrategy;

	private final Set<String> excludedEntities = Sets.newConcurrentHashSet();

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
	public synchronized void register(EntityType entityType, Object entityId)
	{
		String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
		if (transactionId != null)
		{
			Impact impact = createSingleEntityImpact(entityType.getId(), entityId);
			LOG.debug("register({})", impact);

			final boolean newlyRegistered = changesPerTransaction.put(transactionId, impact);
			if (newlyRegistered && LOG.isWarnEnabled())
			{
				final int size = changesPerTransaction.get(transactionId).size();
				if (size >= LOG_EVERY && size % LOG_EVERY == 0)
				{
					LOG.warn(
							"Transaction {} has caused {} IndexActions to be created. Consider streaming your data manipulations.",
							transactionId, size);
				}
			}
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
		Set<Impact> changes = getChangesForCurrentTransaction();
		if (changes.isEmpty())
		{
			return;
		}
		if (changes.stream().allMatch(impact -> excludedEntities.contains(impact.getEntityTypeId())))
		{
			return;
		}

		IndexActionGroup indexActionGroup = indexActionGroupFactory.create(transactionId);
		IndexDependencyModel dependencyModel = createIndexDependencyModel(changes);
		Stream<Impact> impactStream = indexingStrategy.determineImpact(changes, dependencyModel)
													  .stream()
													  .filter(key -> !excludedEntities.contains(key.getEntityTypeId()));
		List<IndexAction> indexActions = mapWithIndex(impactStream,
				(key, actionOrder) -> createIndexAction(indexActionGroup, key, (int) actionOrder)).collect(toList());
		if (indexActions.isEmpty())
		{
			return;
		}
		LOG.debug("Store index actions for transaction {}", transactionId);
		dataService.add(INDEX_ACTION_GROUP,
				indexActionGroupFactory.create(transactionId).setCount(indexActions.size()));
		dataService.add(INDEX_ACTION, indexActions.stream());
	}

	private IndexDependencyModel createIndexDependencyModel(Set<Impact> changes)
	{
		Set<String> entityTypeIds = changes.stream().map(Impact::getEntityTypeId).collect(toSet());
		boolean hasReferences = dataService.query(ATTRIBUTE_META_DATA).in(REF_ENTITY_TYPE, entityTypeIds).count() > 0;
		if (hasReferences)
		{
			return new IndexDependencyModel(getEntityTypes());
		}
		else
		{
			return new IndexDependencyModel(emptyList());
		}
	}

	private IndexAction createIndexAction(IndexActionGroup indexActionGroup, Impact key, int actionOrder)
	{
		IndexAction indexAction = indexActionFactory.create();
		indexAction.setIndexStatus(PENDING);
		if (key.getId() != null)
		{
			indexAction.setEntityId(key.getId().toString());
		}
		indexAction.setEntityTypeId(key.getEntityTypeId());
		indexAction.setIndexActionGroup(indexActionGroup);
		indexAction.setActionOrder(actionOrder);
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
		return !changesPerTransaction.removeAll(transactionId)
									 .stream()
									 .map(Impact::getEntityTypeId)
									 .allMatch(excludedEntities::contains);
	}

	private Set<Impact> getChangesForCurrentTransaction()
	{
		String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
		return Optional.of(changesPerTransaction.get(transactionId)).orElse(emptySet());
	}

	/* TransactionInformation implementation */

	@Override
	public boolean isEntityDirty(EntityKey entityKey)
	{
		return getChangesForCurrentTransaction().stream()
												.filter(Impact::isSingleEntity)
												.map(Impact::toEntityKey)
												.anyMatch(entityKey::equals);
	}

	@Override
	public boolean isEntireRepositoryDirty(EntityType entityType)
	{
		return getChangesForCurrentTransaction().stream()
												.filter(Impact::isWholeRepository)
												.map(Impact::getEntityTypeId)
												.anyMatch(entityType.getId()::equals);
	}

	@Override
	public boolean isRepositoryCompletelyClean(EntityType entityType)
	{
		return !getDirtyRepositories().contains(entityType.getId());
	}

	@Override
	public Set<EntityKey> getDirtyEntities()
	{
		return getChangesForCurrentTransaction().stream()
												.filter(Impact::isSingleEntity)
												.map(Impact::toEntityKey)
												.collect(toSet());
	}

	@Override
	public Set<String> getEntirelyDirtyRepositories()
	{
		return getChangesForCurrentTransaction().stream()
												.filter(Impact::isWholeRepository)
												.map(Impact::getEntityTypeId)
												.collect(toSet());
	}

	@Override
	public Set<String> getDirtyRepositories()
	{
		return getChangesForCurrentTransaction().stream().map(Impact::getEntityTypeId).collect(toSet());
	}
}
