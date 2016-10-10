package org.molgenis.data.meta.model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.molgenis.data.Entity;
import org.molgenis.data.support.StaticEntity;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

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
import static org.molgenis.data.meta.model.AttributeMetadata.DESCRIPTION;
import static org.molgenis.data.meta.model.AttributeMetadata.LABEL;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.DEEP_COPY_ATTRS;
import static org.molgenis.data.meta.model.EntityTypeMetadata.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.support.AttributeUtils.getI18nAttributeName;

/**
 * EntityType defines the structure and attributes of an Entity. Attributes are unique. Other software components
 * can use this to interact with Entity and/or to configure backends and frontends, including Repository instances.
 */
public class EntityType extends StaticEntity
{
	private transient Map<String, Attribute> cachedAttrs;
	private transient List<Attribute> cachedOwnAtomicAttrs;
	private transient Boolean cachedHasAttrWithExpession;

	public EntityType(Entity entity)
	{
		super(entity);
	}

	/**
	 * Creates a new entity meta data.
	 */
	protected EntityType()
	{
	}

	/**
	 * Creates a new entity meta data. Normally called by its {@link EntityTypeFactory entity factory}.
	 *
	 * @param entityType entity meta data
	 */
	public EntityType(EntityType entityType)
	{
		super(entityType);
		setDefaultValues();
	}

	/**
	 * Creates a new entity meta data with the given identifier. Normally called by its {@link EntityTypeFactory entity factory}.
	 *
	 * @param entityId   entity identifier (fully qualified entity name)
	 * @param entityType entity meta data
	 */
	public EntityType(String entityId, EntityType entityType)
	{
		super(entityType);
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
	 * {@link #EntityType(EntityType)}). Creates shallow-copy of package, tags and extended entity.
	 *
	 * @param entityType   entity meta data
	 * @param attrCopyMode attribute copy mode that defines whether to deep-copy or shallow-copy attributes
	 * @return copy of entity meta data
	 */
	public static EntityType newInstance(EntityType entityType, AttributeCopyMode attrCopyMode)
	{
		EntityType entityTypeCopy = new EntityType(entityType.getEntityType()); // do not deep-copy
		entityTypeCopy.setSimpleName(entityType.getSimpleName());
		entityTypeCopy.setPackage(entityType.getPackage()); // do not deep-copy
		entityTypeCopy.setLabel(entityType.getLabel());
		entityTypeCopy.setDescription(entityType.getDescription());

		// Own attributes (deep copy or shallow copy)
		if (attrCopyMode == DEEP_COPY_ATTRS)
		{
			LinkedHashMap<String, Attribute> ownAttrMap = stream(entityType.getOwnAttributes().spliterator(), false)
					.map(attr -> Attribute.newInstance(attr, attrCopyMode))
					.map(attrCopy -> attrCopy.setIdentifier(null))
					.collect(toMap(Attribute::getName, Function.identity(), (u, v) ->
					{
						throw new IllegalStateException(String.format("Duplicate key %s", u));
					}, LinkedHashMap::new));
			entityTypeCopy.setOwnAttributes(ownAttrMap.values());

			// Own id attribute (use attribute reference from attributes map)
			Attribute ownIdAttribute = entityType.getOwnIdAttribute();
			entityTypeCopy.setIdAttribute(ownIdAttribute != null ? ownAttrMap.get(ownIdAttribute.getName()) : null);

			// Own label attribute (use attribute reference from attributes map)
			Attribute ownLabelAttr = entityType.getOwnLabelAttribute();
			entityTypeCopy.setLabelAttribute(ownLabelAttr != null ? ownAttrMap.get(ownLabelAttr.getName()) : null);

			// Own lookup attrs (use attribute reference from attributes map)
			Iterable<Attribute> ownLookupAttrs = entityType.getOwnLookupAttributes();
			entityTypeCopy.setLookupAttributes(stream(ownLookupAttrs.spliterator(), false)
					.map(ownLookupAttr -> ownAttrMap.get(ownLookupAttr.getName())).collect(toList()));
		}
		else
		{
			entityTypeCopy.setOwnAttributes(newArrayList(entityType.getOwnAttributes()));
			entityTypeCopy.setIdAttribute(entityType.getOwnIdAttribute());
			entityTypeCopy.setLabelAttribute(entityType.getOwnLabelAttribute());
			entityTypeCopy.setLookupAttributes(newArrayList(entityType.getOwnLookupAttributes()));
		}

		entityTypeCopy.setAbstract(entityType.isAbstract());
		entityTypeCopy.setExtends(entityType.getExtends()); // do not deep-copy
		entityTypeCopy.setTags(newArrayList(entityType.getTags())); // do not deep-copy
		entityTypeCopy.setBackend(entityType.getBackend());

		return entityTypeCopy;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return stream(getEntities(ATTRIBUTES, Attribute.class).spliterator(), false).map(Attribute::getName)::iterator;
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
	public EntityType setName(String fullName)
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
	public EntityType setSimpleName(String simpleName)
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

	public EntityType setLabel(String label)
	{
		if (label == null)
		{
			label = getSimpleName();
		}
		set(LABEL, label);
		return this;
	}

	public EntityType setLabel(String languageCode, String label)
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

	public EntityType setDescription(String description)
	{
		set(DESCRIPTION, description);
		return this;
	}

	public EntityType setDescription(String languageCode, String description)
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

	public EntityType setBackend(String backend)
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

	public EntityType setPackage(Package package_)
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
	public Attribute getIdAttribute()
	{
		Attribute idAttr = getOwnIdAttribute();
		if (idAttr == null)
		{
			EntityType extends_ = getExtends();
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
	public Attribute getOwnIdAttribute()
	{
		return getEntity(ID_ATTRIBUTE, Attribute.class);
	}

	public EntityType setIdAttribute(Attribute idAttr)
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
	public Attribute getLabelAttribute()
	{
		Attribute labelAttr = getOwnLabelAttribute();
		if (labelAttr == null)
		{
			EntityType extends_ = getExtends();
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
	public Attribute getLabelAttribute(String langCode)
	{
		Attribute labelAttr = getLabelAttribute();
		Attribute i18nLabelAttr = labelAttr != null ? getAttribute(labelAttr.getName() + '-' + langCode) : null;
		return i18nLabelAttr != null ? i18nLabelAttr : labelAttr;
	}

	/**
	 * Same as {@link #getLabelAttribute()} but returns null if the label does not exist or the label exists in its
	 * parent class.
	 *
	 * @return label attribute
	 */
	public Attribute getOwnLabelAttribute()
	{
		return getEntity(LABEL_ATTRIBUTE, Attribute.class);
	}

	public Attribute getOwnLabelAttribute(String languageCode)
	{
		return getEntity(getI18nAttributeName(LABEL_ATTRIBUTE, languageCode), Attribute.class);
	}

	public EntityType setLabelAttribute(Attribute labelAttr)
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
	public Attribute getLookupAttribute(String lookupAttrName)
	{
		return stream(getLookupAttributes().spliterator(), false)
				.filter(lookupAttr -> lookupAttr.getName().equals(lookupAttrName)).findFirst().orElse(null);
	}

	/**
	 * Returns attributes that must be searched in case of xref/mref search
	 *
	 * @return lookup attributes
	 */
	public Iterable<Attribute> getLookupAttributes()
	{
		Iterable<Attribute> lookupAttributes = getOwnLookupAttributes();
		EntityType extends_ = getExtends();
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
	public Iterable<Attribute> getOwnLookupAttributes()
	{
		return getEntities(LOOKUP_ATTRIBUTES, Attribute.class);
	}

	public EntityType setLookupAttributes(Iterable<Attribute> lookupAttrs)
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
		Boolean abstract_ = getBoolean(IS_ABSTRACT);
		return abstract_ != null ? abstract_ : false;
	}

	public EntityType setAbstract(boolean abstract_)
	{
		set(IS_ABSTRACT, abstract_);
		return this;
	}

	/**
	 * Entity can extend another entity, adding its properties to their own
	 *
	 * @return parent entity
	 */
	public EntityType getExtends()
	{
		return getEntity(EXTENDS, EntityType.class);
	}

	public EntityType setExtends(EntityType extends_)
	{
		set(EXTENDS, extends_);
		return this;
	}

	/**
	 * Same as {@link #getAttributes()} but does not return attributes of its parent class.
	 *
	 * @return entity attributes without extended entity attributes
	 */
	public Iterable<Attribute> getOwnAttributes()
	{
		return getEntities(ATTRIBUTES, Attribute.class);
	}

	/**
	 * Returns a list of {@link Attribute} which is ordered in case of compound attributes.
	 * Order: 1. attributeParts 2. parentAttribute
	 * <p>
	 * When adding attributes through the {@link org.molgenis.data.DataService}, adding a compound attribute is immediately
	 * persisted to the attributes_parts linking table. This will fail if the corresponding attributeParts have not yet been
	 * added to the database.
	 * <p>
	 * By adding attributeParts before the parent compound attribute, import errors are prevented
	 *
	 * @return A {@link List} of {@link Attribute} containing all own attributes, with compound attributes being placed after
	 * their respective attribute parts
	 */
	public LinkedHashSet<Attribute> getCompoundOrderedAttributes()
	{
		LinkedHashSet<Attribute> attributes = newLinkedHashSet();
		getEntities(ATTRIBUTES, Attribute.class).forEach(attribute ->
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

	private void resolvePossibleNestedCompounds(Attribute attribute, LinkedHashSet<Attribute> attributes)
	{
		if (attribute.getDataType() == COMPOUND)
		{
			attribute.getAttributeParts()
					.forEach(attributePart -> resolvePossibleNestedCompounds(attributePart, attributes));
		}
		attributes.add(attribute);
		return;
	}

	public EntityType setOwnAttributes(Iterable<Attribute> attrs)
	{
		set(ATTRIBUTES, attrs);
		return this;
	}

	// FIXME add getter/setter for tags

	/**
	 * Returns all attributes. In case of compound attributes (attributes consisting of atomic attributes) only the
	 * compound attribute is returned. This attribute can be used to retrieve parts of the compound attribute.
	 * <p>
	 * In case EntityType extends other EntityType then the attributes of this EntityType as well as its
	 * parent class are returned.
	 *
	 * @return entity attributes
	 */
	public Iterable<Attribute> getAttributes()
	{
		Iterable<Attribute> attrs = getOwnAttributes();
		EntityType extends_ = getExtends();
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
	 * In case EntityType extends other EntityType then the attributes of this EntityType as well as its
	 * parent class are returned.
	 *
	 * @return atomic attributes
	 */
	public Iterable<Attribute> getAtomicAttributes()
	{
		Iterable<Attribute> atomicAttrs = getCachedOwnAtomicAttrs();
		EntityType extends_ = getExtends();
		if (extends_ != null)
		{
			atomicAttrs = Iterables.concat(extends_.getAtomicAttributes(), atomicAttrs);
		}
		return atomicAttrs;
	}

	public Iterable<Attribute> getAllAttributes()
	{
		Iterable<Attribute> allAttrs = getOwnAllAttributes();
		EntityType extends_ = getExtends();
		if (extends_ != null)
		{
			allAttrs = concat(allAttrs, extends_.getAllAttributes());
		}
		return allAttrs;
	}

	public Iterable<Attribute> getOwnAllAttributes()
	{
		List<Attribute> allAttrs = new ArrayList<>();
		getOwnAllAttributesRec(getOwnAttributes(), allAttrs);
		return allAttrs;
	}

	/**
	 * Get attribute by name
	 *
	 * @return attribute or <tt>null</tt>
	 */
	public Attribute getAttribute(String attrName)
	{
		Attribute attr = getCachedAttrs().get(attrName);
		if (attr == null)
		{
			// look up attribute in parent entity
			EntityType extendsEntityType = getExtends();
			if (extendsEntityType != null)
			{
				attr = extendsEntityType.getAttribute(attrName);
			}
		}
		return attr;
	}

	public EntityType addAttribute(Attribute attr, AttributeRole... attrTypes)
	{
		invalidateCachedAttrs();

		Iterable<Attribute> attrs = getEntities(ATTRIBUTES, Attribute.class);
		set(ATTRIBUTES, concat(attrs, singletonList(attr)));
		setAttributeRoles(attr, attrTypes);
		return this;
	}

	public void addAttributes(Iterable<Attribute> attrs)
	{
		attrs.forEach(this::addAttribute);
	}

	protected void setAttributeRoles(Attribute attr, AttributeRole... attrTypes)
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

	public void removeAttribute(Attribute attr)
	{
		requireNonNull(attr);
		// FIXME does not remove attr if attr is located in a compound attr
		Iterable<Attribute> existingAttrs = getEntities(ATTRIBUTES, Attribute.class);
		List<Attribute> filteredAttrs = stream(existingAttrs.spliterator(), false)
				.filter(existingAttr -> !existingAttr.getName().equals(attr.getName())).collect(toList());
		set(ATTRIBUTES, filteredAttrs);
	}

	public void addLookupAttribute(Attribute lookupAttr)
	{
		Iterable<Attribute> lookupAttrs = getEntities(LOOKUP_ATTRIBUTES, Attribute.class);
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
	public EntityType setTags(Iterable<Tag> tags)
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
	 * In case EntityType extends other EntityType then the attributes of this EntityType as well as its
	 * parent class are returned.
	 *
	 * @return atomic attributes without extended entity atomic attributes
	 */
	public Iterable<Attribute> getOwnAtomicAttributes()
	{
		List<Attribute> atomicAttrs = new ArrayList<>();
		getOwnAtomicAttributesRec(getOwnAttributes(), atomicAttrs);
		return atomicAttrs;
	}

	public boolean hasBidirectionalAttributes()
	{
		return hasMappedByAttributes() || hasInversedByAttributes();
	}

	public boolean hasMappedByAttributes()
	{
		return getMappedByAttributes().findFirst().orElse(null) != null;
	}

	public Stream<Attribute> getOwnMappedByAttributes()
	{
		return stream(getOwnAtomicAttributes().spliterator(), false).filter(Attribute::isMappedBy);
	}

	public Stream<Attribute> getMappedByAttributes()
	{
		return stream(getAtomicAttributes().spliterator(), false).filter(Attribute::isMappedBy);
	}

	public boolean hasInversedByAttributes()
	{
		return getInversedByAttributes().findFirst().orElse(null) != null;
	}

	public Stream<Attribute> getInversedByAttributes()
	{
		return stream(getAtomicAttributes().spliterator(), false).filter(Attribute::isInversedBy);
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

	private void getOwnAtomicAttributesRec(Iterable<Attribute> attrs, List<Attribute> atomicAttrs)
	{
		for (Attribute attr : attrs)
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

	private void getOwnAllAttributesRec(Iterable<Attribute> attrs, List<Attribute> allAttrs)
	{
		for (Attribute attr : attrs)
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

	private Map<String, Attribute> getCachedAttrs()
	{
		if (cachedAttrs == null)
		{
			cachedAttrs = Maps.newHashMap();
			Iterable<Attribute> attrs = getEntities(ATTRIBUTES, Attribute.class);
			fillCachedAttrsRec(attrs);
		}
		return cachedAttrs;
	}

	private void fillCachedAttrsRec(Iterable<Attribute> attrs)
	{
		for (Attribute attr : attrs)
		{
			cachedAttrs.put(attr.getName(), attr);
			if (attr.getDataType() == COMPOUND)
			{
				fillCachedAttrsRec(attr.getAttributeParts());
			}
		}
	}

	private List<Attribute> getCachedOwnAtomicAttrs()
	{
		if (cachedOwnAtomicAttrs == null)
		{
			cachedOwnAtomicAttrs = new ArrayList<>();
			fillCachedAtomicAttrsRec(getOwnAttributes());
		}
		return cachedOwnAtomicAttrs;
	}

	private void fillCachedAtomicAttrsRec(Iterable<Attribute> attrs)
	{
		for (Attribute attr : attrs)
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
		return "EntityType{" + "name=" + getName() + '}';
	}
}