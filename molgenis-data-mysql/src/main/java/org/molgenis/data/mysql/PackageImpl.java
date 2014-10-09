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

	public PackageImpl(final String name, final String description)
	{
		super();
		set(PackageMetaData.NAME, name);
		set(PackageMetaData.DESCRIPTION, description);
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

	@Override
	public String getName()
	{
		return getString(PackageMetaData.NAME);
	}

	@Override
	public String getDescription()
	{
		return getString(PackageMetaData.DESCRIPTION);
	}

	// TODO: add methods for the package browser. Retrieve subpackages?

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
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
		if (getDescription() == null)
		{
			if (other.getDescription() != null) return false;
		}
		else if (!getDescription().equals(other.getDescription())) return false;
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
