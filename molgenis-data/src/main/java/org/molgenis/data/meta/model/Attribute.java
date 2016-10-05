package org.molgenis.data.meta.model;

import com.google.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.support.StaticEntity;

import java.util.List;

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
import static org.molgenis.data.meta.model.AttributeMetadata.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeCopyMode.DEEP_COPY_ATTRS;
import static org.molgenis.data.support.AttributeUtils.getI18nAttributeName;
import static org.molgenis.data.support.EntityMetaDataUtils.isReferenceType;

/**
 * Attribute defines the properties of an entity. Synonyms: feature, column, data item.
 */
public class Attribute extends StaticEntity
{
	private transient AttributeType cachedDataType;

	public Attribute(Entity entity)
	{
		super(entity);
	}

	/**
	 * Creates a new attribute. Normally called by its {@link AttributeFactory entity factory}.
	 *
	 * @param entityMeta attribute meta data
	 */
	public Attribute(EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
	}

	/**
	 * Creates a new attribute with the given identifier. Normally called by its {@link AttributeFactory entity factory}.
	 *
	 * @param attrId     attribute identifier (not the attribute name)
	 * @param entityMeta attribute meta data
	 */
	public Attribute(String attrId, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
		setIdentifier(attrId);
	}

	/**
	 * Copy-factory (instead of copy-constructor to avoid accidental method overloading to
	 * {@link #Attribute(EntityMetaData)}). Creates a copy of attribute with a shallow copy of referenced
	 * entity and tags.
	 *
	 * @param attrMeta     attribute
	 * @param attrCopyMode attribute copy mode that defines whether to deep-copy or shallow-copy attribute parts
	 * @return deep copy of attribute
	 */
	public static Attribute newInstance(Attribute attrMeta, AttributeCopyMode attrCopyMode)
	{
		Attribute attrMetaCopy = new Attribute(attrMeta.getEntityMetaData()); // do not deep-copy
		attrMetaCopy.setIdentifier(attrMeta.getIdentifier());
		attrMetaCopy.setName(attrMeta.getName());
		attrMetaCopy.setDataType(attrMeta.getDataType());
		attrMetaCopy.setRefEntity(attrMeta.getRefEntity()); // do not deep-copy
		attrMetaCopy.setMappedBy(attrMeta.getMappedBy()); // do not deep-copy
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
		if (attrCopyMode == DEEP_COPY_ATTRS)
		{
			attrMetaCopy.setAttributeParts(stream(attrMeta.getAttributeParts().spliterator(), false)
					.map(attr -> Attribute.newInstance(attr, attrCopyMode))
					.map(attrCopy -> attrCopy.setIdentifier(null)).collect(toList()));
		}
		else
		{
			attrMetaCopy.setAttributeParts(Lists.newArrayList(attrMeta.getAttributeParts()));
		}

		attrMetaCopy.setTags(Lists.newArrayList(attrMeta.getTags())); // do not deep-copy
		attrMetaCopy.setVisibleExpression(attrMeta.getVisibleExpression());
		attrMetaCopy.setDefaultValue(attrMeta.getDefaultValue());
		return attrMetaCopy;
	}

	public String getIdentifier()
	{
		return getString(IDENTIFIER);
	}

	public Attribute setIdentifier(String identifier)
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

	public Attribute setName(String name)
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

	public Attribute setLabel(String label)
	{
		set(LABEL, label);
		return this;
	}

	public Attribute setLabel(String languageCode, String label)
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

	public Attribute setDescription(String description)
	{
		set(DESCRIPTION, description);
		return this;
	}

	public Attribute setDescription(String languageCode, String description)
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
		return getCachedDataType();
	}

	public Attribute setDataType(AttributeType dataType)
	{
		invalidateCachedDataType();

		set(DATA_TYPE, AttributeType.getValueString(dataType));
		return this;
	}

	/**
	 * When getDataType=compound, get compound attribute parts
	 *
	 * @return Iterable of attributes or empty Iterable if no attribute parts exist
	 */
	public Iterable<Attribute> getAttributeParts()
	{
		return getEntities(PARTS, Attribute.class);
	}

	public Attribute setAttributeParts(Iterable<Attribute> parts)
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

	public Attribute setRefEntity(EntityMetaData refEntity)
	{
		set(REF_ENTITY, refEntity);
		return this;
	}

	public Attribute getMappedBy()
	{
		return getEntity(MAPPED_BY, Attribute.class);
	}

	public Attribute setMappedBy(Attribute mappedByAttr)
	{
		set(MAPPED_BY, mappedByAttr);
		return this;
	}

	/**
	 * Indicates if this attribute is the one-to-many back-reference of a bidirectionally navigable relationship.
	 */
	public boolean isMappedBy()
	{
		return getMappedBy() != null;
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

	public Attribute setExpression(String expression)
	{
		set(EXPRESSION, expression);
		return this;
	}

	/**
	 * Wheter attribute has an expression or not
	 *
	 * @return true if attribute has expression
	 */
	public boolean hasExpression()
	{
		return getExpression() != null;
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

	public Attribute setNillable(boolean nillable)
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

	public Attribute setAuto(boolean auto)
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

	public Attribute setVisible(boolean visible)
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
		return requireNonNull(getBoolean(AGGREGATABLE));
	}

	public Attribute setAggregatable(boolean aggregatable)
	{
		set(AGGREGATABLE, aggregatable);
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

	public Attribute setEnumOptions(Class<? extends Enum<?>> e)
	{
		return setEnumOptions(stream(e.getEnumConstants()).map(Enum::name).collect(toList()));
	}

	public Attribute setEnumOptions(List<String> enumOptions)
	{
		set(ENUM_OPTIONS, toEnumOptionsString(enumOptions));
		return this;
	}

	public Long getRangeMin()
	{
		return getLong(RANGE_MIN);
	}

	public Attribute setRangeMin(Long rangeMin)
	{
		set(RANGE_MIN, rangeMin);
		return this;
	}

	public Long getRangeMax()
	{
		return getLong(RANGE_MAX);
	}

	public Attribute setRangeMax(Long rangeMax)
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

	public Attribute setReadOnly(boolean readOnly)
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

	public Attribute setUnique(boolean unique)
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

	public Attribute setVisibleExpression(String visibleExpression)
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

	public Attribute setValidationExpression(String validationExpression)
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

	public Attribute setDefaultValue(String defaultValue)
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

	public Attribute setRange(Range range)
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
	public Attribute getAttributePart(String attrName)
	{
		Iterable<Attribute> attrParts = getEntities(PARTS, Attribute.class);
		return stream(attrParts.spliterator(), false).filter(attrPart -> attrPart.getName().equals(attrName))
				.findFirst().orElse(null);
	}

	public void addAttributePart(Attribute attrPart)
	{
		Iterable<Attribute> attrParts = getEntities(PARTS, Attribute.class);
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
	public Attribute setTags(Iterable<Tag> tags)
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

	private AttributeType getCachedDataType()
	{
		if (cachedDataType == null)
		{
			String dataTypeStr = getString(DATA_TYPE);
			cachedDataType = dataTypeStr != null ? AttributeType.toEnum(dataTypeStr) : null;
		}
		return cachedDataType;
	}

	private void invalidateCachedDataType()
	{
		cachedDataType = null;
	}

	@Override
	public String toString()
	{
		return "Attribute{" + "name=" + getName() + '}';
	}

	/**
	 * For a reference type attribute, searches the referenced entity for its inversed attribute.
	 * This is the one-to-many attribute that has "mappedBy" set to this attribute.
	 * Returns null if this is not a reference type attribute, or no inverse attribute exists.
	 */
	public Attribute getInversedBy()
	{
		// FIXME besides checking mappedBy attr name also check attr.getRefEntity().getName
		if (isReferenceType(this))
		{
			return stream(getRefEntity().getAtomicAttributes().spliterator(), false)
					.filter(Attribute::isMappedBy)
					.filter(attr -> getName().equals(attr.getMappedBy().getName())).findFirst().orElse(null);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Determines if this is a reference type attribute whose refEntity has an attribute that has mappedBy set to this
	 * attribute.
	 */
	public boolean isInversedBy()
	{
		return getInversedBy() != null;
	}
}
