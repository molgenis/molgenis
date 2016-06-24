package org.molgenis.data.mapper.mapping.model;

import java.util.Collection;
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

	public void removeIdentifiers()
	{
		identifier = null;
		attributeMappings.values().forEach(a -> a.setIdentifier(null));
	}

	public String getName()
	{
		if (sourceEntityMetaData == null)
		{
			return null;
		}
		return sourceEntityMetaData.getName();
	}

	public String getLabel()
	{
		if (sourceEntityMetaData == null)
		{
			return null;
		}
		return sourceEntityMetaData.getLabel();
	}

	public EntityMetaData getSourceEntityMetaData()
	{
		return sourceEntityMetaData;
	}

	public EntityMetaData getTargetEntityMetaData()
	{
		return targetEntityMetaData;
	}

	public Collection<AttributeMapping> getAttributeMappings()
	{
		return attributeMappings.values();
	}

	public AttributeMapping getAttributeMapping(String name)
	{
		return attributeMappings.get(name);
	}

	/**
	 * Adds a new empty attribute mapping to a target attribute
	 * 
	 * @param targetAttributeName
	 * @return the newly created attribute mapping.
	 */
	public AttributeMapping addAttributeMapping(String targetAttributeName)
	{
		if (attributeMappings.containsKey(targetAttributeName))
		{
			throw new IllegalStateException("AttributeMapping already exists for target attribute "
					+ targetAttributeName);
		}
		AttributeMetaData targetAttributeMetaData = targetEntityMetaData.getAttribute(targetAttributeName);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMappings.put(targetAttributeName, attributeMapping);
		return attributeMapping;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeMappings == null) ? 0 : attributeMappings.hashCode());
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((sourceEntityMetaData == null) ? 0 : sourceEntityMetaData.hashCode());
		result = prime * result + ((targetEntityMetaData == null) ? 0 : targetEntityMetaData.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		EntityMapping other = (EntityMapping) obj;
		if (attributeMappings == null)
		{
			if (other.attributeMappings != null) return false;
		}
		else if (!attributeMappings.equals(other.attributeMappings)) return false;
		if (identifier == null)
		{
			if (other.identifier != null) return false;
		}
		else if (!identifier.equals(other.identifier)) return false;
		if (sourceEntityMetaData == null)
		{
			if (other.sourceEntityMetaData != null) return false;
		}
		else if (!sourceEntityMetaData.equals(other.sourceEntityMetaData)) return false;
		if (targetEntityMetaData == null)
		{
			if (other.targetEntityMetaData != null) return false;
		}
		else if (!targetEntityMetaData.equals(other.targetEntityMetaData)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "EntityMapping [identifier=" + identifier + ", sourceEntityMetaData=" + sourceEntityMetaData
				+ ", targetEntityMetaData=" + targetEntityMetaData + ", attributeMappings=" + attributeMappings + "]";
	}

	public void deleteAttributeMapping(String attribute)
	{
		attributeMappings.remove(attribute);
	}

}
