package org.molgenis.data.mapper.mapping.model;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EntityMapping
{
	private String identifier;
	private final EntityType sourceEntityType;
	private final EntityType targetEntityType;
	private final Map<String, AttributeMapping> attributeMappings;

	/**
	 * Creates a new empty EntityMapping with no {@link AttributeMapping}s.
	 */
	public EntityMapping(EntityType source, EntityType target)
	{
		this.identifier = null;
		this.sourceEntityType = source;
		this.targetEntityType = target;
		this.attributeMappings = new LinkedHashMap<>();
	}

	public EntityMapping(String identifier, EntityType sourceEntityType, EntityType targetEntityType,
			List<AttributeMapping> attributeMappings)
	{
		this.identifier = identifier;
		this.sourceEntityType = sourceEntityType;
		this.targetEntityType = targetEntityType;
		this.attributeMappings = new LinkedHashMap<>();
		for (AttributeMapping mapping : attributeMappings)
		{
			this.attributeMappings.put(mapping.getTargetAttribute().getName(), mapping);
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
		if (sourceEntityType == null)
		{
			return null;
		}
		return sourceEntityType.getId();
	}

	public String getLabel()
	{
		if (sourceEntityType == null)
		{
			return null;
		}
		return sourceEntityType.getLabel();
	}

	public EntityType getSourceEntityType()
	{
		return sourceEntityType;
	}

	public EntityType getTargetEntityType()
	{
		return targetEntityType;
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
			throw new IllegalStateException(
					"AttributeMapping already exists for target attribute " + targetAttributeName);
		}
		Attribute targetAttribute = targetEntityType.getAttribute(targetAttributeName);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
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
		result = prime * result + ((sourceEntityType == null) ? 0 : sourceEntityType.hashCode());
		result = prime * result + ((targetEntityType == null) ? 0 : targetEntityType.hashCode());
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
		if (sourceEntityType == null)
		{
			if (other.sourceEntityType != null) return false;
		}
		else if (!sourceEntityType.equals(other.sourceEntityType)) return false;
		if (targetEntityType == null)
		{
			if (other.targetEntityType != null) return false;
		}
		else if (!targetEntityType.equals(other.targetEntityType)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "EntityMapping [identifier=" + identifier + ", sourceEntityType=" + sourceEntityType
				+ ", targetEntityType=" + targetEntityType + ", attributeMappings=" + attributeMappings + "]";
	}

	public void deleteAttributeMapping(String attribute)
	{
		attributeMappings.remove(attribute);
	}
}
