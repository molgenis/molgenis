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
import static org.molgenis.data.meta.model.AttributeMetadata.*;
import static org.molgenis.data.meta.model.EntityTypeMetadata.FULL_NAME;
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
		Set<IndexAction> indexActionSet = filterUnnecessaryIndexActions();
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
	 * Filter all unnecessary index actions
	 *
	 * @return
	 */
	Set<IndexAction> filterUnnecessaryIndexActions()
	{
		// 1. add all referencing entities
		Set<IndexAction> allIndexAction = getIndexActionsForCurrentTransaction().stream()
				.flatMap(this::addReferencingEntities).collect(toSet());

		// 2. Filter excluded entities
		Set<IndexAction> indexActionWithoutExcluded = allIndexAction.stream()
				.filter(indexAction -> !excludedEntities.contains(indexAction.getEntityFullName())).collect(toSet());

		// 3. Find all entities names of actions where no row is specified
		Set<String> entityFullNames = indexActionWithoutExcluded.stream()
				.filter(indexAction -> indexAction.getEntityId() == null).map(IndexAction::getEntityFullName)
				.collect(toSet());

		// 4. Filter all row index actions from list
		return indexActionWithoutExcluded.stream()
				.filter(indexAction -> (indexAction.getEntityId() == null) || !entityFullNames
						.contains(indexAction.getEntityFullName())).collect(toSet());
	}

	/**
	 * Add for all referencing entities an index action
	 *
	 * @param indexAction
	 * @return Stream<IndexAction>
	 */
	private Stream<IndexAction> addReferencingEntities(IndexAction indexAction)
	{
		if (indexAction.getEntityId() != null)
		{
			return Stream.of(indexAction);
		}

		EntityType entityType = dataService.getEntityType(indexAction.getEntityFullName());
		if (entityType == null) // When entity is deleted the entityType cannot be retrieved
		{
			return Stream.of(indexAction);
		}

		// get referencing entity names
		Set<String> referencingEntityNames = dataService.query(ATTRIBUTE_META_DATA, Attribute.class)
				.fetch(new Fetch().field(ID).field(ENTITY, new Fetch().field(FULL_NAME)))
				.eq(REF_ENTITY_TYPE, entityType).findAll().map(attr -> attr.getEntity().getName()).collect(toSet());

		// convert referencing entity names to index actions
		Stream<IndexAction> referencingEntityIndexActions = referencingEntityNames.stream()
				.map(referencingEntityName -> indexActionFactory.create().setEntityFullName(referencingEntityName)
						.setIndexActionGroup(indexAction.getIndexActionGroup()).setIndexStatus(PENDING));

		return Stream.concat(Stream.of(indexAction), referencingEntityIndexActions);
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
				indexAction -> indexAction.getEntityId() != null && indexAction.getEntityFullName()
						.equals(entityKey.getEntityName()) && indexAction.getEntityId()
						.equals(entityKey.getId().toString()));
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
				.map(indexAction -> createEntityKey(indexAction)).collect(toSet());
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

	/**
	 * Create an EntityKey
	 * Attention! MOLGENIS supports multiple id object types and the Entity id from the index registry s always a String
	 *
	 * @param indexAction
	 * @return EntityKey
	 */
	private EntityKey createEntityKey(IndexAction indexAction)
	{
		return EntityKey.create(indexAction.getEntityFullName(),
				indexAction.getEntityId() != null ? EntityUtils.getTypedValue(indexAction.getEntityId(),
						dataService.getEntityType(indexAction.getEntityFullName()).getIdAttribute()) : null);
	}

}
