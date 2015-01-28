package org.molgenis.data.mapping.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

/**
 * Created by charbonb on 14/01/15.
 */
public class EntityMapping
{
	private String identifier;
	private final EntityMetaData sourceEntityMetaData;
	private final EntityMetaData targetEntityMetaData;
	private final Map<String, AttributeMapping> attributeMappings;

	/**
	 * Creates a new empty EntityMapping with no {@link AttributeMapping}s.
	 */
	public EntityMapping(EntityMetaData source, EntityMetaData target)
	{
		this.identifier = null;
		this.sourceEntityMetaData = source;
		this.targetEntityMetaData = target;
		this.attributeMappings = new LinkedHashMap<String, AttributeMapping>();
	}

	public EntityMapping(String identifier, EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData,
			List<AttributeMapping> attributeMappings)
	{
		this.identifier = identifier;
		this.sourceEntityMetaData = sourceEntityMetaData;
		this.targetEntityMetaData = targetEntityMetaData;
		this.attributeMappings = new LinkedHashMap<String, AttributeMapping>();
		for (AttributeMapping mapping : attributeMappings)
		{
			this.attributeMappings.put(mapping.getTargetAttributeMetaData().getName(), mapping);
		}
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

	public EntityMetaData getTargetEntityMetaData()
	{
		return targetEntityMetaData;
	}

	public Map<String, AttributeMapping> getAttributeMappings()
	{
		return attributeMappings;
	}

	/**
	 * Adds a new empty attribute mapping to a target attribute
	 * 
	 * @param targetAttributeName
	 * @return the newly created attribute mapping.
	 */
	public AttributeMapping addAttributeMapping(String targetAttributeName)
	{
		AttributeMetaData targetAttributeMetaData = targetEntityMetaData.getAttribute(targetAttributeName);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMappings.put(targetAttributeName, attributeMapping);
		return attributeMapping;
	}
}
