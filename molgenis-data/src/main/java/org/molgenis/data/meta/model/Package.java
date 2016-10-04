package org.molgenis.data.meta.model;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.util.ApplicationContextProvider;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.removeAll;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;

/**
 * Package defines the structure and attributes of a Package. Attributes are unique. Other software components can use
 * this to interact with Packages and/or to configure backends and frontends, including Repository instances.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class Package extends StaticEntity
{
	public static final String PACKAGE_SEPARATOR = "_";

	public Package(Entity entity)
	{
		super(entity);
	}

	/**
	 * Constructs a package with the given meta data
	 *
	 * @param entityMeta package meta data
	 */
	public Package(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	/**
	 * Constructs a package with the given type code and meta data
	 *
	 * @param packageId  package identifier (fully qualified package name)
	 * @param entityMeta language meta data
	 */
	public Package(String packageId, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setSimpleName(packageId);
	}

	/**
	 * Copy-factory (instead of copy-constructor to avoid accidental method overloading to {@link #Package(EntityMetaData)})
	 *
	 * @param package_ package
	 * @return deep copy of package
	 */
	public static Package newInstance(Package package_)
	{
		Package packageCopy = new Package(package_.getEntityMetaData());
		packageCopy.setName(package_.getName());
		packageCopy.setSimpleName(package_.getSimpleName());
		packageCopy.setLabel(package_.getLabel());
		packageCopy.setDescription(package_.getDescription());
		Package parent = package_.getParent();
		packageCopy.setParent(parent != null ? Package.newInstance(parent) : null);
		Iterable<Tag> tags = package_.getTags();
		packageCopy.setTags(stream(tags.spliterator(), false).map(Tag::newInstance).collect(toList()));
		return packageCopy;
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
		updateFullName();
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
		updateFullName();
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
	 * The label of this package
	 *
	 * @return package label or <tt>null</tt>
	 */
	public String getLabel()
	{
		return getString(PackageMetaData.LABEL);
	}

	public Package setLabel(String label)
	{
		set(PackageMetaData.LABEL, label);
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
	 * Get all tags for this package
	 *
	 * @return package tags
	 */
	public Iterable<Tag> getTags()
	{
		return getEntities(PackageMetaData.TAGS, Tag.class);
	}

	/**
	 * Set tags for this package
	 *
	 * @param tags package tags
	 * @return this package
	 */
	public Package setTags(Iterable<Tag> tags)
	{
		set(PackageMetaData.TAGS, tags);
		return this;
	}

	/**
	 * Add a {@link Tag} to this {@link Package}
	 *
	 * @param tag
	 */
	public void addTag(Tag tag)
	{
		set(PackageMetaData.TAGS, concat(getTags(), singletonList(tag)));
	}

	/**
	 * Remove a {@link Tag} from this {@link Package}
	 *
	 * @param tag
	 */
	public void removeTag(Tag tag)
	{
		Iterable<Tag> tags = getTags();
		removeAll(tags, singletonList(tag));
		set(PackageMetaData.TAGS, tag);
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
		Query<EntityMetaData> query = dataService.query(ENTITY_META_DATA, EntityMetaData.class)
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
		Query<Package> query = dataService.query(PackageMetaData.PACKAGE, Package.class)
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

	private void updateFullName()
	{
		String simpleName = getSimpleName();
		if (simpleName != null)
		{
			String fullName;
			Package parentPackage = getParent();
			if (parentPackage != null)
			{
				fullName = parentPackage.getName() + PACKAGE_SEPARATOR + simpleName;
			}
			else
			{
				fullName = simpleName;
			}
			set(PackageMetaData.FULL_NAME, fullName);
		}
	}

	@Override
	public String toString()
	{
		return "Package{" + "name=" + getName() + '}';
	}
}
