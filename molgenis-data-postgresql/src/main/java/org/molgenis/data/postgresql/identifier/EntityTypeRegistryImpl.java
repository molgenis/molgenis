package org.molgenis.data.postgresql.identifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.postgresql.PostgreSqlNameGenerator;
import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.molgenis.data.transaction.TransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.postgresql.PostgreSqlNameGenerator.getJunctionTableName;
import static org.molgenis.data.support.EntityTypeUtils.isMultipleReferenceType;
import static org.molgenis.data.transaction.TransactionManager.TRANSACTION_ID_RESOURCE_NAME;

/**
 * Tracks PostgreSQL table names.
 */
@Component
public class EntityTypeRegistryImpl extends DefaultMolgenisTransactionListener implements EntityTypeRegistry
{
	/**
	 * Maps table name to {@link EntityTypeDescription} for the table name.
	 * Junction table names are mapped to the EntityTypeDescription of the entity type with the reference attribute.
	 */
	private final ConcurrentMap<String, EntityTypeDescription> entityTypeDescriptionMap;
	private final ConcurrentMap<String, Map<String, EntityTypeDescription>> transactionsEntityTypeDescriptionMap;

	EntityTypeRegistryImpl(TransactionManager transactionManager)
	{
		entityTypeDescriptionMap = new ConcurrentHashMap<>();
		transactionsEntityTypeDescriptionMap = new ConcurrentHashMap<>();
		transactionManager.addTransactionListener(this);
	}

	@Override
	public void registerEntityType(EntityType entityType)
	{
		Iterable<Attribute> attributes = entityType.getAtomicAttributes();
		EntityTypeDescription entityTypeDescription = createEntityTypeDescription(entityType, attributes);
		registerTableNames(getTableNames(entityType, stream(attributes)), entityTypeDescription);
	}

	@Override
	public void unregisterEntityType(EntityType entityType)
	{
		Iterable<Attribute> attributes = entityType.getAtomicAttributes();
		registerTableNames(getTableNames(entityType, stream(attributes)), null);
	}

	@Override
	public void addAttribute(EntityType entityType, Attribute attribute)
	{
		Iterable<Attribute> attributes = Iterables.concat(entityType.getAtomicAttributes(),
				ImmutableList.of(attribute));
		EntityTypeDescription entityTypeDescription = createEntityTypeDescription(entityType, attributes);
		registerTableNames(getTableNames(entityType, stream(attributes)), entityTypeDescription);
	}

	@Override
	public void updateAttribute(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		registerTableNames(getJunctionTableNames(entityType, Stream.of(attr)), null);
		List<Attribute> attributes = stream(entityType.getAtomicAttributes()).filter(
				existing -> !existing.getName().equals(attr.getName())).collect(Collectors.toList());
		attributes.add(updatedAttr);
		EntityTypeDescription entityTypeDescription = createEntityTypeDescription(entityType, attributes);
		registerTableNames(getTableNames(entityType, attributes.stream()), entityTypeDescription);
	}

	@Override
	public void deleteAttribute(EntityType entityType, Attribute attr)
	{
		registerTableNames(getJunctionTableNames(entityType, Stream.of(attr)), null);
		List<Attribute> attributes = stream(entityType.getAtomicAttributes()).filter(
				existing -> !existing.getName().equals(attr.getName())).collect(Collectors.toList());
		EntityTypeDescription entityTypeDescription = createEntityTypeDescription(entityType, attributes);
		registerTableNames(getTableNames(entityType, attributes.stream()), entityTypeDescription);
	}

	private Set<String> getTableNames(EntityType entityType, Stream<Attribute> attributes)
	{
		Set<String> tableNames = newHashSet(getTableName(entityType));
		tableNames.addAll(getJunctionTableNames(entityType, attributes));
		return tableNames;
	}

	private Set<String> getJunctionTableNames(EntityType entityType, Stream<Attribute> attributes)
	{
		return attributes.filter(EntityTypeRegistryImpl::hasJunctionTable)
						 .map(attribute -> getJunctionTableName(entityType, attribute, false))
						 .collect(toSet());
	}

	private EntityTypeDescription createEntityTypeDescription(EntityType entityType, Iterable<Attribute> attributes)
	{
		Map<String, AttributeDescription> attributeDescriptions = stream(attributes).filter(
				attribute -> !hasJunctionTable(attribute))
																					.collect(toMap(this::getColumnName,
																							attribute -> AttributeDescription
																									.create(attribute.getName())));
		return EntityTypeDescription.create(entityType.getId(), attributeDescriptions);
	}

	private void registerTableNames(Set<String> tableNames, EntityTypeDescription entityTypeDescription)
	{
		tableNames.forEach(tableName -> registerTableName(entityTypeDescription, tableName));
	}

	private void registerTableName(EntityTypeDescription entityTypeDescription, String tableName)
	{
		if (entityTypeDescription == null && getTransactionId() == null)
		{
			entityTypeDescriptionMap.remove(tableName);
		}
		else
		{
			getEntityTypeDescriptionMap().put(tableName, entityTypeDescription);
		}
	}

	private static boolean hasJunctionTable(Attribute attribute)
	{
		return isMultipleReferenceType(attribute) && attribute.getDataType() != AttributeType.ONE_TO_MANY;
	}

	@Override
	public EntityTypeDescription getEntityTypeDescription(String tableName)
	{
		return Optional.ofNullable(getTransactionId())
					   .filter(transactionsEntityTypeDescriptionMap::containsKey)
					   .map(transactionsEntityTypeDescriptionMap::get)
					   .filter(transactionMap -> transactionMap.containsKey(tableName))
					   .orElse(entityTypeDescriptionMap)
					   .get(tableName);
	}

	@Override
	public void afterCommitTransaction(String transactionId)
	{
		Map<String, EntityTypeDescription> transactionEntityTypeDescriptionMap = transactionsEntityTypeDescriptionMap.remove(
				transactionId);
		if (transactionEntityTypeDescriptionMap != null)
		{
			transactionEntityTypeDescriptionMap.forEach((tableName, entityTypeDescription) ->
			{
				if (entityTypeDescription != null)
				{
					entityTypeDescriptionMap.put(tableName, entityTypeDescription);
				}
				else
				{
					entityTypeDescriptionMap.remove(tableName);
				}
			});
		}
	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
		transactionsEntityTypeDescriptionMap.remove(transactionId);
	}

	private String getTransactionId()
	{
		return (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
	}

	private String getTableName(EntityType entityType)
	{
		return PostgreSqlNameGenerator.getTableName(entityType, false);
	}

	private String getColumnName(Attribute attr)
	{
		return PostgreSqlNameGenerator.getColumnName(attr, false);
	}

	private Map<String, EntityTypeDescription> getEntityTypeDescriptionMap()
	{
		String transactionId = getTransactionId();
		if (transactionId == null)
		{
			return entityTypeDescriptionMap;
		}
		return transactionsEntityTypeDescriptionMap.computeIfAbsent(transactionId, k -> new HashMap<>());
	}
}
