package org.molgenis.standardsregistry.model;

import java.util.List;

/**
 * @author sido
 */
public class PackageResponse
{

	private String name;
	private String label;
	private String description;
	private String matchDescription;
	private List<StandardRegistryEntity> entitiesInPackage;
	private List<StandardRegistryTag> tags;

	public PackageResponse(String name, String label, String description, String matchDescription,
			List<StandardRegistryEntity> entitiesInPackage, List<StandardRegistryTag> tags)
	{
		this.name = name;
		this.label = label;
		this.description = description;
		this.matchDescription = matchDescription;
		this.entitiesInPackage = entitiesInPackage;
		this.tags = tags;
	}

	@SuppressWarnings("unused")
	public String getName()
	{
		return name;
	}

	public String getLabel()
	{
		return label;
	}

	@SuppressWarnings("unused")
	public String getDescription()
	{
		return description;
	}

	@SuppressWarnings("unused")
	public String getMatchDescription()
	{
		return matchDescription;
	}

	@SuppressWarnings("unused")
	public List<StandardRegistryEntity> getEntities()
	{
		return entitiesInPackage;
	}

	@SuppressWarnings("unused")
	public Iterable<StandardRegistryTag> getTags()
	{
		return tags;
	}
}
