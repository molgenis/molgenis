package org.molgenis.data.meta;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Tag;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.ApplicationContextProvider;

/**
 * Package defines the structure and attributes of a Package. Attributes are unique. Other software components can use
 * this to interact with Packages and/or to configure backends and frontends, including Repository instances.
 */
public class Package extends AbstractEntity
{
	private final Entity entity;

	public static final String DEFAULT_PACKAGE_NAME = "base";
	public static final String PACKAGE_SEPARATOR = "_";

	public static final Package defaultPackage = new Package(DEFAULT_PACKAGE_NAME, "The default package");

	public Package(Entity entity)
	{
		this.entity = requireNonNull(entity);
	}

	public Package(String simpleName)
	{
		this(simpleName, null);
	}

	public Package(String simpleName, String description)
	{
		this(simpleName, description, null);
	}

	public Package(String simpleName, String description, Package parent)
	{
		this.entity = new MapEntity(getEntityMetaData());
		String fullName;
		if (parent != null)
		{
			fullName = parent.getName() + PACKAGE_SEPARATOR + simpleName;
		}
		else
		{
			fullName = simpleName;
		}
		set(PackageMetaData.FULL_NAME, fullName);
		set(PackageMetaData.SIMPLE_NAME, simpleName);
		set(PackageMetaData.DESCRIPTION, description);
		set(PackageMetaData.PARENT, parent);
	}

	/**
	 * Copy-factory (instead of copy-constructor to avoid accidental method overloading to {@link #Package(Entity)})
	 *
	 * @param package_ package
	 * @return deep copy of package
	 */
	public static Package newInstance(Package package_)
	{
		Entity entityCopy = MapEntity.newInstance(package_.entity);
		return new Package(entityCopy);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return PackageMetaData.INSTANCE;
	}

	@Override
	public Object get(String attributeName)
	{
		return entity.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		entity.set(attributeName, value);
	}

	@Override
	public void set(Entity values)
	{
		entity.set(values);
	}

	/**
	 * Gets the name of the package without the trailing parent packages
	 *
	 * @return package name
	 */
	public String getSimpleName()
	{
		return getString(PackageMetaData.SIMPLE_NAME);
	}

	public Package setSimpleName(String simpleName)
	{
		set(PackageMetaData.SIMPLE_NAME, simpleName);
		return this;
	}

	/**
	 * Gets the parent package or null if this package does not have a parent package
	 *
	 * @return parent package or <tt>null</tt>
	 */
	public Package getParent()
	{
		return getEntity(PackageMetaData.PARENT, Package.class);
	}

	public Package setParent(Package parentPackage)
	{
		set(PackageMetaData.PARENT, parentPackage);
		return this;
	}

	/**
	 * Gets the fully qualified name of this package
	 *
	 * @return fully qualified package name
	 */
	public String getName()
	{
		return getString(PackageMetaData.FULL_NAME);
	}

	public Package setName(String fullName)
	{
		set(PackageMetaData.FULL_NAME, fullName);
		return this;
	}

	/**
	 * The description of this package
	 *
	 * @return package description or <tt>null</tt>
	 */
	public String getDescription()
	{
		return getString(PackageMetaData.DESCRIPTION);
	}

	public Package setDescription(String description)
	{
		set(PackageMetaData.DESCRIPTION, description);
		return this;
	}

	/**
	 * Gets the entities in this package. Does not return entities referred to by sub-packages
	 *
	 * @return package entities
	 */
	public Iterable<EntityMetaData> getEntityMetaDatas()
	{
		// TODO Use one-to-many relationship for EntityMetaData.package
		DataService dataService = ApplicationContextProvider.getApplicationContext().getBean(DataService.class);
		Query<EntityMetaData> query = dataService.query(EntityMetaDataMetaData.ENTITY_NAME, EntityMetaData.class)
				.eq(EntityMetaDataMetaData.PACKAGE, getName());
		return () -> query.findAll().iterator();
	}

	/**
	 * Gets the subpackages of this package or an empty list if this package doesn't have any subpackages.
	 *
	 * @return sub-packages
	 */
	public Iterable<Package> getSubPackages()
	{
		// TODO use one-to-many relationship for Package.parent
		DataService dataService = ApplicationContextProvider.getApplicationContext().getBean(DataService.class);
		Query<Package> query = dataService.query(PackageMetaData.ENTITY_NAME, Package.class)
				.eq(PackageMetaData.PARENT, this);
		return () -> query.findAll().iterator();
	}

	/**
	 * Get the root of this package, or itself if this is a root package
	 *
	 * @return root package of this package or <tt>null</tt>
	 */
	public Package getRootPackage()
	{
		Package package_ = this;
		while (package_.getParent() != null)
		{
			package_ = package_.getParent();
		}
		return package_;
	}

	/**
	 * Get all tags for this package
	 *
	 * @return package tags
	 */
	public Iterable<Tag<Package, LabeledResource, LabeledResource>> getTags()
	{
		// FIXME implement
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Package aPackage = (Package) o;

		return entity.equals(aPackage.entity);

	}

	@Override
	public int hashCode()
	{
		return entity.hashCode();
	}

	@Override
	public String toString()
	{
		return "Package{" +
				"entity=" + entity +
				'}';
	}
}
