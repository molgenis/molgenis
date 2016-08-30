package org.molgenis.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.MolgenisFieldTypes.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.support.EntityMetaDataUtils.isSingleReferenceType;

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
		Map<String, EntityMetaData> metaDataByName = newHashMap();

		// All dependencies of EntityMetaData
		Map<String, Set<String>> dependenciesByName = newHashMap();

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
				if (attr.getDataType() != ONE_TO_MANY && (attr.getRefEntity() != null) && !attr.getRefEntity().getName()
						.equals(meta.getName()))
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
				// accept the cyclic dependency between entity meta <--> attribute meta which is dealt with during
				// bootstrapping, see SystemEntityMetaDataPersister.
				if (dependenciesByName.containsKey(ENTITY_META_DATA) && dependenciesByName
						.containsKey(ATTRIBUTE_META_DATA))
				{
					ready.add(ENTITY_META_DATA);
					ready.add(ATTRIBUTE_META_DATA);
					resolved.add(metaDataByName.get(ENTITY_META_DATA));
					resolved.add(metaDataByName.get(ATTRIBUTE_META_DATA));
				}
				else
				{
					throw new MolgenisDataException(
							"Could not resolve dependencies of entities " + dependenciesByName.keySet()
									+ " are there circular dependencies?");
				}
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
			if (attr.getRefEntity() != null)
			{
				if (EntityUtils.equals(attr.getRefEntity(), emd))
				{
					return true;
				}
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
		Map<Object, Entity> entitiesById = newHashMap();

		// All self-references of an entity
		Map<Object, Set<Object>> dependenciesById = newHashMap();

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

				if (isSingleReferenceType(attr))
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
			packageMap.forEach((name, package_) ->
			{
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
