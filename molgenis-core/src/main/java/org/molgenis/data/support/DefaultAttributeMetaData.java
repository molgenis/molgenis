package org.molgenis.data.support;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.fieldtypes.FieldType;

/**
 * Default implementation of the AttributeMetaData interface
 * 
 */
public class DefaultAttributeMetaData implements AttributeMetaData
{
	private final String name;
	private final FieldTypeEnum fieldType;
	private String description = null;
	private boolean nillable = true;
	private boolean readOnly = false;
	private Object defaultValue = null;
	private boolean idAttribute = false;
	private boolean labelAttribute = false;
	private EntityMetaData refEntity = null;
	private AttributeMetaData refAttribute = null;

	public DefaultAttributeMetaData(String name, FieldTypeEnum fieldType)
	{
		if (name == null) throw new IllegalArgumentException("Name cannot be null");
		this.name = name;
		this.fieldType = fieldType;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public FieldType getDataType()
	{
		return MolgenisFieldTypes.getType(fieldType.toString().toLowerCase());
	}

	@Override
	public boolean isNillable()
	{
		return nillable;
	}

	public void setNillable(boolean nillable)
	{
		this.nillable = nillable;
	}

	@Override
	public boolean isReadonly()
	{
		return readOnly;
	}

	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	@Override
	public Object getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	@Override
	public boolean isIdAtrribute()
	{
		return idAttribute;
	}

	public void setIdAttribute(boolean idAttribute)
	{
		this.idAttribute = idAttribute;
	}

	@Override
	public boolean isLabelAttribute()
	{
		return labelAttribute;
	}

	public void setLabelAttribute(boolean labelAttribute)
	{
		this.labelAttribute = labelAttribute;
	}

	@Override
	public EntityMetaData getRefEntity()
	{
		return refEntity;
	}

	public void setRefEntity(EntityMetaData refEntity)
	{
		this.refEntity = refEntity;
	}

	@Override
	public AttributeMetaData getRefAttribute()
	{
		return refAttribute;
	}

	public void setRefAttribute(AttributeMetaData refAttribute)
	{
		this.refAttribute = refAttribute;
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
		DefaultAttributeMetaData other = (DefaultAttributeMetaData) obj;
		if (name == null)
		{
			if (other.name != null) return false;
		}
		else if (!name.equals(other.name)) return false;
		return true;
	}

}
