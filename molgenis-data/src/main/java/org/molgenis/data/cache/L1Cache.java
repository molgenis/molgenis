package org.molgenis.data.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.molgenis.util.Pair;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class L1Cache extends DefaultMolgenisTransactionListener
{
	private static final Logger LOG = getLogger(L1Cache.class);
	private final ThreadLocal<Cache<String, Entity>> cache;

	@Autowired
	public L1Cache(MolgenisTransactionManager molgenisTransactionManager)
	{
		cache = new ThreadLocal<>();
		requireNonNull(molgenisTransactionManager).addTransactionListener(this);
	}

	@Override
	public void transactionStarted(String transactionId)
	{
		LOG.trace("Creating L1 cache for transaction [{}]", transactionId);
		cache.set(createCache());
	}

	@Override
	public void doCleanupAfterCompletion(String transactionId)
	{
		LOG.trace("Cleaning up L1 cache after transaction [{}]", transactionId);
		cache.remove();
	}

	/**
	 * Retrieves a dehydrated entity from the L1 cache based on a combination of entity name and entity id.
	 * returns a hydrated entity or null if there is no value present for the requested key
	 *
	 * @param entityName
	 * @param id
	 * @return
	 */
	public Entity cacheGet(String entityName, Object id)
	{
		Cache<String, Entity> entityCache = cache.get();
		String key = toKey(entityName, id);

		// TODO cache should return a dehydrated entity, make it go through hydration method
		Entity entity = entityCache.getIfPresent(key);
		if (entity != null) LOG.trace("Retrieving entity with id [{}] from L1 cache", key);
		else LOG.trace("Entity with id [{}] not present in L1 cache", key);

		return entity;
	}

	/**
	 * Puts a dehydrated entity into the L1 cache, with the key based on a combination of entity name and entity id.
	 *
	 * @param entityName
	 * @param entity
	 */
	public void cachePut(String entityName, Entity entity)
	{
		Cache<String, Entity> entityCache = cache.get();
		String key = toKey(entityName, entity.getIdValue());

		// TODO Entity should go through a dehydration method before being put in the cache
		entityCache.put(key, entity);
		LOG.trace("Add entity [{}] to L1 Cache", key);
	}

	// TODO Review this POC
	// TODO wipe xrefs/mrefs
	// TODO do not cache entity objects, but entity object values to avoid cache corruption on entity manipulation, see hydration/dehydration methods in this class
	// TODO not entityId but TransactionId
	public void cacheEvictAll(String entityName)
	{
		Cache<String, Entity> entityCache = cache.get();

		entityCache.invalidate();
		for (Iterator<Entry<String, Entity>> it = entityCache.asMap().entrySet().iterator(); it.hasNext(); )
			{
				Entry<String, Entity> entry = it.next();
				if (entry.getKey().startsWith(toKey(entityName, EMPTY)))
				{
					it.remove();
				}
			}
		}
	}

	public void cacheEvict(String entityName, Object entityId)
	{
		Cache<String, Entity> entityCache = cache.get();
		String key = toKey(entityName, entityId);
		entityCache.invalidate(key);
	}

	private Cache<String, Entity> createCache()
	{
		return CacheBuilder.newBuilder().maximumSize(1000).build();
	}

	private String toKey(String entityName, Object id)
	{
		return entityName + '/' + id.toString();
	}

	private String getTransactionId()
	{
		return (String) TransactionSynchronizationManager
				.getResource(MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME);
	}

	/**
	 * Rebuild entity from the values representing this entity.
	 *
	 * @param dehydratedEntity
	 * @return
	 */
	private Entity hydrateEntity(List<Pair<String, Object>> dehydratedEntity)
	{
		// TODO hydrate metadata
		// MapEntity entity = new MapEntity();
		dehydratedEntity.forEach(keyValue -> {
			// TODO hydrate value based on metadata types
		});
		return null;
	}

	/**
	 * Do not store entity in the cache since it might be updated by client code, instead store the values requires to
	 * rebuild this entity. For references to other entities only store the ids.
	 *
	 * @param entity
	 * @return
	 */
	private List<Pair<String, Object>> dehydrateEntity(Entity entity)
	{
		// TODO dehydrate metadata
		List<Pair<String, Object>> dehydratedEntity = new ArrayList<>();
		for (AttributeMetaData attribute : entity.getEntityMetaData().getAtomicAttributes())
		{
			String attributeName = attribute.getName();
			FieldTypeEnum attributeType = attribute.getDataType().getEnumType();

			Object value;
			switch (attributeType)
			{
				case CATEGORICAL:
				case XREF:
				case FILE:
					Entity refEntity = entity.getEntity(attributeName);
					value = refEntity != null ? refEntity.getIdValue() : null;
					break;
				case CATEGORICAL_MREF:
				case MREF:
					Iterator<Entity> refEntitiesIt = entity.getEntities(attributeName).iterator();
					if (refEntitiesIt.hasNext())
					{
						List<Object> mrefValues = new ArrayList<>();
						do
						{
							Entity mrefEntity = refEntitiesIt.next();
							mrefValues.add(mrefEntity != null ? mrefEntity.getIdValue() : null);
						}
						while (refEntitiesIt.hasNext());
						value = mrefValues;
					}
					else
					{
						value = emptyList();
					}
					break;
				case DATE:
					// Store timestamp since data is mutable
					Date dateValue = entity.getDate(attributeName);
					value = dateValue != null ? dateValue.getTime() : null;
					break;
				case DATE_TIME:
					// Store timestamp since data is mutable
					Timestamp dateTimeValue = entity.getTimestamp(attributeName);
					value = dateTimeValue != null ? dateTimeValue.getTime() : null;
					break;
				case BOOL:
				case COMPOUND:
				case DECIMAL:
				case EMAIL:
				case ENUM:
				case HTML:
				case HYPERLINK:
				case INT:
				case LONG:
				case SCRIPT:
				case STRING:
				case TEXT:
					value = entity.get(attributeName);
				default:
					throw new RuntimeException(format("Unknown attribute type [%s]", attributeType));
			}
			dehydratedEntity.add(new Pair<>(attributeName, value));
		}
		return dehydratedEntity;
	}
}
