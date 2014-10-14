package org.molgenis.data.mysql.meta;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.Package;
import org.molgenis.data.Query;
import org.molgenis.data.mysql.MysqlRepositoryCollection;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Repository to add and retrieve Package entities.
 */
class MysqlPackageRepository
{
	public static final PackageMetaData META_DATA = new PackageMetaData();

	private CrudRepository repository;

	public MysqlPackageRepository(MysqlRepositoryCollection repositoryCollection)
	{
		repository = repositoryCollection.add(META_DATA);
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
		TreeSet<String> names = new TreeSet<String>(Collections.reverseOrder());
		for (Entity p : repository)
		{
			names.add(p.getString(META_DATA.FULL_NAME));
		}
		for (String name : names)
		{
			repository.deleteById(name);
		}
	}

	/**
	 * Adds all packages including parent packages, in the right order.
	 * 
	 * @param emd
	 */
	public void add(Package p)
	{
		// add packages
		List<Package> packages = Lists.newArrayList();
		while (p != null)
		{
			packages.add(p);
			p = p.getParent();
		}

		Collections.reverse(packages);
		for (Package pack : packages)
		{
			if (getPackage(pack.getName()) == null)
			{
				repository.add(new PackageImpl(pack));
			}
		}
	}
}
