package org.molgenis.data.mysql.meta;

import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.molgenis.data.Entity;
import org.molgenis.data.Package;
import org.molgenis.data.Query;
import org.molgenis.data.mysql.MysqlRepository;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Repository to add and retrieve Package entities.
 */
class MysqlPackageRepository extends MysqlRepository
{
	public static final PackageMetaData META_DATA = new PackageMetaData();

	public MysqlPackageRepository(DataSource dataSource)
	{
		super(dataSource);
		setMetaData(META_DATA);
	}

	@Override
	public void create()
	{
		super.create();
		addDefaultPackage();
	}

	public void addDefaultPackage()
	{
		addPackage(new PackageImpl(Package.DEFAULT_PACKAGE_NAME, "The default package."));
	}

	public Iterable<Package> getPackages()
	{
		Set<Package> result = new TreeSet<Package>();
		// iterate over self
		for (Entity entity : this)
		{
			result.add(new PackageImpl(entity));
		}
		return result;
	}

	public Package getPackage(String name)
	{
		Entity entity = findOne(name);
		if (entity == null)
		{
			return null;
		}
		return new PackageImpl(entity);
	}

	/**
	 * Inserts a new package into the repository
	 * 
	 * @param p
	 *            the Package to insert
	 */
	public void addPackage(Package p)
	{
		add(new PackageImpl(p));
	}

	public Iterable<Package> getSubPackages(Package p)
	{
		return findPackages(query().eq(PackageMetaData.PARENT, p));
	}

	protected Iterable<Package> findPackages(Query q)
	{
		return Iterables.transform(findAll(q), new Function<Entity, Package>()
		{
			@Override
			public Package apply(Entity entity)
			{
				return new PackageImpl(entity);
			}
		});
	}
}
