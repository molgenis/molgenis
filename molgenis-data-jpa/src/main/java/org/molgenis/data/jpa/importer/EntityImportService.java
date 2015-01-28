package org.molgenis.data.jpa.importer;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Component
public class EntityImportService
{
	private static final long MAX_CACHE_ITEMS = 100000;
	private static int BATCH_SIZE = 1000;
	private DataService dataService;
	private LoadingCache<CacheKey, Iterable<Entity>> refLoadingCache;

	public EntityImportService()
	{

		refLoadingCache = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_ITEMS).softValues()
				.build(new CacheLoader<CacheKey, Iterable<Entity>>()
				{
					@Override
					public Iterable<Entity> load(CacheKey key) throws Exception
					{

						Iterable<Entity> result = dataService.findAll(key.getEnityName(),
								new QueryImpl().in(key.getAttrName(), key.getKeyValues()));

						return result;
					}
				});
	}

	@Autowired
	public void setDataService(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Transactional
	public int importEntity(String entityName, Repository source, DatabaseAction dbAction)
	{
		final Repository jpaRepository = dataService.getRepository(entityName);

		// Convert to MapEntity so we can be sure we can set xref/mref fields on it
		List<Entity> entitiesToImport = Lists.newArrayList();
		for (Entity entity : source)
		{
			entitiesToImport.add(new MapEntity(entity));
		}

		EntityMetaData entityMetaData = jpaRepository.getEntityMetaData();

		String updateKey = entityMetaData.getLabelAttribute().getName();
		List<Entity> batch = Lists.newArrayListWithCapacity(BATCH_SIZE);
		List<Entity> unresolved = Lists.newArrayList();
		Set<Object> labelAttributeValues = Sets.newHashSetWithExpectedSize(BATCH_SIZE);

		long rownr = 0;
		for (Entity entityToImport : entitiesToImport)
		{
			rownr++;

			// Skip empty rows
			if (EntityUtils.isEmpty(entityToImport)) break;

			Object key = entityToImport.get(updateKey);
			if (key == null)
			{
				ConstraintViolation violation = new ConstraintViolation("Missing key", null, entityToImport,
						entityMetaData.getLabelAttribute(), entityMetaData, rownr);
				throw new MolgenisValidationException(Sets.newHashSet(violation));
			}
			else if (labelAttributeValues.contains(key))
			{
				ConstraintViolation violation = new ConstraintViolation("Duplicate key '" + key + "'", key,
						entityToImport, entityMetaData.getLabelAttribute(), entityMetaData, rownr);
				throw new MolgenisValidationException(Sets.newHashSet(violation));
			}

			labelAttributeValues.add(key);

			boolean resolved = true;
			for (AttributeMetaData attr : entityMetaData.getAttributes())
			{
				if ((attr.getDataType().getEnumType() == MREF) || (attr.getDataType().getEnumType() == XREF)
						|| (attr.getDataType().getEnumType() == CATEGORICAL))
				{
					boolean attrResolved = resolveEntityRef(entityName, entityToImport, attr);
					resolved = resolved && attrResolved;

					// If the ref can not be resolved and it's a ref to another entity we can do nothing about it, we
					// can stop.
					if (!attrResolved && !attr.getRefEntity().getName().equalsIgnoreCase(entityName))
					{
						throw new MolgenisValidationException(Sets.newHashSet(createViolation(attr, entityMetaData,
								entityToImport, rownr)));
					}
				}
			}

			if (resolved)
			{
				batch.add(entityToImport);
			}
			else
			{
				unresolved.add(entityToImport);
			}

			if (batch.size() == BATCH_SIZE)
			{
				update(jpaRepository, batch, dbAction, updateKey);
				jpaRepository.flush();
				batch.clear();
			}
		}

		if (!batch.isEmpty())
		{
			update(jpaRepository, batch, dbAction, updateKey);
			jpaRepository.flush();
			batch.clear();
		}

		if (!unresolved.isEmpty())
		{
			// We got unresolved refs to the same entity, try the unresolved again max 100 times
			int iterations = 0;
			while (!unresolved.isEmpty() && (++iterations < 100))
			{
				ListIterator<Entity> it = unresolved.listIterator();
				while (it.hasNext())
				{
					Entity entityToImport = it.next();

					boolean resolved = true;
					for (AttributeMetaData attr : entityMetaData.getAttributes())
					{
						if (((attr.getDataType().getEnumType() == MREF) || (attr.getDataType().getEnumType() == XREF) || (attr
								.getDataType().getEnumType() == CATEGORICAL))
								&& attr.getRefEntity().getName().equalsIgnoreCase(entityName))
						{
							resolved = resolved && resolveEntityRef(entityName, entityToImport, attr);
						}
					}

					if (resolved)
					{
						// Add to the repository and remove from unresolved list
						it.remove();
						batch.add(entityToImport);
						update(jpaRepository, batch, dbAction, updateKey);
						jpaRepository.flush();
						batch.clear();
					}
				}

			}

		}

		if (!unresolved.isEmpty())
		{
			// We still got unresolved entities, create the ConstraintViolations and throw
			Set<ConstraintViolation> violations = Sets.newLinkedHashSetWithExpectedSize(unresolved.size());
			for (Entity entity : unresolved)
			{
				// Find the attribute that could not be resolved (we could have multiple ref attributes that point to
				// this entity)
				for (AttributeMetaData attr : entityMetaData.getAttributes())
				{
					if (((attr.getDataType().getEnumType() == MREF) || (attr.getDataType().getEnumType() == XREF) || (attr
							.getDataType().getEnumType() == CATEGORICAL))
							&& attr.getRefEntity().getName().equalsIgnoreCase(entityName)
							&& !resolveEntityRef(entityName, entity, attr))
					{
						long rowNr = getRowNr(entity, entitiesToImport, entityMetaData.getLabelAttribute().getName());
						violations.add(createViolation(attr, entityMetaData, entity, rowNr));
					}
				}

			}

			throw new MolgenisValidationException(violations);
		}

		if (!batch.isEmpty())
		{
			update(jpaRepository, batch, dbAction, updateKey);
			jpaRepository.flush();
		}

		return entitiesToImport.size();
	}

	public void update(Repository repo, List<? extends Entity> entities, DatabaseAction dbAction, String... keyNames)
	{
		if (keyNames.length == 0) throw new MolgenisDataException("At least one key must be provided, e.g. 'name'");

		// nothing todo?
		if (entities.size() == 0) return;

		// retrieve entity class and name
		String entityName = repo.getEntityMetaData().getEntityClass().getSimpleName();

		// create maps to store key values and entities
		// key is a concat of all key values for an entity
		Map<String, Entity> entityIndex = new LinkedHashMap<String, Entity>();
		// list of all keys, each list item a map of a (composite) key for one
		// entity e.g. investigation_name + name
		List<Map<String, Object>> keyIndex = new ArrayList<Map<String, Object>>();

		// select existing for update, only works if one (composit key allows
		// for nulls) the key values are set
		// otherwise skipped
		boolean keysMissing = false;
		for (Entity entity : entities)
		{
			// get all the value of all keys (composite key)
			// use an index to hash the entities
			StringBuilder combinedKeyBuilder = new StringBuilder();

			// extract its key values and put in map
			Map<String, Object> keyValues = new LinkedHashMap<String, Object>();
			boolean incompleteKey = true;

			// note: we can expect null values in composite keys but need at
			// least one key value.
			for (String key : keyNames)
			{
				// create a hash that concats all key values into one string
				combinedKeyBuilder.append(';');

				if (entity.get(key) != null)
				{
					combinedKeyBuilder.append(entity.get(key));
					incompleteKey = false;
					keyValues.put(key, entity.get(key));
				}
			}
			// check if we have missing key
			if (incompleteKey) keysMissing = true;

			// add the keys to the index, if exists
			if (!keysMissing)
			{
				keyIndex.add(keyValues);
				// create the entity index using the hash
				entityIndex.put(combinedKeyBuilder.toString(), entity);
			}
			else
			{
				if ((dbAction.equals(DatabaseAction.ADD) || dbAction.equals(DatabaseAction.ADD_UPDATE_EXISTING))
						&& keyNames.length == 1
						&& keyNames[0].equals(repo.getEntityMetaData().getIdAttribute().getName()))
				{
					// don't complain is 'id' field is emptyr
				}
				else
				{
					throw new MolgenisDataException("keys are missing: "
							+ repo.getEntityMetaData().getEntityClass().getSimpleName() + "." + Arrays.asList(keyNames));
				}
			}
		}

		// split lists in new and existing entities, but only if keys are set
		List<? extends Entity> newEntities = entities;
		List<Entity> existingEntities = new ArrayList<Entity>();
		if (!keysMissing && keyIndex.size() > 0)
		{
			newEntities = new ArrayList<Entity>();
			QueryImpl q = new QueryImpl();

			// in case of one field key, simply query
			if (keyNames.length == 1)
			{
				List<Object> values = new ArrayList<Object>();
				for (Map<String, Object> keyValues : keyIndex)
				{
					values.add(keyValues.get(keyNames[0]));
				}
				q.in(keyNames[0], values);
			}
			// in case of composite key make massive 'OR' query
			// form (key1 = x AND key2 = X) OR (key1=y AND key2=y)
			else
			{
				// very expensive!
				for (Map<String, Object> keyValues : keyIndex)
				{
					for (int i = 0; i < keyNames.length; i++)
					{
						if (i > 0) q.or();
						q.eq(keyNames[i], keyValues.get(keyNames[i]));
					}
				}
			}

			Iterable<Entity> selectForUpdate = repo.findAll(q);
			// separate existing from new entities
			for (Entity p : selectForUpdate)
			{
				// reconstruct composite key so we can use the entityIndex
				StringBuilder combinedKeyBuilder = new StringBuilder();
				for (String key : keyNames)
				{
					combinedKeyBuilder.append(';').append(p.get(key));
				}
				// copy existing from entityIndex to existingEntities
				entityIndex.remove(combinedKeyBuilder.toString());

				Entity e = new MapEntity(repo.getEntityMetaData().getIdAttribute().getName());
				e.set(p);

				existingEntities.add(e);
			}

			// copy remaining to newEntities
			newEntities = new ArrayList<Entity>(entityIndex.values());
		}

		// if existingEntities are going to be updated, they will need to
		// receive new values from 'entities' in addition to be mapped to the
		// database as is the case at this point
		if (existingEntities.size() > 0
				&& (dbAction == DatabaseAction.ADD_UPDATE_EXISTING || dbAction == DatabaseAction.UPDATE))
		{
			matchByNameAndUpdateFields(repo.getEntityMetaData(), existingEntities, entities);
		}

		switch (dbAction)
		{

		// will test for existing entities before add
		// (so only add if existingEntities.size == 0).
			case ADD:
				if (existingEntities.size() == 0)
				{
					repo.add(newEntities);
				}
				else
				{
					throw new MolgenisDataException("Tried to add existing "
							+ entityName
							+ " elements as new insert: "
							+ Arrays.asList(keyNames)
							+ "="
							+ existingEntities.subList(0, Math.min(5, existingEntities.size()))
							+ (existingEntities.size() > 5 ? " and " + (existingEntities.size() - 5) + "more" : ""
									+ existingEntities));
				}
				break;

			// will try to update(existingEntities) entities and
			// add(missingEntities)
			// so allows user to be sloppy in adding/updating
			case ADD_UPDATE_EXISTING:
				repo.add(newEntities);
				repo.update(existingEntities);
				break;

			// update while testing for newEntities.size == 0
			case UPDATE:
				if (newEntities.size() == 0)
				{
					repo.update(existingEntities);
				}
				else
				{
					throw new MolgenisDataException("Tried to update non-existing " + entityName + "elements "
							+ Arrays.asList(keyNames) + "=" + entityIndex.values());
				}
				break;

			// unexpected error
			default:
				throw new MolgenisDataException("updateByName failed because of unknown dbAction " + dbAction);
		}
	}

	private void matchByNameAndUpdateFields(EntityMetaData emd, List<? extends Entity> existingEntities,
			List<? extends Entity> entities)
	{
		// check if there are any label fields otherwise check impossible
		if (emd.getLabelAttribute() != null)
		{
			for (Entity entityInDb : existingEntities)
			{
				for (Entity newEntity : entities)
				{
					Object x1 = entityInDb.get(emd.getLabelAttribute().getName());
					Object x2 = newEntity.get(emd.getLabelAttribute().getName());

					if (x1.equals(x2))
					{
						try
						{
							MapEntity mapEntity = new MapEntity();
							for (String field : entityInDb.getAttributeNames())
							{
								mapEntity.set(field, newEntity.get(field));
							}
							entityInDb.set(mapEntity);
						}
						catch (Exception ex)
						{
							throw new MolgenisDataException(ex);
						}
					}
				}
			}
		}
	}

	private long getRowNr(Entity entityToFind, Iterable<Entity> entities, String keyAttr)
	{
		Object key = entityToFind.get(keyAttr);
		if (key == null)
		{
			return 0;
		}

		long rownr = 0;
		for (Entity entity : entities)
		{
			rownr++;
			if (key.equals(entity.get(keyAttr)))
			{
				return rownr;
			}
		}

		return 0;
	}

	private boolean resolveEntityRef(String entityName, Entity entityToBeImported, AttributeMetaData attr)
	{
		final List<Object> keyValues = Lists.newArrayList();
		final String foreignAttr = attr.getRefEntity().getLabelAttribute().getName();
		String key = attr.getName() + "_" + foreignAttr;

		if (attr.getDataType().getEnumType() == MREF)
		{
			List<String> keys = entityToBeImported.getList(key);
			if (keys != null)
			{
				keyValues.addAll(keys);
			}
		}
		else
		{
			Object keyValue = entityToBeImported.get(key);
			if (keyValue != null)
			{
				keyValues.add(keyValue);
			}
		}

		if (keyValues.isEmpty())
		{
			return true;
		}

		CacheKey cacheKey = new CacheKey(attr.getRefEntity().getName(), foreignAttr, keyValues);
		List<Entity> foundRefEntityList = null;

		try
		{
			foundRefEntityList = Lists.newArrayList(refLoadingCache.get(cacheKey));
			if (foundRefEntityList.isEmpty())
			{
				refLoadingCache.invalidate(cacheKey);
			}

			// Sort entity list the same as the original key list
			Collections.sort(foundRefEntityList, new Comparator<Entity>()
			{
				@Override
				public int compare(Entity o1, Entity o2)
				{
					Object value1 = o1.get(foreignAttr);
					Object value2 = o2.get(foreignAttr);
					Integer index1 = keyValues.indexOf(value1);
					Integer index2 = keyValues.indexOf(value2);

					return index1.compareTo(index2);
				}

			});
		}
		catch (ExecutionException e)
		{
			throw new MolgenisDataException(e);
		}

		if (!foundRefEntityList.isEmpty())
		{
			// Add the found ref entities
			if (attr.getDataType().getEnumType() == MREF)
			{
				@SuppressWarnings("unchecked")
				List<Entity> entityRefs = (List<Entity>) entityToBeImported.get(attr.getName());
				if (entityRefs == null)
				{
					entityRefs = Lists.newArrayList();
					entityToBeImported.set(attr.getName(), entityRefs);
				}
				entityRefs.addAll(foundRefEntityList);

				// Remove the found keys from the list
				for (Entity entityRef : entityRefs)
				{
					keyValues.remove(entityRef.getString(foreignAttr));
				}
				entityToBeImported.set(key, keyValues);
			}
			else
			{
				entityToBeImported.set(attr.getName(), foundRefEntityList.get(0));
				keyValues.remove(0);

				// Set found key to null
				entityToBeImported.set(key, null);
			}
		}

		return keyValues.isEmpty();
	}

	private ConstraintViolation createViolation(AttributeMetaData attr, EntityMetaData entityMetaData, Entity entity,
			long rownr)
	{
		String foreignAttr = attr.getRefEntity().getLabelAttribute().getName();
		String key = attr.getName() + "_" + foreignAttr;
		Object value = entity.get(key);

		String message = String
				.format("Could not resolve attribute '%s' with value '%s' of entity '%s'. This is a reference to entity '%s'.This happens when the key is missing in the referencing entity or when there are duplicate keys.",
						attr.getName(), value, entityMetaData.getName(), attr.getRefEntity().getName());

		return new ConstraintViolation(message, value, entity, attr, entityMetaData, rownr);
	}

	private class CacheKey
	{
		private final String enityName;
		private final String attrName;
		private final List<Object> keyValues;

		public CacheKey(String enityName, String attrName, List<Object> keyValues)
		{
			this.enityName = enityName;
			this.attrName = attrName;
			this.keyValues = keyValues;
		}

		public String getEnityName()
		{
			return enityName;
		}

		public String getAttrName()
		{
			return attrName;
		}

		public List<Object> getKeyValues()
		{
			return keyValues;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int hashCode = 1;
			hashCode = prime * hashCode + getOuterType().hashCode();
			hashCode = prime * hashCode + ((attrName == null) ? 0 : attrName.hashCode());
			hashCode = prime * hashCode + ((enityName == null) ? 0 : enityName.hashCode());
			hashCode = prime * hashCode + ((keyValues == null) ? 0 : keyValues.hashCode());

			return hashCode;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CacheKey other = (CacheKey) obj;
			if (!getOuterType().equals(other.getOuterType())) return false;
			if (attrName == null)
			{
				if (other.attrName != null) return false;
			}
			else if (!attrName.equals(other.attrName)) return false;
			if (enityName == null)
			{
				if (other.enityName != null) return false;
			}
			else if (!enityName.equals(other.enityName)) return false;
			if (keyValues == null)
			{
				if (other.keyValues != null) return false;
			}
			else if (!keyValues.equals(other.keyValues)) return false;
			return true;
		}

		private EntityImportService getOuterType()
		{
			return EntityImportService.this;
		}

	}
}
