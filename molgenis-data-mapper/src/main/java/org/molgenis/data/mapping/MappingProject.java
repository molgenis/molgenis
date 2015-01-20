package org.molgenis.data.mapping;

import java.util.List;

/**
 * Created by charbonb on 14/01/15.
 */
public class MappingProject
{
	private String identifier;
	private String owner;
	private List<EntityMapping> entityMappings;

	public MappingProject(String identifier, String owner, List<EntityMapping> entityMappings)
	{
		this.identifier = identifier;
		this.owner = owner;
		this.entityMappings = entityMappings;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public String getOwner()
	{
		return owner;
	}

	public void String(String owner)
	{
		this.owner = owner;
	}

	public List<EntityMapping> getEntityMappings()
	{
		return entityMappings;
	}

	public void setEntityMappings(List<EntityMapping> entityMappings)
	{
		this.entityMappings = entityMappings;
	}
}
