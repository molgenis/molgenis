package org.molgenis.data.meta.model;

import com.google.common.collect.Maps;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.support.StaticEntity;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.removeAll;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.*;
import static org.molgenis.data.support.AttributeMetaDataUtils.getI18nAttributeName;

/**
 * Attribute defines the properties of an entity. Synonyms: feature, column, data item.
 */
public class AttributeMetaData extends StaticEntity
{
	public AttributeMetaData(Entity entity)
	{
		super(entity);
	}

	/**
	 * Creates a new attribute. Normally called by its {@link AttributeMetaDataFactory entity factory}.
	 *
	 * @param entityMeta attribute meta data
	 */
	public AttributeMetaData(EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
	}

	/**
	 * Creates a new attribute with the given identifier. Normally called by its {@link AttributeMetaDataFactory entity factory}.
	 *
	 * @param attrId     attribute identifier (not the attribute name)
	 * @param entityMeta attribute meta data
	 */
	public AttributeMetaData(String attrId, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
		setIdentifier(attrId);
	}

	/**
	 * Copy-factory (instead of copy-constructor to avoid accidental method overloading to {@link #AttributeMetaData(EntityMetaData)})
	 *
	 * @param attrMeta attribute
	 * @return deep copy of attribute
	 */
	public static AttributeMetaData newInstance(AttributeMetaData attrMeta)
	{
		return newInstance(attrMeta, Maps.newConcurrentMap());
	}

	/**
	 * Copy-factory (instead of copy-constructor to avoid accidental method overloading to {@link #AttributeMetaData(EntityMetaData)})
	 *
	 * @param attrMeta attribute
	 * @return deep copy of attribute
	 */
	static AttributeMetaData newInstance(AttributeMetaData attrMeta, Map<String, EntityMetaData> copiedEntityMetaData)
	{
		EntityMetaData entityMeta = attrMeta.getEntityMetaData();
		AttributeMetaData attrMetaCopy = new AttributeMetaData(entityMeta);
		attrMetaCopy.setIdentifier(attrMeta.getIdentifier());
		attrMetaCopy.setName(attrMeta.getName());
		attrMetaCopy.setDataType(attrMeta.getDataType());
		EntityMetaData refEntity = attrMeta.getRefEntity();

		if (refEntity != null)
		{
			attrMetaCopy.setRefEntity(copiedEntityMetaData.containsKey(refEntity.getName()) ? copiedEntityMetaData
					.get(refEntity.getName()) : EntityMetaData.newInstance(refEntity, copiedEntityMetaData));
		}

		attrMetaCopy.setExpression(attrMeta.getExpression());
		attrMetaCopy.setNillable(attrMeta.isNillable());
		attrMetaCopy.setAuto(attrMeta.isAuto());
		attrMetaCopy.setLabel(attrMeta.getLabel());
		attrMetaCopy.setDescription(attrMeta.getDescription());
		attrMetaCopy.setAggregatable(attrMeta.isAggregatable());
		attrMetaCopy.setEnumOptions(attrMeta.getEnumOptions());
		attrMetaCopy.setRangeMin(attrMeta.getRangeMin());
		attrMetaCopy.setRangeMax(attrMeta.getRangeMax());
		attrMetaCopy.setReadOnly(attrMeta.isReadOnly());
		attrMetaCopy.setUnique(attrMeta.isUnique());
		Iterable<AttributeMetaData> attrParts = attrMeta.getAttributeParts();
		attrMetaCopy.setAttributeParts(
				stream(attrParts.spliterator(), false).map(e -> newInstance(e, copiedEntityMetaData))
						.collect(toList()));
		Iterable<Tag> tags = attrMeta.getTags();
		attrMetaCopy.setTags(stream(tags.spliterator(), false).map(Tag::newInstance).collect(toList()));
		attrMetaCopy.setVisibleExpression(attrMeta.getVisibleExpression());
		attrMetaCopy.setDefaultValue(attrMeta.getDefaultValue());
		return attrMetaCopy;
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
	public AttributeType getDataType()
	{
		String dataTypeStr = getString(DATA_TYPE);
		return dataTypeStr != null ? AttributeType.toEnum(dataTypeStr) : null;
	}

	public AttributeMetaData setDataType(AttributeType dataType)
	{
		set(DATA_TYPE, AttributeType.getValueString(dataType));
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
		return getEntity(REF_ENTITY, EntityMetaData.class);
	}

	public AttributeMetaData setRefEntity(EntityMetaData refEntity)
	{
		set(REF_ENTITY, refEntity);
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
		Long rangeMin = getRangeMin();
		Long rangeMax = getRangeMax();
		return rangeMin != null || rangeMax != null ? new Range(rangeMin, rangeMax) : null;
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
		set(TAGS, concat(getTags(), singletonList(tag)));
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
		set(TAGS, tag);
	}

	public void setDefaultValues()
	{
		setDataType(STRING);
		setNillable(true);
		setAuto(false);
		setVisible(true);
		setAggregatable(false);
		setReadOnly(false);
		setUnique(false);
	}

	private static String toEnumOptionsString(List<String> enumOptions)
	{
		return !enumOptions.isEmpty() ? enumOptions.stream().collect(joining(",")) : null;
	}
}
