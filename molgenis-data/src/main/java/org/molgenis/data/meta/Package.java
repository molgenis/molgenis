package org.molgenis.data.meta;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.removeAll;
import static java.util.Collections.singletonList;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.meta.PackageMetaData.DESCRIPTION;
import static org.molgenis.data.meta.PackageMetaData.FULL_NAME;
import static org.molgenis.data.meta.PackageMetaData.LABEL;
import static org.molgenis.data.meta.PackageMetaData.PACKAGE;
import static org.molgenis.data.meta.PackageMetaData.PARENT;
import static org.molgenis.data.meta.PackageMetaData.SIMPLE_NAME;
import static org.molgenis.data.meta.PackageMetaData.TAGS;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.util.ApplicationContextProvider;

/**
 * Package defines the structure and attributes of a Package. Attributes are unique. Other software components can use
 * this to interact with Packages and/or to configure backends and frontends, including Repository instances.
 */
public class Package extends SystemEntity
{
	public static final String PACKAGE_SEPARATOR = "_";

	/**
	 * Constructs a package based on the given entity
	 *
	 * @param entity decorated entity
	 */
	public Package(Entity entity)
	{
		super(entity, PACKAGE);
	}

	/**
	 * Constructs a package with the given meta data
	 *
	 * @param packageMetaData package meta data
	 */
	public Package(PackageMetaData packageMetaData)
	{
		super(packageMetaData);
	}

	/**
	 * Constructs a package with the given type code and meta data
	 *
	 * @param simpleName      package name
	 * @param packageMetaData language meta data
	 */
	public Package(String simpleName, PackageMetaData packageMetaData)
	{
		super(packageMetaData);
		setSimpleName(simpleName);
		setName(simpleName);
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
		return getEntity(PARENT, Package.class);
	}

	public Package setParent(Package parentPackage)
	{
		set(PARENT, parentPackage);
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
		return getString(FULL_NAME);
	}

	public Package setName(String fullName)
	{
		set(FULL_NAME, fullName);
		return this;
	}

	/**
	 * The label of this package
	 *
	 * @return package label or <tt>null</tt>
	 */
	public String getLabel()
	{
		return getString(LABEL);
	}

	public Package setLabel(String label)
	{
		set(LABEL, label);
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
		set(TAGS, concat(getTags(), singletonList(tag)));
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
		set(TAGS, tag);
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
		Query<Package> query = dataService.query(PACKAGE, Package.class).eq(PARENT, this);
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
			set(EntityMetaDataMetaData.FULL_NAME, fullName);
		}
	}
}
