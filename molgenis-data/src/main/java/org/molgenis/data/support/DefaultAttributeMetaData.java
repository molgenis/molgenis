package org.molgenis.data.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Range;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.FieldType;

import com.google.common.collect.Lists;

/**
 * Default implementation of the AttributeMetaData interface
 * 
 */
public class DefaultAttributeMetaData implements AttributeMetaData
{
	private String name;
	private FieldType fieldType = MolgenisFieldTypes.STRING;
	private String description;
	private boolean nillable = true;
	private boolean readOnly = false;
	private String defaultValue = null;
	private boolean idAttribute = false;
	private boolean labelAttribute = false; // remove?
	private boolean lookupAttribute = false; // remove?
	private EntityMetaData refEntity;
	private String expression;
	private String label;
	private boolean visible = true; // remove?
	private boolean unique = false;
	private boolean auto = false;
	private List<AttributeMetaData> attributesMetaData;
	private boolean aggregateable = false;
	private Range range;
	private String visibleExpression;
	private String validationExpression;

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

	public DefaultAttributeMetaData(String newName, AttributeMetaData attributeMetaData)
	{
		this(attributeMetaData);
		this.name = newName;
	}

	/**
	 * Copy constructor
	 * 
	 * @param attributeMetaData
	 */
	public DefaultAttributeMetaData(AttributeMetaData attributeMetaData)
	{
		this.name = attributeMetaData.getName();
		this.fieldType = attributeMetaData.getDataType();
		this.description = attributeMetaData.getDescription();
		this.nillable = attributeMetaData.isNillable();
		this.readOnly = attributeMetaData.isReadonly();
		this.defaultValue = attributeMetaData.getDefaultValue();
		this.idAttribute = attributeMetaData.isIdAtrribute();
		this.labelAttribute = attributeMetaData.isLabelAttribute();
		this.lookupAttribute = attributeMetaData.isLookupAttribute();
		EntityMetaData refEntity = attributeMetaData.getRefEntity();
		this.refEntity = refEntity != null ? new DefaultEntityMetaData(refEntity) : null; // deep copy
		this.expression = attributeMetaData.getExpression();
		this.label = attributeMetaData.getLabel();
		this.visible = attributeMetaData.isVisible();
		this.unique = attributeMetaData.isUnique();
		this.auto = attributeMetaData.isAuto();
		this.aggregateable = attributeMetaData.isAggregateable();
		this.range = attributeMetaData.getRange();
		this.visibleExpression = attributeMetaData.getVisibleExpression();
		this.validationExpression = attributeMetaData.getValidationExpression();

		// deep copy
		Iterable<AttributeMetaData> attributeParts = attributeMetaData.getAttributeParts();
		if (attributeParts != null)
		{
			List<AttributeMetaData> attributesMetaData = new ArrayList<AttributeMetaData>();
			for (AttributeMetaData attributePart : attributeParts)
			{
				attributesMetaData.add(new DefaultAttributeMetaData(attributePart));
			}
			this.attributesMetaData = attributesMetaData;
		}
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

	public DefaultAttributeMetaData setDescription(String description)
	{
		this.description = description;
		return this;
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
		if (idAttribute)
		{
			readOnly = true;
		}

		return readOnly;
	}

	public DefaultAttributeMetaData setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
		return this;
	}

	@Override
	public String getDefaultValue()
	{
		return defaultValue;
	}

	public DefaultAttributeMetaData setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
		return this;
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
	public String getExpression()
	{
		return expression;
	}

	public DefaultAttributeMetaData setExpression(String expression)
	{
		this.expression = expression;
		return this;
	}

	@Override
	public Iterable<AttributeMetaData> getAttributeParts()
	{
		return this.attributesMetaData != null ? this.attributesMetaData : Collections.<AttributeMetaData> emptyList();
	}

	public void addAttributePart(AttributeMetaData attributePart)
	{
		if (this.attributesMetaData == null)
		{
			this.attributesMetaData = new ArrayList<AttributeMetaData>();
		}
		this.attributesMetaData.add(attributePart);
	}

	public void setAttributesMetaData(Iterable<AttributeMetaData> attributeParts)
	{
		this.attributesMetaData = Lists.newArrayList(attributeParts);
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
		if (idAttribute)
		{
			unique = true;
		}

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

	public DefaultAttributeMetaData setAggregateable(boolean aggregateable)
	{
		this.aggregateable = aggregateable;
		return this;
	}

	@Override
	public Range getRange()
	{
		return range;
	}

	public DefaultAttributeMetaData setRange(Range range)
	{
		this.range = range;
		return this;
	}

	@Override
	public List<String> getEnumOptions()
	{
		if (fieldType instanceof EnumField)
		{
			return ((EnumField) fieldType).getEnumOptions();
		}

		return null;
	}

	public DefaultAttributeMetaData setEnumOptions(List<String> enumOptions)
	{
		if (fieldType instanceof EnumField)
		{
			((EnumField) fieldType).setEnumOptions(enumOptions);
		}

		return this;
	}

	@Override
	public String getVisibleExpression()
	{
		return this.visibleExpression;
	}

	public DefaultAttributeMetaData setVisibleExpression(String visibleExpression)
	{
		this.visibleExpression = visibleExpression;
		return this;
	}

	@Override
	public String getValidationExpression()
	{
		return validationExpression;
	}

	public DefaultAttributeMetaData setValidationExpression(String validationExpression)
	{
		this.validationExpression = validationExpression;
		return this;
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

	@Override
	public boolean isSameAs(AttributeMetaData other)
	{
		if (this == other) return true;
		if (other == null) return false;

		if (isAggregateable() != other.isAggregateable()) return false;
		if (isAuto() != other.isAuto()) return false;
		if (getDescription() == null)
		{
			if (other.getDescription() != null) return false;
		}
		else if (!getDescription().equals(other.getDescription())) return false;
		if (getDataType() == null)
		{
			if (other.getDataType() != null) return false;
		}
		else
		{
			if (getDataType().getEnumType() != other.getDataType().getEnumType()) return false;
			if (getDataType().getEnumType() == FieldTypeEnum.ENUM)
			{
				if (((EnumField) getDataType()).getEnumOptions() == null)
				{
					if (((EnumField) other.getDataType()).getEnumOptions() != null) return false;
					if (!((EnumField) getDataType()).getEnumOptions()
							.equals(((EnumField) other.getDataType()).getEnumOptions()))
						return true;
				}
			}
		}
		if (isIdAtrribute() != other.isIdAtrribute()) return false;
		if (getLabel() == null)
		{
			if (other.getLabel() != null) return false;
		}
		else if (!getLabel().equals(other.getLabel())) return false;
		if (isLabelAttribute() != other.isLabelAttribute()) return false;
		if (isLookupAttribute() != other.isLookupAttribute()) return false;
		if (getName() == null)
		{
			if (other.getName() != null) return false;
		}
		else if (!getName().equals(other.getName())) return false;
		if (isNillable() != other.isNillable()) return false;

		if (getRange() == null)
		{
			if (other.getRange() != null) return false;
		}
		else if (!getRange().equals(other.getRange())) return false;
		if (isReadonly() != other.isReadonly()) return false;
		if (getRefEntity() == null)
		{
			if (other.getRefEntity() != null) return false;
		}
		else if (!getRefEntity().getName().equals(other.getRefEntity().getName())) return false;
		if (isUnique() != other.isUnique()) return false;
		if (isVisible() != other.isVisible()) return false;

		// attributeparts
		Iterator<AttributeMetaData> attributeParts = getAttributeParts().iterator();
		Iterator<AttributeMetaData> otherAttributeParts = other.getAttributeParts().iterator();
		Map<String, AttributeMetaData> otherAttributePartsMap = new HashMap<String, AttributeMetaData>();
		while (otherAttributeParts.hasNext())
		{
			AttributeMetaData otherAttributePart = otherAttributeParts.next();
			otherAttributePartsMap.put(otherAttributePart.getName(), otherAttributePart);
		}
		while (attributeParts.hasNext())
		{
			AttributeMetaData attributePart = attributeParts.next();
			if (!attributePart.isSameAs(otherAttributePartsMap.get(attributePart.getName())))
			{
				return false;
			}
			else
			{
				otherAttributePartsMap.remove(attributePart.getName());
			}
		}
		if (otherAttributePartsMap.size() > 0) return false;
		if (getVisibleExpression() == null)
		{
			if (other.getVisibleExpression() != null) return false;
		}
		else if (!getVisibleExpression().equals(other.getVisibleExpression())) return false;
		if (getValidationExpression() == null)
		{
			if (other.getValidationExpression() != null) return false;
		}
		else if (!getValidationExpression().equals(other.getValidationExpression())) return false;

		return true;
	}
}
