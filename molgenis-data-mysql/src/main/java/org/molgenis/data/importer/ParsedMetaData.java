package org.molgenis.data.importer;

import java.util.List;
import java.util.Map;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public final class ParsedMetaData
{
	private final ImmutableList<EntityMetaData> entities;
	private final ImmutableMap<String, Package> packages;

	public ParsedMetaData(List<? extends EntityMetaData> entities, Map<String, ? extends Package> packages)
	{
		if (entities == null)
		{
			throw new NullPointerException("Null entities");
		}

		this.entities = ImmutableList.copyOf(entities);
		if (packages == null)
		{
			throw new NullPointerException("Null packages");
		}
		this.packages = ImmutableMap.copyOf(packages);
	}

	public ImmutableList<EntityMetaData> getEntities()
	{
		return entities;
	}

	public ImmutableMap<String, Package> getPackages()
	{
		return packages;
	}

	@Override
	public String toString()
	{
		return "ParsedMetaData{" + "getEntities=" + entities + ", getPackages=" + packages + "}";
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (o instanceof ParsedMetaData)
		{
			ParsedMetaData that = (ParsedMetaData) o;
			return (this.entities.equals(that.getEntities())) && (this.packages.equals(that.getPackages()));
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		int h = 1;
		h *= 1000003;
		h ^= entities.hashCode();
		h *= 1000003;
		h ^= packages.hashCode();
		return h;
	}

	/**
	 * Gets a specific package
	 * 
	 * @param name
	 *            the name of the package
	 * @return
	 */
	public org.molgenis.data.Package getPackage(String name)
	{
		return getPackages().get(name);
	}
}
