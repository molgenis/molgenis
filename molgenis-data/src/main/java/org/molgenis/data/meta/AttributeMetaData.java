package org.molgenis.data.meta;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.removeAll;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.AGGREGATEABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.AUTO;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DATA_TYPE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DEFAULT_VALUE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ENUM_OPTIONS;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.EXPRESSION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.IDENTIFIER;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NAME;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NILLABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.PARTS;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MAX;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MIN;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.READ_ONLY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.TAGS;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.UNIQUE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VALIDATION_EXPRESSION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE_EXPRESSION;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.support.AttributeMetaDataUtils.getI18nAttributeName;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistrySingleton;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.fieldtypes.FieldType;

/**
 * Attribute defines the properties of an entity. Synonyms: feature, column, data item.
 */
public class AttributeMetaData extends AbstractEntity
{
	private final Entity entity;

	public AttributeMetaData(Entity entity)
	{
		this.entity = requireNonNull(entity);
		if (!entity.getEntityMetaData().getName().equals(ATTRIBUTE_META_DATA))
		{
			throw new IllegalArgumentException(
					format("Entity must be of type [%s] instead of [%s]", ATTRIBUTE_META_DATA,
							entity.getEntityMetaData().getName()));
		}
	}

	public AttributeMetaData(EntityMetaData attrMetaDataMetaData)
	{
		this.entity = new MapEntity(attrMetaDataMetaData);

		// FIXME use default value for this
		set(DATA_TYPE, toDataTypeString(MolgenisFieldTypes.getType(requireNonNull(STRING).toString().toLowerCase())));
		set(NILLABLE, true);
		set(AUTO, false);
		set(VISIBLE, true);
		set(AGGREGATEABLE, false);
		set(READ_ONLY, false);
		set(UNIQUE, false);
	}

	@Deprecated
	public AttributeMetaData(String name)
	{
		this(name, STRING);
	}

	@Deprecated
	public AttributeMetaData(String name, FieldTypeEnum fieldType)
	{
		this.entity = new MapEntity(getEntityMetaData());
		FieldType dataType = MolgenisFieldTypes.getType(requireNonNull(fieldType).toString().toLowerCase());
		set(NAME, name);
		set(DATA_TYPE, toDataTypeString(dataType));

		// FIXME use default value for this
		set(NILLABLE, true);
		set(AUTO, false);
		set(VISIBLE, true);
		set(AGGREGATEABLE, false);
		set(READ_ONLY, false);
		set(UNIQUE, false);
	}

	/**
	 * Copy-factory (instead of copy-constructor to avoid accidental method overloading to {@link #AttributeMetaData(Entity)})
	 *
	 * @param attr attribute
	 * @return deep copy of attribute
	 */
	public static AttributeMetaData newInstance(AttributeMetaData attr)
	{
		Entity entityCopy = MapEntity.newInstance(attr.entity);
		return new AttributeMetaData(entityCopy);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return SystemEntityMetaDataRegistrySingleton.INSTANCE.getSystemEntityMetaData(ATTRIBUTE_META_DATA);
	}

	@Override
	public Object get(String attributeName)
	{
		return entity.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		entity.set(attributeName, value);
	}

	@Override
	public void set(Entity values)
	{
		entity.set(values);
	}

	public String getIdentifier()
	{
		return getString(IDENTIFIER);
	}

	public AttributeMetaData setIdentifier(String identifier)
	{
		set(IDENTIFIER, identifier);
		return this;
	}

	/**
	 * Name of the attribute
	 *
	 * @return attribute name
	 */
	public String getName()
	{
		return getString(NAME);
	}

	public AttributeMetaData setName(String name)
	{
		set(NAME, name);
		return this;
	}

	/**
	 * Label of the attribute in the default language if set else returns name
	 *
	 * @return attribute label
	 */
	public String getLabel()
	{
		String label = getString(LABEL);
		return label != null ? label : getName();
	}

	/**
	 * Label of the attribute in the default language if set else returns name
	 *
	 * @return attribute label
	 */
	public String getLabel(String languageCode)
	{
		String i18nString = getString(getI18nAttributeName(LABEL, languageCode));
		return i18nString != null ? i18nString : getLabel();
	}

	public AttributeMetaData setLabel(String label)
	{
		set(LABEL, label);
		return this;
	}

	public AttributeMetaData setLabel(String languageCode, String label)
	{
		set(getI18nAttributeName(LABEL, languageCode), label);
		return this;
	}

	/**
	 * Description of the attribute
	 *
	 * @return attribute description or <tt>null</tt>
	 */
	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	/**
	 * Description of the attribute in the requested languages
	 *
	 * @return attribute description or <tt>null</tt>
	 */
	public String getDescription(String languageCode)
	{
		String i18nDescription = getString(getI18nAttributeName(DESCRIPTION, languageCode));
		return i18nDescription != null ? i18nDescription : getDescription();
	}

	public AttributeMetaData setDescription(String description)
	{
		set(DESCRIPTION, description);
		return this;
	}

	public AttributeMetaData setDescription(String languageCode, String description)
	{
		set(getI18nAttributeName(DESCRIPTION, languageCode), description);
		return this;
	}

	/**
	 * Data type of the attribute
	 *
	 * @return attribute data type
	 */
	public FieldType getDataType()
	{
		String dataTypeStr = getString(DATA_TYPE);
		return dataTypeStr != null ? MolgenisFieldTypes.getType(dataTypeStr) : null;
	}

	public AttributeMetaData setDataType(FieldType dataType)
	{
		set(DATA_TYPE, toDataTypeString(dataType));
		return this;
	}

	/**
	 * When getDataType=compound, get compound attribute parts
	 *
	 * @return Iterable of attributes or empty Iterable if no attribute parts exist
	 */
	public Iterable<AttributeMetaData> getAttributeParts()
	{
		return getEntities(PARTS, AttributeMetaData.class);
	}

	public AttributeMetaData setAttributeParts(Iterable<AttributeMetaData> parts)
	{
		set(PARTS, parts);
		return this;
	}

	/**
	 * When getDataType=xref/mref, get other end of xref
	 *
	 * @return referenced entity
	 */
	public EntityMetaData getRefEntity()
	{
		String refEntityName = getString(REF_ENTITY);
		if (refEntityName != null)
		{
			SystemEntityMetaData systemEntityMetaData = SystemEntityMetaDataRegistrySingleton.INSTANCE
					.getSystemEntityMetaData(refEntityName);
			if (systemEntityMetaData != null)
			{
				return systemEntityMetaData;
			}
			else
			{
				// FIXME get rid of static getApplicationContext reference
				return getApplicationContext().getBean(DataService.class)
						.findOneById(ENTITY_META_DATA, refEntityName, EntityMetaDataImpl.class);
			}
		}
		else
		{
			return null;
		}
	}

	public AttributeMetaData setRefEntity(EntityMetaData refEntity)
	{
		set(REF_ENTITY, refEntity != null ? refEntity.getName() : null);
		return this;
	}

	/**
	 * Expression used to compute this attribute.
	 *
	 * @return String representation of expression, in JSON format
	 */
	public String getExpression()
	{
		return getString(EXPRESSION);
	}

	public AttributeMetaData setExpression(String expression)
	{
		set(EXPRESSION, expression);
		return this;
	}

	/**
	 * Whether attribute has not null constraint
	 *
	 * @return <tt>true</tt> if this attribute is nillable
	 */
	public boolean isNillable()
	{
		return requireNonNull(getBoolean(NILLABLE));
	}

	public AttributeMetaData setNillable(boolean nillable)
	{
		set(NILLABLE, nillable);
		return this;
	}

	/**
	 * When true the attribute is automatically assigned a value when persisted (for example the current date)
	 *
	 * @return <tt>true</tt> if this attribute is automatically assigned
	 */
	public boolean isAuto()
	{
		return requireNonNull(getBoolean(AUTO));
	}

	public AttributeMetaData setAuto(boolean auto)
	{
		set(AUTO, auto);
		return this;
	}

	/**
	 * Should this attribute be visible to the user?
	 *
	 * @return <tt>true</tt> if this attribute is visible
	 */
	public boolean isVisible()
	{
		return requireNonNull(getBoolean(VISIBLE));
	}

	public AttributeMetaData setVisible(boolean visible)
	{
		set(VISIBLE, visible);
		return this;
	}

	/**
	 * Whether this attribute can be used to aggregate on. Default only attributes of type 'BOOL', 'XREF' and
	 * 'CATEGORICAL' are aggregatable.
	 *
	 * @return <tt>true</tt> if this attribute is aggregatable
	 */
	public boolean isAggregatable()
	{
		return requireNonNull(getBoolean(AGGREGATEABLE));
	}

	public AttributeMetaData setAggregatable(boolean aggregatable)
	{
		set(AGGREGATEABLE, aggregatable);
		return this;
	}

	/**
	 * For enum fields returns the possible enum values
	 *
	 * @return enum values
	 */
	public List<String> getEnumOptions()
	{
		String enumOptionsStr = getString(ENUM_OPTIONS);
		return enumOptionsStr != null ? asList(enumOptionsStr.split(",")) : emptyList();
	}

	public AttributeMetaData setEnumOptions(Class<? extends Enum<?>> e)
	{
		return setEnumOptions(stream(e.getEnumConstants()).map(Enum::name).collect(toList()));
	}

	public AttributeMetaData setEnumOptions(List<String> enumOptions)
	{
		set(ENUM_OPTIONS, toEnumOptionsString(enumOptions));
		return this;
	}

	public Long getRangeMin()
	{
		return getLong(RANGE_MIN);
	}

	public AttributeMetaData setRangeMin(Long rangeMin)
	{
		set(RANGE_MIN, rangeMin);
		return this;
	}

	public Long getRangeMax()
	{
		return getLong(RANGE_MAX);
	}

	public AttributeMetaData setRangeMax(Long rangeMax)
	{
		set(RANGE_MAX, rangeMax);
		return this;
	}

	/**
	 * Whether attribute is readonly
	 *
	 * @return <tt>true</tt> if this attribute is read-only
	 */
	public boolean isReadOnly()
	{
		return requireNonNull(getBoolean(READ_ONLY));
	}

	public AttributeMetaData setReadOnly(boolean readOnly)
	{
		set(READ_ONLY, readOnly);
		return this;
	}

	/**
	 * Whether attribute should have an unique value for each entity
	 *
	 * @return <tt>true</tt> if this attribute is unique
	 */
	public boolean isUnique()
	{
		return requireNonNull(getBoolean(UNIQUE));
	}

	public AttributeMetaData setUnique(boolean unique)
	{
		set(UNIQUE, unique);
		return this;
	}

	// FIXME add getter/setter for tags

	/**
	 * Javascript expression to determine at runtime if the attribute must be visible or not in the form
	 *
	 * @return expression
	 */
	public String getVisibleExpression()
	{
		return getString(VISIBLE_EXPRESSION);
	}

	public AttributeMetaData setVisibleExpression(String visibleExpression)
	{
		set(VISIBLE_EXPRESSION, visibleExpression);
		return this;
	}

	/**
	 * Javascript expression to validate the value of the attribute
	 */
	public String getValidationExpression()
	{
		return getString(VALIDATION_EXPRESSION);
	}

	public AttributeMetaData setValidationExpression(String validationExpression)
	{
		set(VALIDATION_EXPRESSION, validationExpression);
		return this;
	}

	/**
	 * Default value expression
	 *
	 * @return attribute default value
	 */
	public String getDefaultValue()
	{
		return getString(DEFAULT_VALUE);
	}

	public AttributeMetaData setDefaultValue(String defaultValue)
	{
		set(DEFAULT_VALUE, defaultValue);
		return this;
	}

	/**
	 * For int and long fields, the value must be between min and max (included) of the range
	 *
	 * @return attribute value range
	 */
	public Range getRange()
	{
		return new Range(getRangeMin(), getRangeMax());
	}

	public AttributeMetaData setRange(Range range)
	{
		set(RANGE_MIN, range.getMin());
		set(RANGE_MAX, range.getMax());
		return this;
	}

	/**
	 * Get attribute part by name (case insensitive), returns null if not found
	 *
	 * @param attrName attribute name (case insensitive)
	 * @return attribute or null
	 */
	public AttributeMetaData getAttributePart(String attrName)
	{
		Iterable<AttributeMetaData> attrParts = getEntities(PARTS, AttributeMetaData.class);
		return stream(attrParts.spliterator(), false).filter(attrPart -> attrPart.getName().equals(attrName))
				.findFirst().orElse(null);
	}

	public void addAttributePart(AttributeMetaData attrPart)
	{
		Iterable<AttributeMetaData> attrParts = getEntities(PARTS, AttributeMetaData.class);
		set(PARTS, concat(attrParts, singletonList(attrPart)));
	}

	/**
	 * Get all tags for this attribute
	 *
	 * @return attribute tags
	 */
	public Iterable<Tag> getTags()
	{
		return getEntities(TAGS, Tag.class);
	}

	/**
	 * Set tags for this attribute
	 *
	 * @param tags attribute tags
	 * @return this entity
	 */
	public AttributeMetaData setTags(Iterable<Tag> tags)
	{
		set(TAGS, tags);
		return this;
	}

	/**
	 * Add a tag for this attribute
	 *
	 * @param tag attribute tag
	 */
	public void addTag(Tag tag)
	{
		entity.set(TAGS, concat(getTags(), singletonList(tag)));
	}

	/**
	 * Add a tag for this attribute
	 *
	 * @param tag attribute tag
	 */
	public void removeTag(Tag tag)
	{
		Iterable<Tag> tags = getTags();
		removeAll(tags, singletonList(tag));
		entity.set(TAGS, tag);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AttributeMetaData that = (AttributeMetaData) o;

		return entity.equals(that.entity);

	}

	@Override
	public int hashCode()
	{
		return entity.hashCode();
	}

	@Override
	public String toString()
	{
		return "AttributeMetaData{" +
				"entity=" + entity +
				'}';
	}

	private static String toDataTypeString(FieldType dataType)
	{
		return dataType != null ? dataType.toString() : null;
	}

	private static String toEnumOptionsString(List<String> enumOptions)
	{
		return !enumOptions.isEmpty() ? enumOptions.stream().collect(joining(",")) : null;
	}
}
