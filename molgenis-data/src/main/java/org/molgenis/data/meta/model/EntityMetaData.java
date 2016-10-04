package org.molgenis.data.meta.model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.molgenis.data.Entity;
import org.molgenis.data.support.StaticEntity;

import java.util.*;
import java.util.function.Function;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.removeAll;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeCopyMode.DEEP_COPY_ATTRS;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.support.AttributeMetaDataUtils.getI18nAttributeName;

/**
 * EntityMetaData defines the structure and attributes of an Entity. Attributes are unique. Other software components
 * can use this to interact with Entity and/or to configure backends and frontends, including Repository instances.
 */
public class EntityMetaData extends StaticEntity
{
	private transient Map<String, AttributeMetaData> cachedAttrs;
	private transient List<AttributeMetaData> cachedOwnAtomicAttrs;
	private transient Boolean cachedHasAttrWithExpession;

	public EntityMetaData(Entity entity)
	{
		super(entity);
	}

	/**
	 * Creates a new entity meta data.
	 */
	protected EntityMetaData()
	{
	}

	/**
	 * Creates a new entity meta data. Normally called by its {@link EntityMetaDataFactory entity factory}.
	 *
	 * @param entityMeta entity meta data
	 */
	public EntityMetaData(EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
	}

	/**
	 * Creates a new entity meta data with the given identifier. Normally called by its {@link EntityMetaDataFactory entity factory}.
	 *
	 * @param entityId   entity identifier (fully qualified entity name)
	 * @param entityMeta entity meta data
	 */
	public EntityMetaData(String entityId, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
		//FIXME: This is incorrect, the ID value is the fully qualified name, not the simple name!
		setSimpleName(entityId);
	}

	public enum AttributeCopyMode
	{
		SHALLOW_COPY_ATTRS, DEEP_COPY_ATTRS
	}

	/**
	 * Copy-factory (instead of copy-constructor to avoid accidental method overloading to
	 * {@link #EntityMetaData(EntityMetaData)}). Creates shallow-copy of package, tags and extended entity.
	 *
	 * @param entityMeta   entity meta data
	 * @param attrCopyMode attribute copy mode that defines whether to deep-copy or shallow-copy attributes
	 * @return copy of entity meta data
	 */
	public static EntityMetaData newInstance(EntityMetaData entityMeta, AttributeCopyMode attrCopyMode)
	{
		EntityMetaData entityMetaCopy = new EntityMetaData(entityMeta.getEntityMetaData()); // do not deep-copy
		entityMetaCopy.setSimpleName(entityMeta.getSimpleName());
		entityMetaCopy.setPackage(entityMeta.getPackage()); // do not deep-copy
		entityMetaCopy.setLabel(entityMeta.getLabel());
		entityMetaCopy.setDescription(entityMeta.getDescription());

		// Own attributes (deep copy or shallow copy)
		if (attrCopyMode == DEEP_COPY_ATTRS)
		{
			LinkedHashMap<String, AttributeMetaData> ownAttrMap = stream(entityMeta.getOwnAttributes().spliterator(),
					false).map(attr -> AttributeMetaData.newInstance(attr, attrCopyMode))
					.map(attrCopy -> attrCopy.setIdentifier(null))
					.collect(toMap(AttributeMetaData::getName, Function.identity(), (u, v) ->
					{
						throw new IllegalStateException(String.format("Duplicate key %s", u));
					}, LinkedHashMap::new));
			entityMetaCopy.setOwnAttributes(ownAttrMap.values());

			// Own id attribute (use attribute reference from attributes map)
			AttributeMetaData ownIdAttribute = entityMeta.getOwnIdAttribute();
			entityMetaCopy.setIdAttribute(ownIdAttribute != null ? ownAttrMap.get(ownIdAttribute.getName()) : null);

			// Own label attribute (use attribute reference from attributes map)
			AttributeMetaData ownLabelAttr = entityMeta.getOwnLabelAttribute();
			entityMetaCopy.setLabelAttribute(ownLabelAttr != null ? ownAttrMap.get(ownLabelAttr.getName()) : null);

			// Own lookup attrs (use attribute reference from attributes map)
			Iterable<AttributeMetaData> ownLookupAttrs = entityMeta.getOwnLookupAttributes();
			entityMetaCopy.setLookupAttributes(stream(ownLookupAttrs.spliterator(), false)
					.map(ownLookupAttr -> ownAttrMap.get(ownLookupAttr.getName())).collect(toList()));
		}
		else
		{
			entityMetaCopy.setOwnAttributes(newArrayList(entityMeta.getOwnAttributes()));
			entityMetaCopy.setIdAttribute(entityMeta.getOwnIdAttribute());
			entityMetaCopy.setLabelAttribute(entityMeta.getOwnLabelAttribute());
			entityMetaCopy.setLookupAttributes(newArrayList(entityMeta.getOwnLookupAttributes()));
		}

		entityMetaCopy.setAbstract(entityMeta.isAbstract());
		entityMetaCopy.setExtends(entityMeta.getExtends()); // do not deep-copy
		entityMetaCopy.setTags(newArrayList(entityMeta.getTags())); // do not deep-copy
		entityMetaCopy.setBackend(entityMeta.getBackend());

		return entityMetaCopy;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return stream(getEntities(ATTRIBUTES, AttributeMetaData.class).spliterator(), false)
				.map(AttributeMetaData::getName)::iterator;
	}

	/**
	 * Gets the fully qualified entity name.
	 *
	 * @return fully qualified entity name
	 */
	public String getName()
	{
		return getString(FULL_NAME);
	}

	/**
	 * Sets the fully qualified entity name.
	 * In case this entity simple name is null, assigns the fully qualified entity name to the simple name.
	 *
	 * @param fullName fully qualified entity name.
	 * @return this entity meta data for chaining
	 */
	public EntityMetaData setName(String fullName)
	{
		set(FULL_NAME, fullName);
		if (getSimpleName() == null)
		{
			set(SIMPLE_NAME, fullName);
		}
		if (getLabel() == null)
		{
			set(LABEL, fullName);
		}
		return this;
	}

	/**
	 * Gets the entity name.
	 *
	 * @return entity name
	 */
	public String getSimpleName()
	{
		return getString(SIMPLE_NAME);
	}

	/**
	 * Sets the entity name.
	 * In case this entity label is null, assigns the entity name to the label.
	 *
	 * @param simpleName entity name.
	 * @return this entity meta data for chaining
	 */
	public EntityMetaData setSimpleName(String simpleName)
	{
		set(SIMPLE_NAME, simpleName);
		updateFullName();

		if (getLabel() == null)
		{
			setLabel(simpleName);
		}
		return this;
	}

	/**
	 * Human readable entity label
	 *
	 * @return entity label
	 */
	public String getLabel()
	{
		return getString(LABEL);
	}

	/**
	 * Label of the entity in the requested language
	 *
	 * @return entity label
	 */
	public String getLabel(String languageCode)
	{
		String i18nLabel = getString(getI18nAttributeName(LABEL, languageCode));
		return i18nLabel != null ? i18nLabel : getLabel();
	}

	public EntityMetaData setLabel(String label)
	{
		if (label == null)
		{
			label = getSimpleName();
		}
		set(LABEL, label);
		return this;
	}

	public EntityMetaData setLabel(String languageCode, String label)
	{
		set(getI18nAttributeName(LABEL, languageCode), label);
		return this;
	}

	/**
	 * Description of the entity
	 *
	 * @return entity description
	 */
	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	/**
	 * Description of the entity in the requested language
	 *
	 * @return entity description
	 */
	public String getDescription(String languageCode)
	{
		String i18nDescription = getString(getI18nAttributeName(DESCRIPTION, languageCode));
		return i18nDescription != null ? i18nDescription : getDescription();
	}

	public EntityMetaData setDescription(String description)
	{
		set(DESCRIPTION, description);
		return this;
	}

	public EntityMetaData setDescription(String languageCode, String description)
	{
		set(getI18nAttributeName(DESCRIPTION, languageCode), description);
		return this;
	}

	/**
	 * The name of the repostory collection/backend where the entities of this type are stored
	 *
	 * @return backend name
	 */
	public String getBackend()
	{
		return getString(BACKEND);
	}

	public EntityMetaData setBackend(String backend)
	{
		set(BACKEND, backend);
		return this;
	}

	/**
	 * Gets the package where this entity belongs to
	 *
	 * @return package
	 */
	public Package getPackage()
	{
		return getEntity(PACKAGE, Package.class);
	}

	public EntityMetaData setPackage(Package package_)
	{
		set(PACKAGE, package_);
		updateFullName();
		return this;
	}

	/**
	 * Attribute that is used as unique Id. Id attribute should always be provided.
	 *
	 * @return id attribute
	 */
	public AttributeMetaData getIdAttribute()
	{
		AttributeMetaData idAttr = getOwnIdAttribute();
		if (idAttr == null)
		{
			EntityMetaData extends_ = getExtends();
			if (extends_ != null)
			{
				idAttr = extends_.getIdAttribute();
			}
		}
		return idAttr;
	}

	/**
	 * Same as {@link #getIdAttribute()} but returns null if the id attribute is defined in its parent class.
	 *
	 * @return id attribute
	 */
	public AttributeMetaData getOwnIdAttribute()
	{
		return getEntity(ID_ATTRIBUTE, AttributeMetaData.class);
	}

	public EntityMetaData setIdAttribute(AttributeMetaData idAttr)
	{
		set(ID_ATTRIBUTE, idAttr);
		if (idAttr != null)
		{
			idAttr.setReadOnly(true);
			idAttr.setUnique(true);
			idAttr.setNillable(false);
		}
		if (getLabelAttribute() == null)
		{
			setLabelAttribute(idAttr);
		}
		return this;
	}

	/**
	 * Attribute that is used as unique label. If no label exist, returns getIdAttribute().
	 *
	 * @return label attribute
	 */
	public AttributeMetaData getLabelAttribute()
	{
		AttributeMetaData labelAttr = getOwnLabelAttribute();
		if (labelAttr == null)
		{
			EntityMetaData extends_ = getExtends();
			if (extends_ != null)
			{
				labelAttr = extends_.getLabelAttribute();
			}
		}
		return labelAttr;
	}

	/**
	 * Gets the correct label attribute for the given language, or the default if not found
	 *
	 * @param langCode language code
	 * @return label attribute
	 */
	public AttributeMetaData getLabelAttribute(String langCode)
	{
		AttributeMetaData labelAttr = getLabelAttribute();
		AttributeMetaData i18nLabelAttr = labelAttr != null ? getAttribute(labelAttr.getName() + '-' + langCode) : null;
		return i18nLabelAttr != null ? i18nLabelAttr : labelAttr;
	}

	/**
	 * Same as {@link #getLabelAttribute()} but returns null if the label does not exist or the label exists in its
	 * parent class.
	 *
	 * @return label attribute
	 */
	public AttributeMetaData getOwnLabelAttribute()
	{
		return getEntity(LABEL_ATTRIBUTE, AttributeMetaData.class);
	}

	public AttributeMetaData getOwnLabelAttribute(String languageCode)
	{
		return getEntity(getI18nAttributeName(LABEL_ATTRIBUTE, languageCode), AttributeMetaData.class);
	}

	public EntityMetaData setLabelAttribute(AttributeMetaData labelAttr)
	{
		set(LABEL_ATTRIBUTE, labelAttr);
		return this;
	}

	/**
	 * Get lookup attribute by name (case insensitive), returns null if not found
	 *
	 * @param lookupAttrName lookup attribute name
	 * @return lookup attribute or <tt>null</tt>
	 */
	public AttributeMetaData getLookupAttribute(String lookupAttrName)
	{
		return stream(getLookupAttributes().spliterator(), false)
				.filter(lookupAttr -> lookupAttr.getName().equals(lookupAttrName)).findFirst().orElse(null);
	}

	/**
	 * Returns attributes that must be searched in case of xref/mref search
	 *
	 * @return lookup attributes
	 */
	public Iterable<AttributeMetaData> getLookupAttributes()
	{
		Iterable<AttributeMetaData> lookupAttributes = getOwnLookupAttributes();
		EntityMetaData extends_ = getExtends();
		if (extends_ != null)
		{
			lookupAttributes = concat(lookupAttributes, extends_.getLookupAttributes());
		}
		return lookupAttributes;
	}

	/**
	 * Returns attributes that must be searched in case of xref/mref search
	 *
	 * @return lookup attributes
	 */
	public Iterable<AttributeMetaData> getOwnLookupAttributes()
	{
		return getEntities(LOOKUP_ATTRIBUTES, AttributeMetaData.class);
	}

	public EntityMetaData setLookupAttributes(Iterable<AttributeMetaData> lookupAttrs)
	{
		set(LOOKUP_ATTRIBUTES, lookupAttrs);
		return this;
	}

	/**
	 * Entities can be abstract (analogous an 'interface' or 'protocol'). Use is to define reusable Entity model
	 * components that cannot be instantiated themselves (i.e. there cannot be data attached to this entity meta data).
	 *
	 * @return whether or not this entity is an abstract entity
	 */
	public boolean isAbstract()
	{
		Boolean abstract_ = getBoolean(ABSTRACT);
		return abstract_ != null ? abstract_ : false;
	}

	public EntityMetaData setAbstract(boolean abstract_)
	{
		set(ABSTRACT, abstract_);
		return this;
	}

	/**
	 * Entity can extend another entity, adding its properties to their own
	 *
	 * @return parent entity
	 */
	public EntityMetaData getExtends()
	{
		return getEntity(EXTENDS, EntityMetaData.class);
	}

	public EntityMetaData setExtends(EntityMetaData extends_)
	{
		set(EXTENDS, extends_);
		return this;
	}

	/**
	 * Same as {@link #getAttributes()} but does not return attributes of its parent class.
	 *
	 * @return entity attributes without extended entity attributes
	 */
	public Iterable<AttributeMetaData> getOwnAttributes()
	{
		return getEntities(ATTRIBUTES, AttributeMetaData.class);
	}

	/**
	 * Returns a list of {@link AttributeMetaData} which is ordered in case of compound attributes.
	 * Order: 1. attributeParts 2. parentAttribute
	 * <p>
	 * When adding attributes through the {@link org.molgenis.data.DataService}, adding a compound attribute is immediately
	 * persisted to the attributes_parts linking table. This will fail if the corresponding attributeParts have not yet been
	 * added to the database.
	 * <p>
	 * By adding attributeParts before the parent compound attribute, import errors are prevented
	 *
	 * @return A {@link List} of {@link AttributeMetaData} containing all own attributes, with compound attributes being placed after
	 * their respective attribute parts
	 */
	public LinkedHashSet<AttributeMetaData> getCompoundOrderedAttributes()
	{
		LinkedHashSet<AttributeMetaData> attributes = newLinkedHashSet();
		getEntities(ATTRIBUTES, AttributeMetaData.class).forEach(attribute ->
		{
			if (attribute.getDataType() == COMPOUND)
			{
				attribute.getAttributeParts()
						.forEach(attributePart -> resolvePossibleNestedCompounds(attributePart, attributes));
			}
			attributes.add(attribute);
		});
		return attributes;
	}

	private void resolvePossibleNestedCompounds(AttributeMetaData attribute,
			LinkedHashSet<AttributeMetaData> attributes)
	{
		if (attribute.getDataType() == COMPOUND)
		{
			attribute.getAttributeParts()
					.forEach(attributePart -> resolvePossibleNestedCompounds(attributePart, attributes));
		}
		attributes.add(attribute);
		return;
	}

	public EntityMetaData setOwnAttributes(Iterable<AttributeMetaData> attrs)
	{
		set(ATTRIBUTES, attrs);
		return this;
	}

	// FIXME add getter/setter for tags

	/**
	 * Returns all attributes. In case of compound attributes (attributes consisting of atomic attributes) only the
	 * compound attribute is returned. This attribute can be used to retrieve parts of the compound attribute.
	 * <p>
	 * In case EntityMetaData extends other EntityMetaData then the attributes of this EntityMetaData as well as its
	 * parent class are returned.
	 *
	 * @return entity attributes
	 */
	public Iterable<AttributeMetaData> getAttributes()
	{
		Iterable<AttributeMetaData> attrs = getOwnAttributes();
		EntityMetaData extends_ = getExtends();
		if (extends_ != null)
		{
			attrs = concat(attrs, extends_.getAttributes());
		}
		return attrs;
	}

	/**
	 * Returns all atomic attributes. In case of compound attributes (attributes consisting of atomic attributes) only
	 * the descendant atomic attributes are returned. The compound attribute itself is not returned.
	 * <p>
	 * In case EntityMetaData extends other EntityMetaData then the attributes of this EntityMetaData as well as its
	 * parent class are returned.
	 *
	 * @return atomic attributes
	 */
	public Iterable<AttributeMetaData> getAtomicAttributes()
	{
		Iterable<AttributeMetaData> atomicAttrs = getCachedOwnAtomicAttrs();
		EntityMetaData extends_ = getExtends();
		if (extends_ != null)
		{
			atomicAttrs = Iterables.concat(extends_.getAtomicAttributes(), atomicAttrs);
		}
		return atomicAttrs;
	}

	public Iterable<AttributeMetaData> getAllAttributes()
	{
		Iterable<AttributeMetaData> allAttrs = getOwnAllAttributes();
		EntityMetaData extends_ = getExtends();
		if (extends_ != null)
		{
			allAttrs = concat(allAttrs, extends_.getAllAttributes());
		}
		return allAttrs;
	}

	public Iterable<AttributeMetaData> getOwnAllAttributes()
	{
		List<AttributeMetaData> allAttrs = new ArrayList<>();
		getOwnAllAttributesRec(getOwnAttributes(), allAttrs);
		return allAttrs;
	}

	/**
	 * Get attribute by name
	 *
	 * @return attribute or <tt>null</tt>
	 */
	public AttributeMetaData getAttribute(String attrName)
	{
		AttributeMetaData attr = getCachedAttrs().get(attrName);
		if (attr == null)
		{
			// look up attribute in parent entity
			EntityMetaData extendsEntityMeta = getExtends();
			if (extendsEntityMeta != null)
			{
				attr = extendsEntityMeta.getAttribute(attrName);
			}
		}
		return attr;
	}

	public EntityMetaData addAttribute(AttributeMetaData attr, AttributeRole... attrTypes)
	{
		invalidateCachedAttrs();

		Iterable<AttributeMetaData> attrs = getEntities(ATTRIBUTES, AttributeMetaData.class);
		set(ATTRIBUTES, concat(attrs, singletonList(attr)));
		setAttributeRoles(attr, attrTypes);
		return this;
	}

	public void addAttributes(Iterable<AttributeMetaData> attrs)
	{
		attrs.forEach(this::addAttribute);
	}

	protected void setAttributeRoles(AttributeMetaData attr, AttributeRole... attrTypes)
	{
		if (attrTypes != null)
		{
			for (AttributeRole attrType : attrTypes)
			{
				switch (attrType)
				{
					case ROLE_ID:
						setIdAttribute(attr);
						break;
					case ROLE_LABEL:
						setLabelAttribute(attr);
						break;
					case ROLE_LOOKUP:
						addLookupAttribute(attr);
						break;
					default:
						throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
				}
			}
		}
	}

	/**
	 * Returns whether this entity has an attribute with expression
	 *
	 * @return whether this entity has an attribute with expression
	 */
	public boolean hasAttributeWithExpression()
	{
		return getCachedHasAttrWithExpession();
	}

	private boolean getCachedHasAttrWithExpession()
	{
		if (cachedHasAttrWithExpession == null)
		{
			cachedHasAttrWithExpession = stream(getAtomicAttributes().spliterator(), false)
					.anyMatch(attr -> attr.getExpression() != null);
		}
		return cachedHasAttrWithExpession;
	}

	public void removeAttribute(AttributeMetaData attr)
	{
		requireNonNull(attr);
		// FIXME does not remove attr if attr is located in a compound attr
		Iterable<AttributeMetaData> existingAttrs = getEntities(ATTRIBUTES, AttributeMetaData.class);
		List<AttributeMetaData> filteredAttrs = stream(existingAttrs.spliterator(), false)
				.filter(existingAttr -> !existingAttr.getName().equals(attr.getName())).collect(toList());
		set(ATTRIBUTES, filteredAttrs);
	}

	public void addLookupAttribute(AttributeMetaData lookupAttr)
	{
		Iterable<AttributeMetaData> lookupAttrs = getEntities(LOOKUP_ATTRIBUTES, AttributeMetaData.class);
		if (!Iterables.contains(lookupAttrs, lookupAttr))
		{
			set(LOOKUP_ATTRIBUTES, concat(lookupAttrs, singletonList(lookupAttr)));
		}
	}

	/**
	 * Get all tags for this entity
	 *
	 * @return entity tags
	 */
	public Iterable<Tag> getTags()
	{
		return getEntities(TAGS, Tag.class);
	}

	/**
	 * Set tags for this entity
	 *
	 * @param tags entity tags
	 * @return this entity
	 */
	public EntityMetaData setTags(Iterable<Tag> tags)
	{
		set(TAGS, tags);
		return this;
	}

	/**
	 * Add a tag for this entity
	 *
	 * @param tag entity tag
	 */
	public void addTag(Tag tag)
	{
		set(TAGS, concat(getTags(), singletonList(tag)));
	}

	/**
	 * Add a tag for this entity
	 *
	 * @param tag entity tag
	 */
	public void removeTag(Tag tag)
	{
		Iterable<Tag> tags = getTags();
		removeAll(tags, singletonList(tag));
		set(TAGS, tag);
	}

	/**
	 * Returns all atomic attributes. In case of compound attributes (attributes consisting of atomic attributes) only
	 * the descendant atomic attributes are returned. The compound attribute itself is not returned.
	 * <p>
	 * In case EntityMetaData extends other EntityMetaData then the attributes of this EntityMetaData as well as its
	 * parent class are returned.
	 *
	 * @return atomic attributes without extended entity atomic attributes
	 */
	public Iterable<AttributeMetaData> getOwnAtomicAttributes()
	{
		List<AttributeMetaData> atomicAttrs = new ArrayList<>();
		getOwnAtomicAttributesRec(getOwnAttributes(), atomicAttrs);
		return atomicAttrs;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		super.set(attributeName, value);
		switch (attributeName)
		{
			case ATTRIBUTES:
				invalidateCachedAttrs();
				break;
			default:
				break;
		}
	}

	private void getOwnAtomicAttributesRec(Iterable<AttributeMetaData> attrs, List<AttributeMetaData> atomicAttrs)
	{
		for (AttributeMetaData attr : attrs)
		{
			if (attr.getDataType() == COMPOUND)
			{
				getOwnAtomicAttributesRec(attr.getAttributeParts(), atomicAttrs);
			}
			else
			{
				atomicAttrs.add(attr);
			}
		}
	}

	private void getOwnAllAttributesRec(Iterable<AttributeMetaData> attrs, List<AttributeMetaData> allAttrs)
	{
		for (AttributeMetaData attr : attrs)
		{
			if (attr.getDataType() == COMPOUND)
			{
				getOwnAllAttributesRec(attr.getAttributeParts(), allAttrs);
			}
			allAttrs.add(attr);
		}
	}

	private void updateFullName()
	{
		String simpleName = getSimpleName();
		if (simpleName != null)
		{
			String fullName;
			Package package_ = getPackage();
			if (package_ != null)
			{
				fullName = package_.getName() + PACKAGE_SEPARATOR + simpleName;
			}
			else
			{
				fullName = simpleName;
			}
			set(FULL_NAME, fullName);
		}
	}

	protected void setDefaultValues()
	{
		setAbstract(false);
	}

	private Map<String, AttributeMetaData> getCachedAttrs()
	{
		if (cachedAttrs == null)
		{
			cachedAttrs = Maps.newHashMap();
			Iterable<AttributeMetaData> attrs = getEntities(ATTRIBUTES, AttributeMetaData.class);
			fillCachedAttrsRec(attrs);
		}
		return cachedAttrs;
	}

	private void fillCachedAttrsRec(Iterable<AttributeMetaData> attrs)
	{
		for (AttributeMetaData attr : attrs)
		{
			cachedAttrs.put(attr.getName(), attr);
			if (attr.getDataType() == COMPOUND)
			{
				fillCachedAttrsRec(attr.getAttributeParts());
			}
		}
	}

	private List<AttributeMetaData> getCachedOwnAtomicAttrs()
	{
		if (cachedOwnAtomicAttrs == null)
		{
			cachedOwnAtomicAttrs = new ArrayList<>();
			fillCachedAtomicAttrsRec(getOwnAttributes());
		}
		return cachedOwnAtomicAttrs;
	}

	private void fillCachedAtomicAttrsRec(Iterable<AttributeMetaData> attrs)
	{
		for (AttributeMetaData attr : attrs)
		{
			if (attr.getDataType() == COMPOUND)
			{
				fillCachedAtomicAttrsRec(attr.getAttributeParts());
			}
			else
			{
				cachedOwnAtomicAttrs.add(attr);
			}
		}
	}

	private void invalidateCachedAttrs()
	{
		cachedAttrs = null;
		cachedOwnAtomicAttrs = null;
		cachedHasAttrWithExpession = null;
	}

	public enum AttributeRole
	{
		ROLE_ID, ROLE_LABEL, ROLE_LOOKUP
	}

	@Override
	public String toString()
	{
		return "EntityMetaData{" + "name=" + getName() + '}';
	}
}