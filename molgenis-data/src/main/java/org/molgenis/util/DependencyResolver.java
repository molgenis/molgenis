package org.molgenis.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DependencyResolver
{

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
			for (String name : dependenciesByName.keySet())
			{
				if (dependenciesByName.get(name).isEmpty())
				{
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
}
