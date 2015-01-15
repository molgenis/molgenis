package org.molgenis.data.mapping;

import org.molgenis.data.EntityMetaData;

import java.util.List;

/**
 * Created by charbonb on 14/01/15.
 */
public class EntityMapping
{
	private String identifier;
	private EntityMetaData sourceEntityMetaData;
	private EntityMetaData targetEntityMetaData;
	private List<AttributeMapping> entityMappings;

	public EntityMapping(String identifier, EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData,
			List<AttributeMapping> entityMappings)
	{
		this.identifier = identifier;
		this.sourceEntityMetaData = sourceEntityMetaData;
		this.targetEntityMetaData = targetEntityMetaData;
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

	public EntityMetaData getSourceEntityMetaData()
	{
		return sourceEntityMetaData;
	}

	public void setSourceEntityMetaData(EntityMetaData sourceEntityMetaData)
	{
		this.sourceEntityMetaData = sourceEntityMetaData;
	}

	public EntityMetaData getTargetEntityMetaData()
	{
		return targetEntityMetaData;
	}

	public void setTargetEntityMetaData(EntityMetaData targetEntityMetaData)
	{
		this.targetEntityMetaData = targetEntityMetaData;
	}

	public List<AttributeMapping> getEntityMappings()
	{
		return entityMappings;
	}

	public void setEntityMappings(List<AttributeMapping> entityMappings)
	{
		this.entityMappings = entityMappings;
	}
}
