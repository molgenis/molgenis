package org.molgenis.data.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.util.CaseInsensitiveLinkedHashMap;

public class DefaultEntityMetaData extends AbstractEntityMetaData
{
	private final String name;
	private final Map<String, AttributeMetaData> attributes = new CaseInsensitiveLinkedHashMap<AttributeMetaData>();
	private final Class<? extends Entity> entityClass;
	private String label;
	private boolean abstract_ = false;
	private String description;
	private EntityMetaData extends_;

	public DefaultEntityMetaData(String name)
	{
		this(name, Entity.class);
	}

	public DefaultEntityMetaData(String name, Class<? extends Entity> entityClass)
	{
		if (name == null) throw new IllegalArgumentException("Name cannot be null");
		if (entityClass == null) throw new IllegalArgumentException("EntityClass cannot be null");
		this.name = name;
		this.entityClass = entityClass;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void addAttributeMetaData(AttributeMetaData attributeMetaData)
	{
		if (attributeMetaData == null) throw new IllegalArgumentException("AttributeMetaData cannot be null");
		if (attributeMetaData.getName() == null) throw new IllegalArgumentException(
				"Name of the AttributeMetaData cannot be null");
		if (attributeMetaData.isLabelAttribute()) setLabelAttribute(attributeMetaData.getName());
		if (attributeMetaData.isIdAtrribute()) setIdAttribute(attributeMetaData.getName());

		attributes.put(attributeMetaData.getName().toLowerCase(), attributeMetaData);
	}

	public void removeAttributeMetaData(AttributeMetaData attributeMetaData)
	{
		this.attributes.remove(attributeMetaData.getName());
	}

	public void addAllAttributeMetaData(List<AttributeMetaData> attributeMetaDataList)
	{
		for (AttributeMetaData attributeMetaData : attributeMetaDataList)
		{
			if (attributeMetaData == null) throw new IllegalArgumentException("AttributeMetaData cannot be null");
			if (attributeMetaData.getName() == null) throw new IllegalArgumentException(
					"Name of the AttributeMetaData cannot be null");

			attributes.put(attributeMetaData.getName().toLowerCase(), attributeMetaData);
		}
	}

	@Override
	public List<AttributeMetaData> getAttributes()
	{
		List<AttributeMetaData> result = new ArrayList<AttributeMetaData>();
		if (this.getExtends() != null)
		{
			for (AttributeMetaData att : getExtends().getAttributes())
			{
				result.add(att);
			}
		}
		result.addAll(attributes.values());
		return Collections.unmodifiableList(result);
	}

	@Override
	public String getLabel()
	{
		return label != null ? label : name;
	}

	public DefaultEntityMetaData setLabel(String label)
	{
		this.label = label;
		return this;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public DefaultEntityMetaData setDescription(String description)
	{
		this.description = description;
		return this;
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return entityClass;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DefaultEntityMetaData other = (DefaultEntityMetaData) obj;
		if (name == null)
		{
			if (other.name != null) return false;
		}
		else if (!name.equals(other.name)) return false;
		return true;
	}

	public DefaultAttributeMetaData addAttribute(String name)
	{
		DefaultAttributeMetaData result = new DefaultAttributeMetaData(name);
		this.addAttributeMetaData(result);
		return result;
	}

	@Override
	public boolean isAbstract()
	{
		return abstract_;
	}

	public DefaultEntityMetaData setAbstract(boolean abstract_)
	{
		this.abstract_ = abstract_;
		return this;
	}

	@Override
	public EntityMetaData getExtends()
	{
		return extends_;
	}

	public DefaultEntityMetaData setExtends(EntityMetaData extends_)
	{
		this.extends_ = extends_;
		return this;
	}

	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder("\nEntityMetaData(name='");
		strBuilder.append(this.getName()).append('\'');
		if (isAbstract()) strBuilder.append(" abstract='true'");
		if (getExtends() != null) strBuilder.append(" extends='" + getExtends().getName()).append('\'');
		if (getIdAttribute() != null) strBuilder.append(" idAttribute='").append(getIdAttribute().getName())
				.append('\'');
		if (getDescription() != null) strBuilder.append(" description='")
				.append(getDescription().substring(0, Math.min(25, getDescription().length())))
				.append(getDescription().length() > 25 ? "...'" : "'");
		strBuilder.append(')');
		for (AttributeMetaData att : this.getAttributes())
		{
			strBuilder.append("\n\t").append(att.toString());
		}
		return strBuilder.toString();
	}
}
