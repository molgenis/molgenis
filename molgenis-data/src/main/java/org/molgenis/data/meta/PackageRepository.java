package org.molgenis.data.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Tag;
import org.molgenis.data.semantic.TagImpl;
import org.molgenis.util.DependencyResolver;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Helper class around the {@link PackageMetaData} repository. Caches the package metadata in {@link PackageImpl}s.
 * Internal implementation, use {@link MetaDataService} instead.
 */
class PackageRepository
{
	public static final PackageMetaData META_DATA = new PackageMetaData();

	final static Logger LOG = Logger.getLogger(PackageRepository.class);

	/**
	 * The repository where the package entities are stored.
	 */
	private final CrudRepository repository;

	/**
	 * In-memory cache of all packages, filled with entities and attributes.
	 */
	private Map<String, PackageImpl> packageCache = new HashMap<String, PackageImpl>();

	/**
	 * Creates a new PackageRepository.
	 * 
	 * @param coll
	 *            {@link ManageableCrudRepositoryCollection} that will be used to store the package entities.
	 */
	public PackageRepository(ManageableCrudRepositoryCollection coll)
	{
		repository = coll.add(META_DATA);
		updatePackageCache();
		addDefaultPackage();
	}

	/**
	 * Adds the default package to the repository if it does not yet exist.
	 */
	private void addDefaultPackage()
	{
		if (getPackage(Package.DEFAULT_PACKAGE_NAME) == null)
		{
			add(PackageImpl.defaultPackage);
		}
	}

	/**
	 * Fetches the package tree from the repository.
	 */
	void updatePackageCache()
	{
		packageCache = createPackageTree(repository);
	}

	/**
	 * Gets the {@link Package}s that have no parent.
	 * 
	 * @return {@link List} of root {@link Package}s.
	 */
	public List<Package> getRootPackages()
	{
		List<Package> result = new ArrayList<Package>();
		for (Package p : packageCache.values())
		{
			if (p.getParent() == null)
			{
				result.add(p);
			}
		}
		return result;
	}

	/**
	 * Gets a package.
	 * 
	 * @param name
	 *            fully qualified name of the pacakge
	 * @return the {@link Package} or null if not found
	 */
	public Package getPackage(String name)
	{
		return packageCache.get(name);
	}

	/**
	 * Clears the repository and empties the cache. Re-adds the default package.
	 */
	public void deleteAll()
	{
		List<Entity> importOrderPackages = Lists.newLinkedList(DependencyResolver.resolveSelfReferences(repository,
				META_DATA));
		Collections.reverse(importOrderPackages);
		for (Entity p : importOrderPackages)
		{
			repository.delete(p);
		}
		packageCache.clear();
		addDefaultPackage();
	}

	/**
	 * Adds a package including parent packages, in the right order.
	 * 
	 * @param p
	 *            the {@link Package} to add
	 */
	public void add(Package p)
	{
		PackageImpl parent = null;
		if (p.getParent() != null)
		{
			add(p.getParent());
			parent = packageCache.get(p.getParent().getName());
		}

		PackageImpl pImpl = new PackageImpl(p.getSimpleName(), p.getDescription(), parent);
		if (parent != null)
		{
			parent.addSubPackage(pImpl);
		}

		if (p.getTags() != null)
		{
			for (Tag<Package, LabeledResource, LabeledResource> tag : p.getTags())
			{
				pImpl.addTag(tag);
			}
		}

		Package existing = getPackage(p.getName());
		if (existing == null)
		{
			repository.add(pImpl.toEntity());
		}
		else
		{
			for (EntityMetaData emd : existing.getEntityMetaDatas())
			{
				if (!Iterables.contains(pImpl.getEntityMetaDatas(), emd))
				{
					pImpl.addEntity(emd);
				}
			}

			repository.update(pImpl.toEntity());
		}

		packageCache.put(p.getName(), pImpl);
	}

	/**
	 * Retrieves a {@link PackageMetaData} entity from the repository.
	 * 
	 * @param fullyQualifiedName
	 *            fully qualified name of the package
	 * @return Entity representing the {@link Package}
	 */
	Entity getEntity(String fullyQualifiedName)
	{
		return packageCache.get(fullyQualifiedName).toEntity();
	}

	/**
	 * Creates a map of {@link PackageImpl}s, reconstructing the package tree from a flat list of {@link Entity}. All
	 * {@link Package#getParent()}s have been set.
	 * 
	 * @param packageEntities
	 *            List of {@link Entity}s from a {@link PackageMetaData} repository.
	 * @return {@link Map} mapping the full names of the packages to {@link PackageImpl}s
	 */
	public static Map<String, PackageImpl> createPackageTree(Iterable<Entity> packageEntities)
	{
		Map<String, PackageImpl> result = new HashMap<String, PackageImpl>();

		List<Entity> entities = new ArrayList<Entity>();
		for (Entity entity : packageEntities)
		{
			entities.add(entity);
		}

		for (Entity entity : entities)
		{
			PackageImpl p = new PackageImpl(entity.getString(PackageMetaData.SIMPLE_NAME),
					entity.getString(PackageMetaData.DESCRIPTION));

			Iterable<Entity> tags = entity.getEntities(PackageMetaData.TAGS);
			if (tags != null)
			{
				for (Entity tagEntity : tags)
				{
					p.addTag(TagImpl.<Package> asTag(p, tagEntity));
				}
			}

			result.put(entity.getString(PackageMetaData.FULL_NAME), p);
		}

		for (Entity e : entities)
		{
			PackageImpl p = result.get(e.get(PackageMetaData.FULL_NAME));
			if (e.get(PackageMetaData.PARENT) != null)
			{
				PackageImpl parent = result.get(e.getEntity(PackageMetaData.PARENT)
						.getString(PackageMetaData.FULL_NAME));
				if (parent == null)
				{
					LOG.error("unknown parent package" + e.get(PackageMetaData.PARENT));
					throw new IllegalStateException("Unknown parent package" + e.get(PackageMetaData.PARENT));
				}
				else
				{
					p.setParent(parent);
					parent.addSubPackage(p);
				}
			}
		}
		return result;
	}
}
