package org.molgenis.data.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.support.MapEntity;

/**
 * Instance of a Package.
 */
public class PackageImpl implements Package
{
	private List<PackageImpl> subPackages = new ArrayList<PackageImpl>();
	private List<EntityMetaData> entities = new ArrayList<EntityMetaData>();
	public static final Package defaultPackage = new PackageImpl(Package.DEFAULT_PACKAGE_NAME,
			"The default package", null);

	final private String simpleName;
	final private String description;
	private Package parent;

	public PackageImpl(String simpleName, String description, PackageImpl parent)
	{
		this.description = description;
		this.simpleName = simpleName;
		this.parent = parent;
	}

	public PackageImpl(String simpleName, String description)
	{
		this(simpleName, description, null);
	}

	@Override
	public String getSimpleName()
	{
		return simpleName;
	}

	@Override
	public Package getParent()
	{
		return parent;
	}

	@Override
	public String getName()
	{
		if (parent != null)
		{
			return (parent.getName() + PACKAGE_SEPARATOR + simpleName);
		}
		return simpleName;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((entities == null) ? 0 : entities.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((simpleName == null) ? 0 : simpleName.hashCode());
		result = prime * result + ((subPackages == null) ? 0 : subPackages.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PackageImpl other = (PackageImpl) obj;
		if (description == null)
		{
			if (other.description != null) return false;
		}
		else if (!description.equals(other.description)) return false;
		if (entities == null)
		{
			if (other.entities != null) return false;
		}
		else if (!entities.equals(other.entities)) return false;
		if (parent == null)
		{
			if (other.parent != null) return false;
		}
		else if (!parent.getName().equals(other.parent.getName())) return false;
		if (simpleName == null)
		{
			if (other.simpleName != null) return false;
		}
		else if (!simpleName.equals(other.simpleName)) return false;
		if (subPackages == null)
		{
			if (other.subPackages != null) return false;
		}
		else if (!subPackages.equals(other.subPackages)) return false;
		return true;
	}

	void addSubPackage(PackageImpl p)
	{
		subPackages.add(p);
	}

	void addEntity(EntityMetaData entityMetaData)
	{
		entities.add(entityMetaData);
	}

	public void setParent(Package parent)
	{
		this.parent = parent;
	}

	@Override
	public Entity toEntity()
	{
		Entity result = new MapEntity();
		result.set(PackageMetaData.FULL_NAME, getName());
		result.set(PackageMetaData.SIMPLE_NAME, simpleName);
		result.set(PackageMetaData.DESCRIPTION, description);
		if (parent != null)
		{
			result.set(PackageMetaData.PARENT, parent.toEntity());
		}
		return result;
	}

	@Override
	public String toString()
	{
		return "PackageImpl [subPackages=" + subPackages + ", entities=" + entities + ", simpleName=" + simpleName
				+ ", description=" + description + ", parent=" + (parent == null ? "null" : parent.getName()) + "]";
	}

	@Override
	public Iterable<EntityMetaData> getEntityMetaDatas()
	{
		return Collections.<EntityMetaData> unmodifiableList(entities);
	}

	@Override
	public Iterable<Package> getSubPackages()
	{
		return Collections.<Package> unmodifiableList(subPackages);
	}
}
