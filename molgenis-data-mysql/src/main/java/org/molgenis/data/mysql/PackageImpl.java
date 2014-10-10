package org.molgenis.data.mysql;

import org.molgenis.data.Entity;
import org.molgenis.data.Package;
import org.molgenis.data.support.MapEntity;

/**
 * Instance of a Package.
 */
public class PackageImpl extends MapEntity implements Package, Comparable<Package>
{
	private static final long serialVersionUID = 1L;

	public PackageImpl(final String simpleName)
	{
		this(simpleName, null);
	}

	public PackageImpl(final String simpleName, final String description)
	{
		super(PackageMetaData.FULL_NAME);
		set(PackageMetaData.SIMPLE_NAME, simpleName);
		set(PackageMetaData.FULL_NAME, simpleName);
		set(PackageMetaData.DESCRIPTION, description);
	}

	public PackageImpl(final String simpleName, final String description, Package parent)
	{
		this(simpleName, description);
		setParent(parent);
	}

	/**
	 * Create a new Package instance, containing the values stored in another Entity.
	 * 
	 * @param entity
	 *            the entity to base this Package entity on.
	 */
	public PackageImpl(Entity entity)
	{
		super(entity);
	}

	public PackageImpl(Package p)
	{
		this(p.getSimpleName(), p.getDescription(), p.getParent());
	}

	@Override
	public String getSimpleName()
	{
		return getString(PackageMetaData.SIMPLE_NAME);
	}

	public void setParent(Package parent)
	{
		set(PackageMetaData.PARENT, parent);
		set(PackageMetaData.FULL_NAME, constructFullName());
	}

	@Override
	public Package getParent()
	{
		Entity parent = getEntity(PackageMetaData.PARENT);
		if (parent == null)
		{
			return null;
		}

		return new PackageImpl(parent);
	}

	@Override
	public String getName()
	{
		return getString(PackageMetaData.FULL_NAME);
	}

	@Override
	public String getDescription()
	{
		return getString(PackageMetaData.DESCRIPTION);
	}

	private String constructFullName()
	{
		StringBuilder sb = new StringBuilder();

		Package parent = getParent();
		if (parent != null)
		{
			sb.append(parent.getName());
			sb.append(PACKAGE_SEPARATOR);
		}

		sb.append(getSimpleName());

		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PackageImpl other = (PackageImpl) obj;
		if (getName() == null)
		{
			if (other.getName() != null) return false;
		}
		else if (!getName().equals(other.getName())) return false;
		return true;
	}

	@Override
	public int compareTo(Package o)
	{
		return getName().compareTo(o.getName());
	}

}
