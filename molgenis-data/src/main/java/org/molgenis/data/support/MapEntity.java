package org.molgenis.data.support;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple Entity implementation based on a Map
 */
public class MapEntity extends AbstractEntity
{
	private static final long serialVersionUID = -8283375007931769373L;
	private EntityMetaData entityMetaData;
	private Map<String, Object> values = new LinkedCaseInsensitiveMap<>();
	private String idAttributeName = null;

	public MapEntity()
	{
	}

	public MapEntity(Entity other)
	{
		set(other);
	}

	public MapEntity(Entity other, EntityMetaData metaData)
	{
		set(other, metaData);
	}

	public MapEntity(String idAttributeName)
	{
		this.idAttributeName = idAttributeName;
	}

	public MapEntity(Map<String, Object> values)
	{
		this.values = values;
	}

	public MapEntity(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	public MapEntity(EntityMetaData metaData)
	{
		this.entityMetaData = metaData;
		this.idAttributeName = entityMetaData.getIdAttribute().getName();
	}

	public void set(Entity other, EntityMetaData metaData)
	{
		this.entityMetaData = metaData;
		this.idAttributeName = entityMetaData.getIdAttribute().getName();
		List<String> otherAttributes = new ArrayList<>();
		for (AttributeMetaData attributeMetaData : other.getEntityMetaData().getAtomicAttributes())
		{
			otherAttributes.add(attributeMetaData.getName());
		}
		for (AttributeMetaData attribute : metaData.getAtomicAttributes())
		{
			if (Iterables.contains(otherAttributes, attribute.getName()))
				set(attribute.getName(), other.get(attribute.getName()));
		}
	}

	@Override
	public Object get(String attributeName)
	{
		return values.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	@Override
	public void set(Entity other)
	{
		for (String attributeName : other.getAttributeNames())
		{
			set(attributeName, other.get(attributeName));
		}
	}

	@Override
	public Object getIdValue()
	{
		if (getIdAttributeName() == null)
		{
			return null;
		}

		return get(getIdAttributeName());
	}

	@Override
	public String getLabelValue()
	{
		return null;
	}

	public String getIdAttributeName()
	{
		return idAttributeName;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		if (entityMetaData != null)
		{
			return Iterables.transform(entityMetaData.getAtomicAttributes(), new Function<AttributeMetaData, String>()
			{
				@Override
				public String apply(AttributeMetaData input)
				{
					return input.getName();
				}
			});
		}
		return values.keySet();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityMetaData == null) ? 0 : entityMetaData.hashCode());
		result = prime * result + ((idAttributeName == null) ? 0 : idAttributeName.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MapEntity other = (MapEntity) obj;
		if (entityMetaData == null)
		{
			if (other.entityMetaData != null) return false;
		}
		else if (!entityMetaData.equals(other.entityMetaData)) return false;
		if (idAttributeName == null)
		{
			if (other.idAttributeName != null) return false;
		}
		else if (!idAttributeName.equals(other.idAttributeName)) return false;
		if (values == null)
		{
			if (other.values != null) return false;
		}
		else if (!values.equals(other.values)) return false;
		return true;
	}
}
