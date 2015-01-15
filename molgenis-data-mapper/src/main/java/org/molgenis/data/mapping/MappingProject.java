package org.molgenis.data.mapping;

import org.molgenis.auth.MolgenisUser;

import java.util.List;

/**
 * Created by charbonb on 14/01/15.
 */
public class MappingProject
{
	private String identifier;
	private MolgenisUser owner;
	private List<EntityMapping> entityMappings;

	public MappingProject(String identifier, MolgenisUser owner, List<EntityMapping> entityMappings)
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

	public MolgenisUser getOwner()
	{
		return owner;
	}

	public void setOwner(MolgenisUser owner)
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
