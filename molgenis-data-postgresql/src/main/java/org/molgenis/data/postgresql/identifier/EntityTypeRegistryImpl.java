package org.molgenis.data.postgresql.identifier;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.postgresql.PostgreSqlNameGenerator;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.molgenis.data.transaction.TransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.postgresql.PostgreSqlNameGenerator.getJunctionTableName;
import static org.molgenis.data.transaction.TransactionManager.TRANSACTION_ID_RESOURCE_NAME;

/**
 * Tracks PostgreSQL table names.
 */
@Component
public class EntityTypeRegistryImpl extends DefaultMolgenisTransactionListener implements EntityTypeRegistry
{
	private final ConcurrentMap<String, EntityTypeDescription> entityTypeDescriptionMap;
	private final ConcurrentMap<String, Map<String, EntityTypeDescription>> transactionsEntityTypeDescriptionMap;

	public EntityTypeRegistryImpl(TransactionManager transactionManager)
	{
		entityTypeDescriptionMap = new ConcurrentHashMap<>();
		transactionsEntityTypeDescriptionMap = new ConcurrentHashMap<>();
		transactionManager.addTransactionListener(this);
	}

	@Override
	public void registerEntityType(EntityType entityType)
	{
		String tableName = getTableName(entityType);
		EntityTypeDescription entityTypeDescription = createEntityTypeDescription(entityType);
		getEntityTypeDescriptionMap().put(tableName, entityTypeDescription);
		putJunctionTableNames(entityType, entityTypeDescription);
	}

	@Override
	public void unregisterEntityType(EntityType entityType)
	{
		String tableName = getTableName(entityType);
		getEntityTypeDescriptionMap().put(tableName, null);
		putJunctionTableNames(entityType, null);
	}

	private void putJunctionTableNames(EntityType entityType, EntityTypeDescription entityTypeDescription)
	{
		StreamSupport.stream(entityType.getAllAttributes().spliterator(), false)
					 .filter(EntityTypeUtils::isReferenceType)
					 .map(attribute -> getJunctionTableName(entityType, attribute, false))
					 .forEach(junctionName -> getEntityTypeDescriptionMap().put(junctionName, entityTypeDescription));
	}

	@Override
	public EntityTypeDescription getEntityTypeDescription(String tableName)
	{
		Map<String, EntityTypeDescription> transactionEntityTypeDescriptionMap = transactionsEntityTypeDescriptionMap.get(
				getTransactionId());
		if (transactionEntityTypeDescriptionMap != null && transactionEntityTypeDescriptionMap.containsKey(tableName))
		{
			return transactionEntityTypeDescriptionMap.get(tableName);
		}
		return entityTypeDescriptionMap.get(tableName);
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

	private EntityTypeDescription createEntityTypeDescription(EntityType entityType)
	{
		String fullyQualifiedName = entityType.getId();
		ImmutableMap<String, AttributeDescription> attrDescriptionMap = ImmutableMap.copyOf(
				createAttributeDescriptionMap(entityType));
		return EntityTypeDescription.create(fullyQualifiedName, attrDescriptionMap);
	}

	private Map<String, AttributeDescription> createAttributeDescriptionMap(EntityType entityType)
	{
		return stream(entityType.getAllAttributes().spliterator(), false).collect(
				toMap(this::getColumnName, this::createAttributeDescription));
	}

	private AttributeDescription createAttributeDescription(Attribute attribute)
	{
		return AttributeDescription.create(attribute.getName());
	}

	private Map<String, EntityTypeDescription> getEntityTypeDescriptionMap()
	{
		return transactionsEntityTypeDescriptionMap.computeIfAbsent(getTransactionId(), k -> new HashMap<>());
	}
}
