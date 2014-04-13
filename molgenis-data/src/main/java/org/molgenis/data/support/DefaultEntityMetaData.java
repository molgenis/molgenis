package org.molgenis.data.support;

import java.util.*;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Entity;

public class DefaultEntityMetaData extends AbstractEntityMetaData
{
	private final String name;
	private final Map<String, AttributeMetaData> attributes = new LinkedHashMap<String, AttributeMetaData>();
	private String label;
	private boolean abstract_ = false;
	private String description;
	private String idAttribute;
	private String labelAttribute; // remove?
	private EntityMetaData extends_;
	private final Class<? extends Entity> entityClass;

    public DefaultEntityMetaData(String name)
    {
        this(name,Entity.class);
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
        if(attributeMetaData.isLabelAttribute()) this.labelAttribute = attributeMetaData.getName();
        if(attributeMetaData.isIdAtrribute()) this.idAttribute = attributeMetaData.getName();

		attributes.put(attributeMetaData.getName().toLowerCase(), attributeMetaData);
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
	public AttributeMetaData getIdAttribute()
	{
		// primary key is first attribute unless otherwise indicate
		if (idAttribute != null)
		{
			AttributeMetaData att = getAttribute(idAttribute);
			if (att == null) throw new RuntimeException("getIdAttribute() failed: '" + idAttribute + "' unknown");
			return att;
		}
		else for (AttributeMetaData att : getAttributes())
			return att;
		return null;
	}

	public DefaultEntityMetaData setIdAttribute(String name)
	{
		this.idAttribute = name;
		return this;
	}

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		if (labelAttribute != null)
		{
			AttributeMetaData att = getAttribute(labelAttribute);
			if (att == null) throw new RuntimeException("getLabelAttribute() failed: '" + labelAttribute + "' unknown");
			return att;
		}
		return null;
	}

	public DefaultEntityMetaData setLabelAttribute(String name)
	{
		this.labelAttribute = name;
		return this;
	}

	@Override
	public AttributeMetaData getAttribute(String attributeName)
	{
		if (attributeName == null) throw new IllegalArgumentException("AttributeName is null");
		AttributeMetaData result = attributes.get(attributeName.toLowerCase());
		if (result == null && getExtends() != null) return getExtends().getAttribute(attributeName);
		else return result;
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

    public String toString()
    {
        String result = "EntityMetaData(name='"+this.getName()+"'";
        if(isAbstract()) result+=" abstract='true'";
        if(getExtends() != null) result +=" extends='"+getExtends().getName()+"'";
        result += ")";
        return result;
    }
}
