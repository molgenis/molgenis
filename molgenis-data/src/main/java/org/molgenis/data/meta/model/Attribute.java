package org.molgenis.data.meta.model;

import com.google.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.i18n.Labeled;

import javax.annotation.Nullable;
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
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.AttributeMetadata.*;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.DEEP_COPY_ATTRS;
import static org.molgenis.data.support.AttributeUtils.getI18nAttributeName;
import static org.molgenis.data.support.EntityTypeUtils.isReferenceType;

/**
 * Attribute defines the properties of an entity. Synonyms: feature, column, data item.
 */
public class Attribute extends StaticEntity implements Labeled
{
	private transient AttributeType cachedDataType;

	public Attribute(Entity entity)
	{
		super(entity);
	}

	/**
	 * Creates a new attribute. Normally called by its {@link AttributeFactory entity factory}.
	 *
	 * @param entityType attribute meta data
	 */
	public Attribute(EntityType entityType)
	{
		super(entityType);
		setDefaultValues();
	}

	/**
	 * Creates a new attribute with the given identifier. Normally called by its {@link AttributeFactory entity factory}.
	 *
	 * @param attrId     attribute identifier (not the attribute name)
	 * @param entityType attribute meta data
	 */
	public Attribute(String attrId, EntityType entityType)
	{
		super(entityType);
		setDefaultValues();
		setIdentifier(attrId);
	}

	/**
	 * Copy-factory (instead of copy-constructor to avoid accidental method overloading to
	 * {@link #Attribute(EntityType)}). Creates a copy of attribute with a shallow copy of referenced
	 * entity and tags.
	 *
	 * @param attrMeta     attribute
	 * @param attrCopyMode attribute copy mode that defines whether to deep-copy or shallow-copy attribute parts
	 * @param attrFactory  attribute factory used to create new attributes in deep-copy mode
	 * @return shallow or deep copy of attribute
	 */
	public static Attribute newInstance(Attribute attrMeta, AttributeCopyMode attrCopyMode,
			AttributeFactory attrFactory)
	{
		Attribute attrMetaCopy = attrFactory.create(); // create new attribute with unique identifier
		attrMetaCopy.setName(attrMeta.getName());
		attrMetaCopy.setEntity(attrMeta.getEntity());
		attrMetaCopy.setSequenceNumber(attrMeta.getSequenceNumber());
		attrMetaCopy.setDataType(attrMeta.getDataType());
		attrMetaCopy.setIdAttribute(attrMeta.isIdAttribute());
		attrMetaCopy.setLabelAttribute(attrMeta.isLabelAttribute());
		attrMetaCopy.setLookupAttributeIndex(attrMeta.getLookupAttributeIndex());
		attrMetaCopy.setRefEntity(attrMeta.getRefEntity()); // do not deep-copy
		attrMetaCopy.setMappedBy(attrMeta.getMappedBy()); // do not deep-copy
		attrMetaCopy.setOrderBy(attrMeta.getOrderBy());
		attrMetaCopy.setExpression(attrMeta.getExpression());
		attrMetaCopy.setNillable(attrMeta.isNillable());
		attrMetaCopy.setAuto(attrMeta.isAuto());
		attrMetaCopy.setVisible(attrMeta.isVisible());
		attrMetaCopy.setLabel(attrMeta.getLabel());
		attrMetaCopy.setDescription(attrMeta.getDescription());
		attrMetaCopy.setAggregatable(attrMeta.isAggregatable());
		attrMetaCopy.setEnumOptions(attrMeta.getEnumOptions());
		attrMetaCopy.setRangeMin(attrMeta.getRangeMin());
		attrMetaCopy.setRangeMax(attrMeta.getRangeMax());
		attrMetaCopy.setReadOnly(attrMeta.isReadOnly());
		attrMetaCopy.setUnique(attrMeta.isUnique());
		Attribute parentAttr = attrMeta.getParent();
		if (attrCopyMode == DEEP_COPY_ATTRS)
		{
			attrMetaCopy.setParent(
					parentAttr != null ? Attribute.newInstance(parentAttr, attrCopyMode, attrFactory) : null);
		}
		else
		{
			attrMetaCopy.setParent(parentAttr);
		}

		attrMetaCopy.setTags(Lists.newArrayList(attrMeta.getTags())); // do not deep-copy
		attrMetaCopy.setNullableExpression(attrMeta.getNullableExpression());
		attrMetaCopy.setValidationExpression(attrMeta.getValidationExpression());
		attrMetaCopy.setVisibleExpression(attrMeta.getVisibleExpression());
		attrMetaCopy.setDefaultValue(attrMeta.getDefaultValue());
		return attrMetaCopy;
	}

	public String getIdentifier()
	{
		return getString(ID);
	}

	public Attribute setIdentifier(String identifier)
	{
		set(ID, identifier);
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
	 * Attribute sequence number that determines attribute order within an entity
	 *
	 * @return attribute sequence number
	 */
	public Integer getSequenceNumber()
	{
		return getInt(SEQUENCE_NR);
	}

	public Attribute setSequenceNumber(int seqNr)
	{
		set(SEQUENCE_NR, seqNr);
		return this;
	}

	public EntityType getEntity()
	{
		return getEntity(ENTITY, EntityType.class);
	}

	public Attribute setEntity(EntityType entityMeta)
	{
		set(ENTITY, entityMeta);
		return this;
	}

	public boolean isIdAttribute()
	{
		Boolean isIdAttr = getBoolean(IS_ID_ATTRIBUTE);
		return isIdAttr != null && isIdAttr;
	}

	public Attribute setIdAttribute(Boolean isIdAttr)
	{
		set(IS_ID_ATTRIBUTE, isIdAttr);
		if (isIdAttr != null && isIdAttr)
		{
			setReadOnly(true);
			setUnique(true);
			setNillable(false);
		}
		return this;
	}

	public boolean isLabelAttribute()
	{
		Boolean isLabelAttr = getBoolean(IS_LABEL_ATTRIBUTE);
		return isLabelAttr != null && isLabelAttr;
	}

	public Attribute setLabelAttribute(Boolean isLabelAttr)
	{
		set(IS_LABEL_ATTRIBUTE, isLabelAttr);
		return this;
	}

	@Nullable
	public Integer getLookupAttributeIndex()
	{
		return getInt(LOOKUP_ATTRIBUTE_INDEX);
	}

	public Attribute setLookupAttributeIndex(Integer lookupAttrIdx)
	{
		set(LOOKUP_ATTRIBUTE_INDEX, lookupAttrIdx);
		return this;
	}

	/**
	 * Label of the attribute in the default language if set else returns name
	 *
	 * @return attribute label
	 */
	@Nullable
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
	@Nullable
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
	@Nullable
	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	/**
	 * Description of the attribute in the requested languages
	 *
	 * @return attribute description or <tt>null</tt>
	 */
	@Nullable
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

		set(TYPE, AttributeType.getValueString(dataType));
		return this;
	}

	@Nullable
	public Boolean getCascadeDelete()
	{
		return getBoolean(IS_CASCADE_DELETE);
	}

	public Attribute setCascadeDelete(Boolean isCascadeDelete)
	{
		set(IS_CASCADE_DELETE, isCascadeDelete);
		return this;
	}

	/**
	 * When getDataType=compound, get compound attribute parts
	 *
	 * @return Iterable of attributes or empty Iterable if no attribute parts exist
	 */
	public Iterable<Attribute> getChildren()
	{
		return getEntities(CHILDREN, Attribute.class);
	}

	/**
	 * When getDataType=xref/mref, get other end of xref
	 *
	 * @return referenced entity
	 */
	@Nullable
	public EntityType getRefEntity()
	{
		return getEntity(REF_ENTITY_TYPE, EntityType.class);
	}

	public Attribute setRefEntity(EntityType refEntity)
	{
		set(REF_ENTITY_TYPE, refEntity);
		return this;
	}

	@Nullable
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

	@Nullable
	public Sort getOrderBy()
	{
		String orderByStr = getString(ORDER_BY);
		return orderByStr != null ? Sort.parse(orderByStr) : null;
	}

	public Attribute setOrderBy(Sort sort)
	{
		String orderByStr = sort != null ? sort.toSortString() : null;
		set(ORDER_BY, orderByStr);
		return this;
	}

	/**
	 * Expression used to compute this attribute.
	 *
	 * @return String representation of expression, in JSON format
	 */
	@Nullable
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
		return requireNonNull(getBoolean(IS_NULLABLE));
	}

	public Attribute setNillable(boolean nillable)
	{
		set(IS_NULLABLE, nillable);
		return this;
	}

	/**
	 * When true the attribute is automatically assigned a value when persisted (for example the current date)
	 *
	 * @return <tt>true</tt> if this attribute is automatically assigned
	 */
	public boolean isAuto()
	{
		return requireNonNull(getBoolean(IS_AUTO));
	}

	public Attribute setAuto(boolean auto)
	{
		set(IS_AUTO, auto);
		return this;
	}

	/**
	 * Should this attribute be visible to the user?
	 *
	 * @return <tt>true</tt> if this attribute is visible
	 */
	public boolean isVisible()
	{
		return requireNonNull(getBoolean(IS_VISIBLE));
	}

	public Attribute setVisible(boolean visible)
	{
		set(IS_VISIBLE, visible);
		return this;
	}

	/**
	 * Whether this attribute can be used to aggregate on. Default only attributes of type 'BOOL', 'XREF' and
	 * 'CATEGORICAL' are isAggregatable.
	 *
	 * @return <tt>true</tt> if this attribute is isAggregatable
	 */
	public boolean isAggregatable()
	{
		return requireNonNull(getBoolean(IS_AGGREGATABLE));
	}

	public Attribute setAggregatable(boolean isAggregatable)
	{
		set(IS_AGGREGATABLE, isAggregatable);
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

	@Nullable
	public Long getRangeMin()
	{
		return getLong(RANGE_MIN);
	}

	public Attribute setRangeMin(Long rangeMin)
	{
		set(RANGE_MIN, rangeMin);
		return this;
	}

	@Nullable
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
		return requireNonNull(getBoolean(IS_READ_ONLY));
	}

	public Attribute setReadOnly(boolean readOnly)
	{
		set(IS_READ_ONLY, readOnly);
		return this;
	}

	/**
	 * Whether attribute should have an unique value for each entity
	 *
	 * @return <tt>true</tt> if this attribute is unique
	 */
	public boolean isUnique()
	{
		return requireNonNull(getBoolean(IS_UNIQUE));
	}

	public Attribute setUnique(boolean unique)
	{
		set(IS_UNIQUE, unique);
		return this;
	}

	/**
	 * JavaScript expression to determine at runtime if the attribute value is required
	 *
	 * @return expression
	 */
	@Nullable
	public String getNullableExpression()
	{
		return getString(NULLABLE_EXPRESSION);
	}

	public Attribute setNullableExpression(String nullableExpression)
	{
		set(NULLABLE_EXPRESSION, nullableExpression);
		return this;
	}

	/**
	 * JavaScript expression to determine at runtime if the attribute must be visible or not in the form
	 *
	 * @return expression
	 */
	@Nullable
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
	 * JavaScript expression to validate the value of the attribute
	 */
	@Nullable
	public String getValidationExpression()
	{
		return getString(VALIDATION_EXPRESSION);
	}

	public Attribute setValidationExpression(String validationExpression)
	{
		set(VALIDATION_EXPRESSION, validationExpression);
		return this;
	}

	public boolean hasDefaultValue()
	{
		return getDefaultValue() != null;
	}

	/**
	 * Default value expression
	 *
	 * @return attribute default value
	 */
	@Nullable
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

	@Nullable
	public Attribute getParent()
	{
		return getEntity(PARENT, Attribute.class);
	}

	public Attribute setParent(Attribute parentAttr)
	{
		Attribute currentParent = getParent();
		if (currentParent != null)
		{
			currentParent.removeChild(this);
		}
		set(PARENT, parentAttr);

		if (parentAttr != null)
		{
			parentAttr.addChild(this);
		}
		return this;
	}

	/**
	 * Get attribute part by name (case insensitive), returns null if not found
	 *
	 * @param attrName attribute name (case insensitive)
	 * @return attribute or null
	 */
	public Attribute getChild(String attrName)
	{
		Iterable<Attribute> attrParts = getEntities(CHILDREN, Attribute.class);
		return stream(attrParts.spliterator(), false).filter(attrPart -> attrPart.getName().equals(attrName))
													 .findFirst()
													 .orElse(null);
	}

	void addChild(Attribute attrPart)
	{
		Iterable<Attribute> attrParts = getEntities(CHILDREN, Attribute.class);
		set(CHILDREN, concat(attrParts, singletonList(attrPart)));
	}

	void removeChild(Attribute attrPart)
	{
		Iterable<Attribute> attrParts = getEntities(CHILDREN, Attribute.class);
		set(CHILDREN, stream(attrParts.spliterator(), false).filter(attr -> !attr.getName().equals(attrPart.getName()))
															.collect(toList()));
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
			String dataTypeStr = getString(TYPE);
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
		return "Attribute{" + "name=" + getName() + " id=" + getIdValue() + '}';
	}

	/**
	 * For a reference type attribute, searches the referenced entity for its inversed attribute.
	 * This is the one-to-many attribute that has "mappedBy" set to this attribute.
	 * Returns null if this is not a reference type attribute, or no inverse attribute exists.
	 */
	public Attribute getInversedBy()
	{
		// FIXME besides checking mappedBy attr name also check attr.getRefEntity().getFullyQualifiedName
		if (isReferenceType(this))
		{
			return stream(getRefEntity().getAtomicAttributes().spliterator(), false).filter(Attribute::isMappedBy)
																					.filter(attr -> getName().equals(
																							attr.getMappedBy()
																								.getName()))
																					.findFirst()
																					.orElse(null);
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
