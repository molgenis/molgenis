package org.molgenis.data.support;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Range;
import org.molgenis.fieldtypes.CategoricalField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;

/**
 * Default implementation of the AttributeMetaData interface
 * 
 */
public class DefaultAttributeMetaData implements AttributeMetaData
{
	private final String name;
	private FieldType fieldType = MolgenisFieldTypes.STRING;
	private String description;
	private boolean nillable = true;
	private boolean readOnly = false;
	private Object defaultValue = null;
	private boolean idAttribute = false;
	private boolean labelAttribute = false; // remove?
	private boolean lookupAttribute = false; // remove?
	private EntityMetaData refEntity;
	private String label;
	private boolean visible = true; // remove?
	private boolean unique = false;
	private boolean auto = false;
	private Iterable<AttributeMetaData> attributesMetaData;
	private boolean aggregateable = false;
	private Range range;

	public DefaultAttributeMetaData(String name, FieldTypeEnum fieldType)
	{
		if (name == null) throw new IllegalArgumentException("Name cannot be null");
		if (fieldType == null) throw new IllegalArgumentException("FieldType cannot be null");
		this.name = name;
		this.fieldType = MolgenisFieldTypes.getType(fieldType.toString().toLowerCase());
	}

	public DefaultAttributeMetaData(String name)
	{
		if (name == null) throw new IllegalArgumentException("Name cannot be null");
		this.name = name;
		this.fieldType = MolgenisFieldTypes.STRING;
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
		return fieldType;
	}

	public DefaultAttributeMetaData setDataType(FieldType type)
	{
		this.fieldType = type;
		return this;
	}

	@Override
	public boolean isNillable()
	{
		return nillable;
	}

	public DefaultAttributeMetaData setNillable(boolean nillable)
	{
		this.nillable = nillable;
		return this;
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
		if (getDataType() instanceof XrefField || getDataType() instanceof MrefField
				|| getDataType() instanceof CategoricalField)
		{
			if (getRefEntity() == null) throw new MolgenisDataException("refEntity is missing for " + getName());
			if (getRefEntity().getIdAttribute() == null) throw new MolgenisDataException(
					"idAttribute is missing for entity [" + getRefEntity().getName() + "]");

			return getRefEntity().getIdAttribute().getDataType().convert(defaultValue);
		}

		return getDataType().convert(defaultValue);
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

	public DefaultAttributeMetaData setIdAttribute(boolean idAttribute)
	{
		this.idAttribute = idAttribute;
		return this;
	}

	@Override
	public boolean isLabelAttribute()
	{
		return labelAttribute;
	}

	public DefaultAttributeMetaData setLabelAttribute(boolean labelAttribute)
	{
		this.labelAttribute = labelAttribute;
		return this;
	}

	@Override
	public EntityMetaData getRefEntity()
	{
		return refEntity;
	}

	public DefaultAttributeMetaData setRefEntity(EntityMetaData refEntity)
	{
		this.refEntity = refEntity;
		return this;
	}

	@Override
	public Iterable<AttributeMetaData> getAttributeParts()
	{
		if (this.attributesMetaData == null && this.getRefEntity() != null)
		{
			return this.refEntity.getAttributes();
		}
		return attributesMetaData;
	}

	public void setAttributesMetaData(Iterable<AttributeMetaData> attributesMetaData)
	{
		this.attributesMetaData = attributesMetaData;
	}

	@Override
	public String getLabel()
	{
		return label == null ? name : label;
	}

	public DefaultAttributeMetaData setLabel(String label)
	{
		this.label = label;
		return this;
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	public DefaultAttributeMetaData setVisible(boolean visible)
	{
		this.visible = visible;
		return this;
	}

	@Override
	public boolean isUnique()
	{
		return unique;
	}

	public DefaultAttributeMetaData setUnique(boolean unique)
	{
		this.unique = unique;
		return this;
	}

	@Override
	public boolean isAuto()
	{
		return auto;
	}

	public DefaultAttributeMetaData setAuto(boolean auto)
	{
		this.auto = auto;
		return this;
	}

	@Override
	public boolean isLookupAttribute()
	{
		return lookupAttribute;
	}

	public DefaultAttributeMetaData setLookupAttribute(boolean lookupAttribute)
	{
		this.lookupAttribute = lookupAttribute;
		return this;
	}

	@Override
	public String toString()
	{
		String result = "AttributeMetaData(name='" + this.getName() + "'";
		result += " dataType='" + getDataType() + "'";
		if (getRefEntity() != null) result += " refEntity='" + getRefEntity().getName() + "'";
		if (getDescription() != null) result += " description='" + getDescription() + "'";
		result += ")";
		return result;
	}

	@Override
	public boolean isAggregateable()
	{
		return this.aggregateable;
	}

	public void setAggregateable(boolean aggregateable)
	{
		this.aggregateable = aggregateable;
	}

	@Override
	public Range getRange()
	{
		return range;
	}

	public void setRange(Range range)
	{
		this.range = range;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DefaultAttributeMetaData that = (DefaultAttributeMetaData) o;

		if (name != null ? !name.equals(that.name) : that.name != null) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		return name != null ? name.hashCode() : 0;
	}
}
