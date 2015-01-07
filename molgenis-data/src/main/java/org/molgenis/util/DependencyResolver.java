package org.molgenis.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DependencyResolver
{

	/**
	 * Determine the entity import order
	 * 
	 * @param coll
	 * @return
	 */
	public static List<EntityMetaData> resolve(Set<EntityMetaData> coll)
	{
		// EntityMetaData by entityname
		Map<String, EntityMetaData> metaDataByName = Maps.newHashMap();

		// All dependencies of EntityMetaData
		Map<String, Set<EntityMetaData>> dependenciesByName = Maps.newHashMap();

		for (EntityMetaData meta : coll)
		{
			metaDataByName.put(meta.getName(), meta);

			Set<EntityMetaData> dependencies = Sets.newHashSet();
			dependenciesByName.put(meta.getName(), dependencies);

			if (meta.getExtends() != null)
			{
				dependencies.add(meta.getExtends());
			}

			for (AttributeMetaData attr : meta.getAttributes())
			{
				if ((attr.getRefEntity() != null) && !attr.getRefEntity().equals(meta))// self reference
				{
					dependencies.add(attr.getRefEntity());
				}
			}
		}

		List<EntityMetaData> resolved = Lists.newArrayList();

		while (!dependenciesByName.isEmpty())
		{
			final List<String> ready = Lists.newArrayList();

			// Get all metadata without dependencies
			for (Entry<String, Set<EntityMetaData>> entry : dependenciesByName.entrySet())
			{
				if (entry.getValue().isEmpty())
				{
					String name = entry.getKey();
					ready.add(name);
					resolved.add(metaDataByName.get(name));
				}
			}

			// When there aren't any we got a non resolvable
			if (ready.isEmpty())
			{
				throw new MolgenisDataException("Could not resolve dependencies of entities "
						+ dependenciesByName.keySet() + " are there circular dependencies?");
			}

			// Remove found metadata from dependency graph
			Set<EntityMetaData> remove = Sets.newHashSet();
			for (String name : ready)
			{
				dependenciesByName.remove(name);
				remove.add(metaDataByName.get(name));
			}

			for (Set<EntityMetaData> dependencies : dependenciesByName.values())
			{
				dependencies.removeAll(remove);
			}

		}

		return resolved;
	}

	/**
	 * Determine the import order of entities that have a self reference
	 * 
	 * @param entities
	 * @param emd
	 * @return
	 */
	public static Iterable<Entity> resolveSelfReferences(Iterable<Entity> entities, EntityMetaData emd)
	{
		List<AttributeMetaData> selfRefAttributes = Lists.newArrayList();
		for (AttributeMetaData attr : emd.getAtomicAttributes())
		{
			if ((attr.getRefEntity() != null) && attr.getRefEntity().equals(emd))
			{
				selfRefAttributes.add(attr);
			}
		}

		if (selfRefAttributes.isEmpty())
		{
			// No self ref attributes
			return entities;
		}

		// You can't have a self reference if you provide id'ds. So we have id's

		Map<Object, Entity> entitiesById = Maps.newHashMap();

		// All self-references of an entity
		Map<Object, Set<Object>> dependenciesById = Maps.newHashMap();

		// Fill maps
		for (Entity entity : entities)
		{
			Object id = entity.getIdValue();
			if (id == null) throw new MolgenisDataException("Entity [" + emd.getName()
					+ "] contains an attribute that has a self reference but is missing an id.");

			entitiesById.put(id, entity);
			Set<Object> dependencies = Sets.newHashSet();
			dependenciesById.put(id, dependencies);
		}

		// Get the dependencies
		for (Entity entity : entities)
		{
			for (AttributeMetaData attr : selfRefAttributes)
			{
				Object id = entity.getIdValue();
				Entity ref = entity.getEntity(attr.getName());
				if (ref != null)
				{
					Object refId = ref.getIdValue();
					if (refId == null) throw new MolgenisDataException("Entity [" + emd.getName()
							+ "] contains an attribute that has a self reference but is missing an id.");
					if (!id.equals(refId))// Ref to the entity itself, should that be possible?
					{
						// If it is an unknown id it is already in the repository (or is missing, this is checked in the
						// validator)
						if (entitiesById.containsKey(refId))
						{
							dependenciesById.get(id).add(refId);
						}
					}
				}
			}
		}

		List<Entity> resolved = Lists.newArrayList();
		while (!dependenciesById.isEmpty())
		{
			final List<Object> ready = Lists.newArrayList();

			// Get all entities without dependencies
			for (Entry<Object, Set<Object>> entry : dependenciesById.entrySet())
			{
				if (entry.getValue().isEmpty())
				{
					Object id = entry.getKey();
					ready.add(id);
					resolved.add(entitiesById.get(id));
				}
			}

			// When there aren't any we got a non resolvable
			if (ready.isEmpty())
			{
				throw new MolgenisDataException("Could not resolve self references of entity " + emd.getName()
						+ " are there circular dependencies?");
			}

			// Remove found from dependency graph
			Set<Object> remove = Sets.newHashSet();
			for (Object id : ready)
			{
				dependenciesById.remove(id);
				remove.add(entitiesById.get(id).getIdValue());
			}

			for (Set<Object> dependencies : dependenciesById.values())
			{
				dependencies.removeAll(remove);
			}
		}

		return resolved;
	}

}
