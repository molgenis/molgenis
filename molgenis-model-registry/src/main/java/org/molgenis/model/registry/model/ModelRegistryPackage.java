package org.molgenis.model.registry.model;

import org.molgenis.data.meta.model.Package;

import java.util.List;

/**
 * @author sido
 */
public class ModelRegistryPackage
{

	private String name;
	private String label;
	private String description;
	private String matchDescription;
	private List<ModelRegistryEntity> entitiesInPackage;
	private List<ModelRegistryTag> tags;

	public ModelRegistryPackage(Package _package)
	{
		this.name = _package.getId();
		this.label = _package.getLabel();
		this.description = _package.getDescription();
//		this.matchDescription = _package.;
//		this.entitiesInPackage = entitiesInPackage;
//		this.tags = tags;
	}

	public ModelRegistryPackage(String name, String label, String description, String matchDescription,
			List<ModelRegistryEntity> entitiesInPackage, List<ModelRegistryTag> tags)
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
	public List<ModelRegistryEntity> getEntities()
	{
		return entitiesInPackage;
	}

	@SuppressWarnings("unused")
	public Iterable<ModelRegistryTag> getTags()
	{
		return tags;
	}
}
