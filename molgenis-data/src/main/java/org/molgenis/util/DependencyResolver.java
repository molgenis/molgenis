package org.molgenis.util;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.Package;
import org.molgenis.fieldtypes.XrefField;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DependencyResolver
{
	@Autowired
	private DataService dataService;

	/**
	 * Determine the entity import order
	 *
	 * @param repos
	 * @return
	 */
	public static List<Repository<Entity>> resolve(Iterable<Repository<Entity>> repos)
	{
		Map<String, Repository<Entity>> repoByName = new HashMap<>();
		for (Repository<Entity> repo : repos)
		{
			repoByName.put(repo.getEntityMetaData().getName(), repo);
		}

		return resolve(repoByName.values().stream().map(repo -> repo.getEntityMetaData()).collect(Collectors.toSet()))
				.stream().map(emd -> repoByName.get(emd.getName())).collect(Collectors.toList());
	}

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
		Map<String, Set<String>> dependenciesByName = Maps.newHashMap();

		for (EntityMetaData meta : coll)
		{
			metaDataByName.put(meta.getName(), meta);

			Set<String> dependencies = Sets.newHashSet();
			dependenciesByName.put(meta.getName(), dependencies);

			if (meta.getExtends() != null)
			{
				dependencies.add(meta.getExtends().getName());
			}

			for (AttributeMetaData attr : meta.getAtomicAttributes())
			{
				if ((attr.getRefEntity() != null) && !attr.getRefEntity().getName()
						.equals(meta.getName()))// self reference
				{
					dependencies.add(attr.getRefEntity().getName());
				}
			}
		}

		List<EntityMetaData> resolved = Lists.newArrayList();

		while (!dependenciesByName.isEmpty())
		{
			final List<String> ready = Lists.newArrayList();

			// Get all metadata without dependencies
			for (Entry<String, Set<String>> entry : dependenciesByName.entrySet())
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
				// FIXME dependency resolving fails if input is missing dependencies
				return Lists.newArrayList(coll);
				//				throw new MolgenisDataException(
				//						"Could not resolve dependencies of entities " + dependenciesByName.keySet()
				//								+ " are there circular dependencies?");
			}

			// Remove found metadata from dependency graph
			Set<String> remove = Sets.newHashSet();
			for (String name : ready)
			{
				dependenciesByName.remove(name);
				remove.add(name);
			}

			for (Set<String> dependencies : dependenciesByName.values())
			{
				dependencies.removeAll(remove);
			}

		}

		return resolved;
	}

	public static boolean hasSelfReferences(EntityMetaData emd)
	{
		for (AttributeMetaData attr : emd.getAtomicAttributes())
		{
			if ((attr.getRefEntity() != null) && attr.getRefEntity().equals(emd))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Determine the import order of entities that have a self reference
	 *
	 * @param entities
	 * @param emd
	 * @return
	 */
	public Iterable<Entity> resolveSelfReferences(Iterable<Entity> entities, EntityMetaData emd)
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
				List<Entity> refs = Lists.newArrayList();

				if (attr.getDataType() instanceof XrefField)
				{
					Entity ref = entity.getEntity(attr.getName());
					if (ref != null) refs.add(ref);
				}
				else
				{
					// mrefs
					Iterable<Entity> it = entity.getEntities(attr.getName());
					if (it != null) Iterables.addAll(refs, it);
				}

				Object id = entity.getIdValue();
				for (Entity ref : refs)
				{
					Object refId = ref.getIdValue();
					if (refId == null) throw new MolgenisDataException("Entity [" + emd.getName()
							+ "] contains an attribute that has a self reference but is missing an id.");
					if (!id.equals(refId))// Ref to the entity itself, should that be possible?
					{
						// If it is an unknown id it is already in the repository or missing
						if (entitiesById.containsKey(refId))
						{
							dependenciesById.get(id).add(refId);
						}
						else
						{
							Entity refEntity = dataService
									.getRepository(emd.getAttribute(attr.getName()).getRefEntity().getName())
									.findOneById(refId);
							if (refEntity == null)
							{
								throw new UnknownEntityException(
										attr.getRefEntity().getName() + " with " + attr.getRefEntity().getIdAttribute()
												.getName() + " [" + refId + "] does not exist");
							}
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

	/**
	 * Resolved package dependencies in database insertion order
	 *
	 * @param packageStream
	 * @return package dependencies in database insertion order
	 */
	public static Stream<Package> resolve(Stream<Package> packageStream)
	{
		Map<String, Package> packageMap = packageStream.collect(toMap(Package::getName, identity()));
		if (packageMap.isEmpty())
		{
			return Stream.empty();
		}

		Map<String, Package> resolvedPackages = new LinkedHashMap<>();
		while (resolvedPackages.size() < packageMap.size())
		{
			AtomicInteger nrResolvedPackages = new AtomicInteger(0);
			packageMap.forEach((name, package_) -> {
				if (package_.getParent() == null || resolvedPackages.containsKey(package_.getParent().getName()))
				{
					resolvedPackages.put(name, package_);
					nrResolvedPackages.incrementAndGet();
				}
			});
			if (nrResolvedPackages.get() == 0)
			{
				throw new RuntimeException("Unable to resolve packages, circular dependency?");
			}
		}
		return resolvedPackages.values().stream();
	}
}
