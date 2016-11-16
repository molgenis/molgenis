package org.molgenis.data.meta.model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.molgenis.data.Entity;
import org.molgenis.data.support.StaticEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.removeAll;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.AttributeType.COMPOUND;
import static org.molgenis.data.meta.model.AttributeMetadata.DESCRIPTION;
import static org.molgenis.data.meta.model.AttributeMetadata.LABEL;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.DEEP_COPY_ATTRS;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.SHALLOW_COPY_ATTRS;
import static org.molgenis.data.meta.model.EntityTypeMetadata.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.support.AttributeUtils.getI18nAttributeName;

/**
 * EntityType defines the structure and attributes of an Entity. Attributes are unique. Other software components
 * can use this to interact with Entity and/or to configure backends and frontends, including Repository instances.
 */
public class EntityType extends StaticEntity
{
	private transient Map<String, Attribute> cachedOwnAttrs;
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
	 * @param entityType entity meta data
	 * @return copy of entity meta data
	 */
	public static EntityType newInstance(EntityType entityType)
	{
		return newInstance(entityType, SHALLOW_COPY_ATTRS, null);
	}

	/**
	 * Copy-factory (instead of copy-constructor to avoid accidental method overloading to
	 * {@link #EntityType(EntityType)}). Creates shallow-copy of package, tags and extended entity.
	 *
	 * @param entityType   entity meta data
	 * @param attrCopyMode attribute copy mode that defines whether to deep-copy or shallow-copy attributes
	 * @param attrFactory  attribute factory used to create new attributes in deep-copy mode
	 * @return copy of entity meta data
	 */
	public static EntityType newInstance(EntityType entityType, AttributeCopyMode attrCopyMode,
			AttributeFactory attrFactory)
	{
		EntityType entityTypeCopy = new EntityType(entityType.getEntityType()); // do not deep-copy
		entityTypeCopy.setSimpleName(entityType.getSimpleName());
		entityTypeCopy.setPackage(entityType.getPackage()); // do not deep-copy
		entityTypeCopy.setLabel(entityType.getLabel());
		entityTypeCopy.setDescription(entityType.getDescription());

		// Own attributes (deep copy or shallow copy)
		if (attrCopyMode == DEEP_COPY_ATTRS)
		{
			// step #1: deep copy attributes
			LinkedHashMap<String, Attribute> ownAttrMap = stream(entityType.getOwnAllAttributes().spliterator(), false)
					.map(attr -> Attribute.newInstance(attr, attrCopyMode, attrFactory))
					.map(attrCopy -> attrCopy.setEntity(entityTypeCopy))
					.collect(toMap(Attribute::getName, Function.identity(), (u, v) ->
					{
						throw new IllegalStateException(String.format("Duplicate key %s", u));
					}, LinkedHashMap::new));

			// step #2: update attribute.parent relations
			ownAttrMap.forEach((attrName, ownAttr) ->
			{
				Attribute ownAttrParent = ownAttr.getParent();
				if (ownAttrParent != null)
				{
					ownAttr.setParent(ownAttrMap.get(ownAttrParent.getName()));
				}
			});

			entityTypeCopy.setOwnAllAttributes(ownAttrMap.values());
		}
		else
		{
			entityTypeCopy.setOwnAllAttributes(newArrayList(entityType.getOwnAllAttributes()));
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
		for (Attribute ownAttr : getOwnAllAttributes())
		{
			if (ownAttr.isIdAttribute())
			{
				return ownAttr;
			}
		}
		return null;
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
	// FIXME cache own label attribute
	public Attribute getOwnLabelAttribute()
	{
		for (Attribute ownAttr : getOwnAllAttributes())
		{
			if (ownAttr.isLabelAttribute())
			{
				return ownAttr;
			}
		}
		return null;
	}

	public Attribute getOwnLabelAttribute(String languageCode)
	{
		Attribute labelAttr = getOwnLabelAttribute();
		if (labelAttr != null)
		{
			return getEntity(getI18nAttributeName(labelAttr.getName(), languageCode), Attribute.class);
		}
		else
		{
			return null;
		}
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
		List<Attribute> ownLookupAttrs = stream(getOwnAllAttributes().spliterator(), false)
				.filter(attr -> attr.getLookupAttributeIndex() != null).collect(toCollection(ArrayList::new));
		if (ownLookupAttrs.size() > 1)
		{
			sort(ownLookupAttrs, (o1, o2) -> o1.getLookupAttributeIndex() < o2.getLookupAttributeIndex() ? -1 : 1);
		}
		return ownLookupAttrs;
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
		return stream(getOwnAllAttributes().spliterator(), false).filter(attr -> attr.getParent() == null)
				.collect(toList());
	}

	public EntityType setOwnAllAttributes(Iterable<Attribute> attrs)
	{
		invalidateCachedOwnAttrs();
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
		Iterable<Attribute> atomicAttrs = getOwnAtomicAttributes();
		EntityType extends_ = getExtends();
		if (extends_ != null)
		{
			atomicAttrs = concat(atomicAttrs, extends_.getAtomicAttributes());
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
		return getCachedOwnAttrs().values();
	}

	/**
	 * Get attribute by name
	 *
	 * @return attribute or <tt>null</tt>
	 */
	public Attribute getAttribute(String attrName)
	{
		Attribute attr = getCachedOwnAttrs().get(attrName);
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
		invalidateCachedOwnAttrs();

		attr.setEntity(this);
		Iterable<Attribute> attrs = getEntities(ATTRIBUTES, Attribute.class);
		attr.setSequenceNumber(Iterables.size(attrs));
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
						attr.setIdAttribute(true);
						if (getLabelAttribute() == null)
						{
							attr.setLabelAttribute(true);
						}
						break;
					case ROLE_LABEL:
						Attribute currentLabelAttr = getLabelAttribute();
						if (currentLabelAttr != null)
						{
							currentLabelAttr.setLabelAttribute(false);
						}
						attr.setLabelAttribute(true);
						break;
					case ROLE_LOOKUP:
						attr.setLookupAttributeIndex(0); // FIXME assign unique lookup attribute index
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
		Map<String, Attribute> cachedOwnAttrs = getCachedOwnAttrs();
		cachedOwnAttrs.remove(attr.getName());
		set(ATTRIBUTES, cachedOwnAttrs.values());
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
		return () -> getCachedOwnAttrs().values().stream().filter(attr -> attr.getDataType() != COMPOUND).iterator();
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
				invalidateCachedOwnAttrs();
				break;
			default:
				break;
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

	private Map<String, Attribute> getCachedOwnAttrs()
	{
		if (cachedOwnAttrs == null)
		{
			cachedOwnAttrs = Maps.newLinkedHashMap();
			getEntities(ATTRIBUTES, Attribute.class).forEach(attr -> cachedOwnAttrs.put(attr.getName(), attr));
		}
		return cachedOwnAttrs;
	}

	private void invalidateCachedOwnAttrs()
	{
		cachedOwnAttrs = null;
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