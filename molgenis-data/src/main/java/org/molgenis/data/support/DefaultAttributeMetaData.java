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
	private String refEntityName = null;
	private String label = null;
	private boolean visible = true;

	public DefaultAttributeMetaData(String name, FieldTypeEnum fieldType)
	{
		if (name == null) throw new IllegalArgumentException("Name cannot be null");
		if (fieldType == null) throw new IllegalArgumentException("FieldType cannot be null");
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
		if (refEntityName == null)
		{
			return null;
		}

		return EntityMetaDataCache.get(refEntityName);
	}

	public void setRefEntityName(String refEntityName)
	{
		this.refEntityName = refEntityName;
	}

	@Override
	public String getLabel()
	{
		return label == null ? name : label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

}
