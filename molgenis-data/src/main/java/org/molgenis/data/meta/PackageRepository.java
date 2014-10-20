package org.molgenis.data.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.Package;
import org.molgenis.util.DependencyResolver;

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
	private CrudRepository repository;

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
		System.out.println(importOrderPackages);
		for (Entity p : importOrderPackages)
		{
			System.out.println(p);
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
		if (packageCache.containsKey(p.getName()))
		{
			return;
		}
		PackageImpl parent = null;
		if (p.getParent() != null)
		{
			add(p.getParent());
			parent = packageCache.get(p.getParent().getName());
		}
		if (getPackage(p.getName()) == null)
		{
			PackageImpl pImpl = new PackageImpl(p.getSimpleName(), p.getDescription(), parent);
			repository.add(pImpl.toEntity());
			packageCache.put(p.getName(), pImpl);
		}
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
