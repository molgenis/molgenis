package org.molgenis.data.meta;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.removeAll;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.PackageMetaData.DESCRIPTION;
import static org.molgenis.data.meta.PackageMetaData.ENTITY_NAME;
import static org.molgenis.data.meta.PackageMetaData.FULL_NAME;
import static org.molgenis.data.meta.PackageMetaData.INSTANCE;
import static org.molgenis.data.meta.PackageMetaData.PARENT;
import static org.molgenis.data.meta.PackageMetaData.SIMPLE_NAME;
import static org.molgenis.data.meta.PackageMetaData.TAGS;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
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
		set(FULL_NAME, fullName);
		set(SIMPLE_NAME, simpleName);
		set(DESCRIPTION, description);
		set(PARENT, parent);
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
		return INSTANCE;
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
		return getString(SIMPLE_NAME);
	}

	public Package setSimpleName(String simpleName)
	{
		set(SIMPLE_NAME, simpleName);
		return this;
	}

	/**
	 * Gets the parent package or null if this package does not have a parent package
	 *
	 * @return parent package or <tt>null</tt>
	 */
	public Package getParent()
	{
		return getEntity(PARENT, Package.class);
	}

	public Package setParent(Package parentPackage)
	{
		set(PARENT, parentPackage);
		return this;
	}

	/**
	 * Gets the fully qualified name of this package
	 *
	 * @return fully qualified package name
	 */
	public String getName()
	{
		return getString(FULL_NAME);
	}

	public Package setName(String fullName)
	{
		set(FULL_NAME, fullName);
		return this;
	}

	/**
	 * The description of this package
	 *
	 * @return package description or <tt>null</tt>
	 */
	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public Package setDescription(String description)
	{
		set(DESCRIPTION, description);
		return this;
	}

	/**
	 * Get all tags for this package
	 *
	 * @return package tags
	 */
	public Iterable<Tag> getTags()
	{
		return getEntities(TAGS, Tag.class);
	}

	/**
	 * Set tags for this package
	 *
	 * @param tags package tags
	 * @return this package
	 */
	public Package setTags(Iterable<Tag> tags)
	{
		set(TAGS, tags);
		return this;
	}

	/**
	 * Add a tag for this package
	 *
	 * @param tag package tag
	 */
	public void addTag(Tag tag)
	{
		entity.set(TAGS, concat(getTags(), singletonList(tag)));
	}

	/**
	 * Add a tag for this package
	 *
	 * @param tag package tag
	 */
	public void removeTag(Tag tag)
	{
		Iterable<Tag> tags = getTags();
		removeAll(tags, singletonList(tag));
		entity.set(TAGS, tag);
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
		Query<Package> query = dataService.query(ENTITY_NAME, Package.class)
				.eq(PARENT, this);
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
