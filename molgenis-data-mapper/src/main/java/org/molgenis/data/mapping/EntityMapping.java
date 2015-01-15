package org.molgenis.data.mapping;

import org.molgenis.data.EntityMetaData;

import java.util.List;

/**
 * Created by charbonb on 14/01/15.
 */
public class EntityMapping
{
	private String id;
	private EntityMetaData sourceEntityMetaData;
	private EntityMetaData targetEntityMetaData;
	private List<AttributeMapping> entityMappings;

	public EntityMapping(String id, EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData,
			List<AttributeMapping> entityMappings)
	{
		this.id = id;
		this.sourceEntityMetaData = sourceEntityMetaData;
		this.targetEntityMetaData = targetEntityMetaData;
		this.entityMappings = entityMappings;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
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
