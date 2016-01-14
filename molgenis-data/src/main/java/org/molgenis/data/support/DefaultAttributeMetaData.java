package org.molgenis.data.support;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.AGGREGATEABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.AUTO;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DATA_TYPE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DEFAULT_VALUE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ENUM_OPTIONS;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.EXPRESSION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LOOKUP_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NILLABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.PARTS;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MAX;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MIN;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.READ_ONLY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.UNIQUE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VALIDATION_EXPRESSION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE_EXPRESSION;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeChangeListener;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Range;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.util.CaseInsensitiveLinkedHashMap;

/**
 * Default implementation of the AttributeMetaData interface
 * 
 */
public class DefaultAttributeMetaData implements AttributeMetaData
{
	private Map<String, AttributeChangeListener> changeListeners;

	private final String name;
	private FieldType fieldType;
	private String description;
	private final Map<String, String> descriptionByLanguageCode = new HashMap<>();
	private boolean nillable = true;
	private boolean readOnly = false;
	private String defaultValue = null;
	private boolean idAttribute = false;
	private boolean labelAttribute = false;
	private boolean lookupAttribute = false; // remove?
	private EntityMetaData refEntity;
	private String expression;
	private String label;// The default label
	private final Map<String, String> labelByLanguageCode = new HashMap<>();
	private boolean visible = true; // remove?
	private boolean unique = false;
	private boolean auto = false;
	private Map<String, AttributeMetaData> attributePartsMap;
	private boolean aggregateable = false;
	private Range range;
	private String visibleExpression;
	private String validationExpression;

	public DefaultAttributeMetaData(String name)
	{
		this(name, STRING);
	}

	public DefaultAttributeMetaData(String name, FieldTypeEnum fieldType)
	{
		this(name, MolgenisFieldTypes.getType(requireNonNull(fieldType).toString().toLowerCase()));
	}

	private DefaultAttributeMetaData(String name, FieldType fieldType)
	{
		this.name = requireNonNull(name);
		this.label = name;
		this.fieldType = requireNonNull(fieldType);
	}

	/**
	 * Copy constructor
	 * 
	 * @param attributeMetaData
	 */
	public DefaultAttributeMetaData(AttributeMetaData attributeMetaData)
	{
		this(attributeMetaData.getName(), attributeMetaData);
	}

	public DefaultAttributeMetaData(String newName, AttributeMetaData attributeMetaData)
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
		setLabel(attributeMetaData.getLabel());
		this.visible = attributeMetaData.isVisible();
		this.unique = attributeMetaData.isUnique();
		this.auto = attributeMetaData.isAuto();
		this.aggregateable = attributeMetaData.isAggregateable();
		this.range = attributeMetaData.getRange();
		this.visibleExpression = attributeMetaData.getVisibleExpression();
		this.validationExpression = attributeMetaData.getValidationExpression();
		addChangeListeners(attributeMetaData.getChangeListeners());

		// deep copy
		Iterable<AttributeMetaData> attributeParts = attributeMetaData.getAttributeParts();
		if (attributeParts != null)
		{
			Map<String, AttributeMetaData> attributePartsMap = new CaseInsensitiveLinkedHashMap<>();
			for (AttributeMetaData attributePart : attributeParts)
			{
				attributePartsMap.put(attributePart.getName(), new DefaultAttributeMetaData(attributePart));
			}
			this.attributePartsMap = attributePartsMap;
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
		fireChangeEvent(DESCRIPTION);
		return this;
	}

	@Override
	public String getDescription(String languageCode)
	{
		String description = descriptionByLanguageCode.get(languageCode);
		return description != null ? description : getDescription();
	}

	public DefaultAttributeMetaData setDescription(String languageCode, String description)
	{
		descriptionByLanguageCode.put(languageCode, description);
		fireChangeEvent(DESCRIPTION + '-' + languageCode);
		return this;
	}

	@Override
	public Set<String> getDescriptionLanguageCodes()
	{
		return Collections.unmodifiableSet(descriptionByLanguageCode.keySet());
	}

	@Override
	public FieldType getDataType()
	{
		return fieldType;
	}

	public DefaultAttributeMetaData setDataType(FieldType type)
	{
		this.fieldType = type;
		fireChangeEvent(DATA_TYPE);
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
		fireChangeEvent(NILLABLE);
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
		fireChangeEvent(READ_ONLY);
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
		fireChangeEvent(DEFAULT_VALUE);
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
		fireChangeEvent(ID_ATTRIBUTE);
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
		fireChangeEvent(LABEL_ATTRIBUTE);
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
		fireChangeEvent(REF_ENTITY);
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
		fireChangeEvent(EXPRESSION);
		return this;
	}

	@Override
	public Iterable<AttributeMetaData> getAttributeParts()
	{
		return this.attributePartsMap != null ? this.attributePartsMap.values() : Collections
				.<AttributeMetaData> emptyList();
	}

	@Override
	public AttributeMetaData getAttributePart(String attrName)
	{
		return attributePartsMap != null ? attributePartsMap.get(attrName) : null;
	}

	public void addAttributePart(AttributeMetaData attributePart)
	{
		if (this.attributePartsMap == null)
		{
			this.attributePartsMap = new CaseInsensitiveLinkedHashMap<>();
		}
		this.attributePartsMap.put(attributePart.getName(), attributePart);

		fireChangeEvent(PARTS);
	}

	public void setAttributesMetaData(Iterable<AttributeMetaData> attributeParts)
	{
		this.attributePartsMap = new CaseInsensitiveLinkedHashMap<>();
		attributeParts.forEach(attrPart -> {
			attributePartsMap.put(attrPart.getName(), attrPart);
		});
		fireChangeEvent(PARTS);
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	public DefaultAttributeMetaData setLabel(String label)
	{
		this.label = label;
		if (this.label == null)
		{
			this.label = this.name;
		}
		fireChangeEvent(LABEL);
		return this;
	}

	public DefaultAttributeMetaData setLabel(String languageCode, String label)
	{
		labelByLanguageCode.put(languageCode, label);
		fireChangeEvent(LABEL + '-' + languageCode);
		return this;
	}

	@Override
	public String getLabel(String languageCode)
	{
		String label = labelByLanguageCode.get(languageCode);
		return label != null ? label : getLabel();
	}

	@Override
	public Set<String> getLabelLanguageCodes()
	{
		return Collections.unmodifiableSet(labelByLanguageCode.keySet());
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	public DefaultAttributeMetaData setVisible(boolean visible)
	{
		this.visible = visible;
		fireChangeEvent(VISIBLE);
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
		fireChangeEvent(UNIQUE);
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
		fireChangeEvent(AUTO);
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
		fireChangeEvent(LOOKUP_ATTRIBUTE);
		return this;
	}

	@Override
	public String toString()
	{
		String result = "AttributeMetaData(name='" + this.getName() + "'";
		result += " dataType='" + getDataType() + "'";
		if (getRefEntity() != null) result += " refEntity='" + getRefEntity().getName() + "'";
		result += " description='" + getDescription() + "'";
		result += " fieldType='" + fieldType + "'";
		result += " nillable='" + nillable + "'";
		result += " readOnly='" + readOnly + "'";
		result += " defaultValue='" + defaultValue + "'";
		result += " idAttribute='" + idAttribute + "'";
		result += " labelAttribute='" + labelAttribute + "'";
		result += " lookupAttribute='" + lookupAttribute + "'";
		result += " expression='" + expression + "'";
		result += " label='" + label + "'";
		result += " visible='" + visible + "'";
		result += " unique='" + unique + "'";
		result += " visible='" + visible + "'";
		result += " auto='" + auto + "'";
		result += " attributesMetaData='" + attributePartsMap + "'";
		result += " aggregateable='" + aggregateable + "'";
		result += " range='" + range + "'";
		result += " visibleExpression='" + visibleExpression + "'";
		result += " validationExpression='" + validationExpression + "'";
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
		fireChangeEvent(AGGREGATEABLE);
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
		fireChangeEvent(RANGE_MIN);
		fireChangeEvent(RANGE_MAX);
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
		fireChangeEvent(ENUM_OPTIONS);
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
		fireChangeEvent(VISIBLE_EXPRESSION);
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
		fireChangeEvent(VALIDATION_EXPRESSION);
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
		if (other == null)
			return false;

		if (isAggregateable() != other.isAggregateable())
			return false;
		if (isAuto() != other.isAuto())
			return false;
		if (getDescription() == null)
		{
			if (other.getDescription() != null) {
				return false;
			}
		}
		else if (!getDescription().equals(other.getDescription()))
			return false;
		if (getDataType() == null)
		{
			if (other.getDataType() != null)
				return false;
		}
		else
		{
			if (getDataType().getEnumType() != other.getDataType().getEnumType())
				return false;
			if (getDataType().getEnumType() == FieldTypeEnum.ENUM)
			{
				if (((EnumField) getDataType()).getEnumOptions() == null)
				{
					if (((EnumField) other.getDataType()).getEnumOptions() != null)
						return false;
					if (!((EnumField) getDataType()).getEnumOptions().equals(
							((EnumField) other.getDataType()).getEnumOptions()))
						return true;
				}
			}
		}
		if (isIdAtrribute() != other.isIdAtrribute())
			return false;
		if (getLabel() == null)
		{
			if (other.getLabel() != null)
				return false;
		}
		else if (!getLabel().equals(other.getLabel()))
			return false;
		if (isLabelAttribute() != other.isLabelAttribute())
			return false;
		if (isLookupAttribute() != other.isLookupAttribute())
			return false;
		if (getName() == null)
		{
			if (other.getName() != null)
				return false;
		}
		else if (!getName().equals(other.getName()))
			return false;
		if (isNillable() != other.isNillable())
			return false;

		if (getRange() == null)
		{
			if (other.getRange() != null)
				return false;
		}
		else if (!getRange().equals(other.getRange()))
			return false;
		if (isReadonly() != other.isReadonly())
			return false;
		if (getRefEntity() == null)
		{
			if (other.getRefEntity() != null)
				return false;
		}
		else if (!getRefEntity().getName().equals(other.getRefEntity().getName()))
			return false;
		if (isUnique() != other.isUnique())
			return false;
		if (isVisible() != other.isVisible())
			return false;

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

	@Override
	public Collection<AttributeChangeListener> getChangeListeners()
	{
		if (changeListeners != null)
		{
			return changeListeners.values();
		}
		else
		{
			return Collections.emptySet();
		}
	}

	@Override
	public void addChangeListener(AttributeChangeListener changeListener)
	{
		if (changeListeners == null)
		{
			changeListeners = new HashMap<>();
		}
		changeListeners.put(changeListener.getId(), changeListener);
	}

	@Override
	public void addChangeListeners(Iterable<AttributeChangeListener> changeListeners)
	{
		changeListeners.forEach(this::addChangeListener);
	}

	@Override
	public void removeChangeListener(String changeListenerId)
	{
		if (changeListeners != null)
		{
			changeListeners.remove(changeListenerId);
		}
	}

	@Override
	public void removeChangeListeners()
	{
		changeListeners = null;
	}

	private void fireChangeEvent(String attrName)
	{
		if (changeListeners != null)
		{
			changeListeners.values().forEach(changeListener -> changeListener.onChange(attrName, this));
		}
	}

}
