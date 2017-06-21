package org.molgenis.data.index;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityKey;
import org.molgenis.data.Fetch;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionGroupFactory;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.GenericDependencyResolver;
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
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetaData.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetaData.IndexStatus.PENDING;
import static org.molgenis.data.meta.model.AttributeMetadata.*;
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
	private final GenericDependencyResolver genericDependencyResolver;

	@Autowired
	IndexActionRegisterServiceImpl(DataService dataService, IndexActionFactory indexActionFactory,
			IndexActionGroupFactory indexActionGroupFactory, GenericDependencyResolver genericDependencyResolver)
	{
		this.dataService = requireNonNull(dataService);
		this.indexActionFactory = requireNonNull(indexActionFactory);
		this.indexActionGroupFactory = requireNonNull(indexActionGroupFactory);
		this.genericDependencyResolver = requireNonNull(genericDependencyResolver);

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
		Set<IndexAction> indexActionSet = filterUnnecessaryIndexActions();
		List<IndexAction> indexActions1 = newArrayList(indexActionSet);
		for (int i = 0; i < indexActions1.size(); i++)
		{
			indexActions1.get(i).setActionOrder(i);
		}
		if (indexActions1.isEmpty())
		{
			return;
		}
		LOG.debug("Store index actions for transaction {}", transactionId);
		dataService
				.add(INDEX_ACTION_GROUP, indexActionGroupFactory.create(transactionId).setCount(indexActions1.size()));
		dataService.add(INDEX_ACTION, indexActions1.stream());
	}

	/**
	 * Filter all unnecessary index actions
	 *
	 * @return Set<IndexAction>
	 */
	private Set<IndexAction> filterUnnecessaryIndexActions()
	{
		// 1. add all referencing entities
		Set<IndexAction> allIndexAction = getIndexActionsForCurrentTransaction().stream()
				.flatMap(this::addReferencingEntities).collect(toSet());

		// 2. Filter excluded entities
		Set<IndexAction> indexActionWithoutExcluded = allIndexAction.stream()
				.filter(indexAction -> !excludedEntities.contains(indexAction.getEntityTypeId())).collect(toSet());

		// 3. Find all entities names of actions where no row is specified
		Set<String> entityFullIds = indexActionWithoutExcluded.stream()
				.filter(indexAction -> indexAction.getEntityId() == null).map(IndexAction::getEntityTypeId)
				.collect(toSet());

		// 4. Filter all row index actions from list
		return indexActionWithoutExcluded.stream()
				.filter(indexAction -> (indexAction.getEntityId() == null) || !entityFullIds
						.contains(indexAction.getEntityTypeId())).collect(toSet());
	}

	/**
	 * Add for all referencing entities an index action
	 *
	 * @return Stream<IndexAction>
	 */
	private Stream<IndexAction> addReferencingEntities(IndexAction indexAction)
	{
		EntityType entityType = dataService.getEntityType(indexAction.getEntityTypeId());
		if (entityType == null) // When entity is deleted the entityType cannot be retrieved
		{
			return Stream.of(indexAction);
		}

		// get referencing entity names
		Set<String> referencingEntityTypes = genericDependencyResolver
				.getAllDependants(indexAction.getEntityTypeId(), this::getDepth, this::getReferencingEntityTypes);

		LOG.debug("Referencing entities for entity type {}: {}", indexAction.getEntityTypeId(), referencingEntityTypes);

		// convert referencing entity names to index actions
		Stream<IndexAction> referencingEntityIndexActions = referencingEntityTypes.stream()
				.map(referencingEntity -> indexActionFactory.create().setEntityTypeId(referencingEntity)
						.setIndexActionGroup(indexAction.getIndexActionGroup()).setIndexStatus(PENDING));

		return Stream.concat(Stream.of(indexAction), referencingEntityIndexActions);
	}

	private int getDepth(String entityTypeId)
	{
		return dataService.getMeta().getEntityType(entityTypeId).getIndexingDepth();
	}

	private Set<String> getReferencingEntityTypes(String entityTypeId)
	{
		return dataService.query(ATTRIBUTE_META_DATA, Attribute.class).eq(REF_ENTITY_TYPE, entityTypeId)
				.fetch(new Fetch().field(ID)).findAll().map(Attribute::getEntity).map(EntityType::getId).
						collect(toSet());
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
