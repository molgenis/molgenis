package org.molgenis.data.postgresql.identifier;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.postgresql.PostgreSqlNameGenerator;
import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.molgenis.data.transaction.TransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.stream.Collectors.toMap;
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
	public void registerEntityType(String entityTypeId, List<Identifiable> referenceTypeAttributes)
	{
		String tableName = getTableName(entityTypeId);
		EntityTypeDescription entityTypeDescription = createEntityTypeDescription(entityTypeId,
				referenceTypeAttributes);
		getEntityTypeDescriptionMap().put(tableName, entityTypeDescription);
		putJunctionTableNames(entityTypeId, referenceTypeAttributes, entityTypeDescription);
	}

	@Override
	public void unregisterEntityType(String entityTypeId, List<Identifiable> referenceTypeAttributes)
	{
		String tableName = getTableName(entityTypeId);
		getEntityTypeDescriptionMap().put(tableName, null);
		putJunctionTableNames(entityTypeId, referenceTypeAttributes, null);
	}

	private void putJunctionTableNames(String entityTypeId, List<Identifiable> referenceTypeAttributes,
			EntityTypeDescription entityTypeDescription)
	{
		referenceTypeAttributes.stream()
							   .map(attribute -> getJunctionTableName(entityTypeId, attribute, false))
							   .forEach(junctionName -> getEntityTypeDescriptionMap().put(junctionName,
									   entityTypeDescription));
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

	private String getTableName(String entityTypeId)
	{
		return PostgreSqlNameGenerator.getTableName(entityTypeId, false);
	}

	private String getColumnName(Identifiable attribute)
	{
		return PostgreSqlNameGenerator.getColumnName(attribute, false);
	}

	private EntityTypeDescription createEntityTypeDescription(String entityTypeId, List<Identifiable> attributes)
	{
		ImmutableMap<String, AttributeDescription> attrDescriptionMap = ImmutableMap.copyOf(
				createAttributeDescriptionMap(attributes));
		return EntityTypeDescription.create(entityTypeId, attrDescriptionMap);
	}

	private Map<String, AttributeDescription> createAttributeDescriptionMap(List<Identifiable> attributes)
	{
		return attributes.stream().collect(toMap(this::getColumnName, this::createAttributeDescription));
	}

	private AttributeDescription createAttributeDescription(Identifiable attribute)
	{
		return AttributeDescription.create(attribute.getName());
	}

	private Map<String, EntityTypeDescription> getEntityTypeDescriptionMap()
	{
		return transactionsEntityTypeDescriptionMap.computeIfAbsent(getTransactionId(), k -> new HashMap<>());
	}
}
