package org.molgenis.data.meta;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.Package;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCreator;
import org.molgenis.util.DependencyResolver;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Repository to add and retrieve Package entities.
 */
class PackageRepository
{
	public static final PackageMetaData META_DATA = new PackageMetaData();

	private CrudRepository repository;

	public PackageRepository(RepositoryCreator repositoryCreator)
	{
		repository = repositoryCreator.create(META_DATA);
		addDefaultPackage();
	}

	public void addDefaultPackage()
	{
		add(new PackageImpl(Package.DEFAULT_PACKAGE_NAME, "The default package."));
	}

	public Iterable<Package> getPackages()
	{
		Set<Package> result = new TreeSet<Package>();
		// iterate over self
		for (Entity entity : repository)
		{
			result.add(new PackageImpl(entity));
		}
		return result;
	}

	public Package getPackage(String name)
	{
		Entity entity = repository.findOne(name);
		if (entity == null)
		{
			return null;
		}
		return new PackageImpl(entity);
	}

	public Iterable<Package> getSubPackages(Package p)
	{
		return findPackages(repository.query().eq(PackageMetaData.PARENT, p));
	}

	protected Iterable<Package> findPackages(Query q)
	{
		return Iterables.transform(repository.findAll(q), new Function<Entity, Package>()
		{
			@Override
			public Package apply(Entity entity)
			{
				return new PackageImpl(entity);
			}
		});
	}

	public void deleteAll()
	{
		List<Entity> importOrderPackages = Lists.newLinkedList(DependencyResolver.resolveSelfReferences(repository,
				META_DATA));
		Collections.reverse(importOrderPackages);
		for (Entity p : importOrderPackages)
		{
			repository.delete(p);
		}
	}

	/**
	 * Adds a package including parent packages, in the right order.
	 * 
	 * @param p
	 *            the {@link Package} to add
	 */
	public void add(Package p)
	{
		if (p != null)
		{
			add(p.getParent());
			if (getPackage(p.getName()) == null)
			{
				repository.add(new PackageImpl(p));
			}
		}
	}
}
