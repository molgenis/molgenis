package org.molgenis.data.meta;

import org.molgenis.data.Package;

public class PackageSearchResultItem
{
	private final Package packageFound;
	private String matchDescription;

	public PackageSearchResultItem(Package packageFound, String matchDescription)
	{
		this(packageFound);
		this.matchDescription = matchDescription;
	}

	public PackageSearchResultItem(Package packageFound)
	{
		if (packageFound == null) throw new IllegalArgumentException("packageFound is null");
		this.packageFound = packageFound;
	}

	public Package getPackageFound()
	{
		return packageFound;
	}

	/**
	 * A description of what was matched, package, entity or attribute
	 * 
	 * @return
	 */
	public String getMatchDescription()
	{
		return matchDescription;
	}

	@Override
	public int hashCode()
	{
		return packageFound.getName().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PackageSearchResultItem other = (PackageSearchResultItem) obj;
		return packageFound.getName().equals(other.getPackageFound().getName());
	}

}
