package org.molgenis.data.meta;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.removeAll;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ABSTRACT;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ATTRIBUTES;
import static org.molgenis.data.meta.EntityMetaDataMetaData.BACKEND;
import static org.molgenis.data.meta.EntityMetaDataMetaData.EXTENDS;
import static org.molgenis.data.meta.EntityMetaDataMetaData.FULL_NAME;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.LABEL_ATTRIBUTE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.LOOKUP_ATTRIBUTES;
import static org.molgenis.data.meta.EntityMetaDataMetaData.PACKAGE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.SIMPLE_NAME;
import static org.molgenis.data.meta.EntityMetaDataMetaData.TAGS;
import static org.molgenis.data.support.AttributeMetaDataUtils.getI18nAttributeName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.molgenis.data.Entity;
import org.molgenis.data.semantic.Tag;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.data.support.MapEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EntityMetaData defines the structure and attributes of an Entity. Attributes are unique. Other software components
 * can use this to interact with Entity and/or to configure backends and frontends, including Repository instances.
 */
public class EntityMetaDataImpl extends AbstractEntity implements EntityMetaData
{
	private static final Logger LOG = LoggerFactory.getLogger(EntityMetaDataImpl.class);

	private final Entity entity;

	protected EntityMetaDataImpl()
	{
		this.entity = new MapEntity(this);
	}

	public EntityMetaDataImpl(Entity entity)
	{
		this.entity = requireNonNull(entity);
	}

	public EntityMetaDataImpl(String simpleName)
	{
		this(simpleName, Entity.class);
	}

	public EntityMetaDataImpl(String simpleName, Package package_)
	{
		this(simpleName, Entity.class, package_);
	}

	public EntityMetaDataImpl(String simpleName, Class<? extends Entity> entityClass)
	{
		this(simpleName, entityClass, null);
	}

	public EntityMetaDataImpl(String simpleName, Class<? extends Entity> entityClass, Package package_)
	{
		this.entity = new MapEntity(getEntityMetaData());
		String fullName;
		if (package_ != null)
		{
			fullName = package_.getName() + Package.PACKAGE_SEPARATOR + simpleName;
		}
		else
		{
			fullName = simpleName;
		}
		set(SIMPLE_NAME, simpleName);
		set(FULL_NAME, fullName);
		set(PACKAGE, package_);
	}

	/**
	 * Copy-factory (instead of copy-constructor to avoid accidental method overloading to {@link #EntityMetaDataImpl(Entity)})
	 *
	 * @param entityMeta entity meta data
	 * @return deep copy of entity meta data
	 */
	public static EntityMetaDataImpl newInstance(EntityMetaData entityMeta)
	{
		Entity entityCopy = MapEntity.newInstance(entityMeta);
		return new EntityMetaDataImpl(entityCopy);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		//		return EntityMetaDataMetaData.get();
		return this;
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

	/**
	 * Gets the fully qualified entity name.
	 *
	 * @return fully qualified entity name
	 */
	@Override
	public String getName()
	{
		return getString(FULL_NAME);
	}

	@Override
	public EntityMetaData setName(String fullName)
	{
		set(FULL_NAME, fullName);
		return this;
	}

	/**
	 * Gets the entity name.
	 *
	 * @return entity name
	 */
	@Override
	public String getSimpleName()
	{
		return getString(SIMPLE_NAME);
	}

	@Override
	public EntityMetaData setSimpleName(String simpleName)
	{
		set(SIMPLE_NAME, simpleName);
		return this;
	}

	/**
	 * Optional human readable longer label
	 *
	 * @return entity label
	 */
	@Override
	public String getLabel()
	{
		return getString(LABEL);
	}

	/**
	 * Label of the entity in the requested language
	 *
	 * @return entity label
	 */
	@Override
	public String getLabel(String languageCode)
	{
		return getString(getI18nAttributeName(LABEL, languageCode));
	}

	@Override
	public EntityMetaData setLabel(String label)
	{
		set(LABEL, label);
		return this;
	}

	@Override
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
	@Override
	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	/**
	 * Description of the entity in the requested language
	 *
	 * @return entity description
	 */
	@Override
	public String getDescription(String languageCode)
	{
		return getString(getI18nAttributeName(DESCRIPTION, languageCode));
	}

	@Override
	public EntityMetaData setDescription(String description)
	{
		set(DESCRIPTION, description);
		return this;
	}

	@Override
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
	@Override
	public String getBackend()
	{
		return getString(BACKEND);
	}

	@Override
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
	@Override
	public Package getPackage()
	{
		return getEntity(PACKAGE, Package.class);
	}

	@Override
	public EntityMetaData setPackage(Package package_)
	{
		set(PACKAGE, package_);
		return this;
	}

	/**
	 * Attribute that is used as unique Id. Id attribute should always be provided.
	 *
	 * @return id attribute
	 */
	@Override
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
	@Override
	public AttributeMetaData getOwnIdAttribute()
	{
		return getEntity(ID_ATTRIBUTE, AttributeMetaData.class);
	}

	@Override
	public EntityMetaData setIdAttribute(AttributeMetaData idAttr)
	{
		set(ID_ATTRIBUTE, idAttr);
		if (getLabelAttribute() == null)
		{
			setLabelAttribute(idAttr);
		}
		if (!getLookupAttributes().iterator().hasNext())
		{
			addLookupAttribute(idAttr);
		}
		return this;
	}

	/**
	 * Attribute that is used as unique label. If no label exist, returns getIdAttribute().
	 *
	 * @return label attribute
	 */
	@Override
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
	 * @return label attribute
	 */
	@Override
	public AttributeMetaData getLabelAttribute(String languageCode)
	{
		AttributeMetaData labelAttr = getOwnLabelAttribute(languageCode);
		if (labelAttr == null)
		{
			EntityMetaData extends_ = getExtends();
			if (extends_ != null)
			{
				labelAttr = extends_.getLabelAttribute(languageCode);
			}
		}
		return labelAttr;
	}

	/**
	 * Same as {@link #getLabelAttribute()} but returns null if the label does not exist or the label exists in its
	 * parent class.
	 *
	 * @return label attribute
	 */
	@Override
	public AttributeMetaData getOwnLabelAttribute()
	{
		return getEntity(LABEL_ATTRIBUTE, AttributeMetaData.class);
	}

	@Override
	public AttributeMetaData getOwnLabelAttribute(String languageCode)
	{
		return getEntity(getI18nAttributeName(LABEL_ATTRIBUTE, languageCode), AttributeMetaData.class);
	}

	@Override
	public EntityMetaData setLabelAttribute(AttributeMetaData labelAttr)
	{
		set(LABEL_ATTRIBUTE, labelAttr);
		if (!getLookupAttributes().iterator().hasNext())
		{
			addLookupAttribute(labelAttr);
		}
		return this;
	}

	/**
	 * Get lookup attribute by name (case insensitive), returns null if not found
	 *
	 * @param lookupAttrName lookup attribute name
	 * @return lookup attribute or <tt>null</tt>
	 */
	@Override
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
	@Override
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
	@Override
	public Iterable<AttributeMetaData> getOwnLookupAttributes()
	{
		return getEntities(LOOKUP_ATTRIBUTES, AttributeMetaData.class);
	}

	@Override
	public EntityMetaData setLookupAttributes(Iterable<AttributeMetaData> lookupAttrs)
	{
		set(LABEL_ATTRIBUTE, lookupAttrs);
		return this;
	}

	/**
	 * Entities can be abstract (analogous an 'interface' or 'protocol'). Use is to define reusable Entity model
	 * components that cannot be instantiated themselves (i.e. there cannot be data attached to this entity meta data).
	 *
	 * @return whether or not this entity is an abstract entity
	 */
	@Override
	public boolean isAbstract()
	{
		return requireNonNull(getBoolean(ABSTRACT));
	}

	@Override
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
	@Override
	public EntityMetaData getExtends()
	{
		return getEntity(EXTENDS, EntityMetaDataImpl.class);
	}

	@Override
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
	@Override
	public Iterable<AttributeMetaData> getOwnAttributes()
	{
		return getEntities(ATTRIBUTES, AttributeMetaData.class);
	}

	@Override
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
	@Override
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
	@Override
	public Iterable<AttributeMetaData> getAtomicAttributes()
	{
		Iterable<AttributeMetaData> atomicAttrs = getOwnAtomicAttributes();
		EntityMetaData extends_ = getExtends();
		if (extends_ != null)
		{
			atomicAttrs = concat(atomicAttrs, extends_.getAtomicAttributes());
		}
		return atomicAttrs;
	}

	/**
	 * Get attribute by name (case insensitive), returns null if not found
	 *
	 * @return attribute or <tt>null</tt>
	 */
	@Override
	public AttributeMetaData getAttribute(String attrName)
	{
		return getAttributeRec(attrName, getAttributes());
	}

	@Override
	public void addAttribute(AttributeMetaData attr, AttributeRole... attrTypes)
	{
		Iterable<AttributeMetaData> attrs = entity.getEntities(ATTRIBUTES, AttributeMetaData.class);
		entity.set(ATTRIBUTES, concat(attrs, singletonList(attr)));
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

	@Override
	public AttributeMetaData addAttribute(String name, AttributeRole... attrTypes)
	{
		AttributeMetaData attr = new AttributeMetaData(name);
		this.addAttribute(attr, attrTypes);
		return attr;
	}

	@Override
	public void addAttributes(Iterable<AttributeMetaData> attrs)
	{
		attrs.forEach(this::addAttribute);
	}

	/**
	 * Returns whether this entity has a attribute with expression
	 *
	 * @return whether this entity has a attribute with expression
	 */
	@Override
	public boolean hasAttributeWithExpression()
	{
		return stream(getAtomicAttributes().spliterator(), false).anyMatch(attr -> attr.getExpression() != null);
	}

	@Override
	public void removeAttribute(AttributeMetaData attr)
	{
		// FIXME does not remove attr if attr is located in a compound attr
		Iterable<AttributeMetaData> existingAttrs = entity.getEntities(ATTRIBUTES, AttributeMetaData.class);
		List<AttributeMetaData> filteredAttrs = stream(existingAttrs.spliterator(), false)
				.filter(existingAttr -> !existingAttr.getName().equals(attr.getName())).collect(Collectors.toList());
		entity.set(ATTRIBUTES, filteredAttrs);
	}

	@Override
	public void addLookupAttribute(AttributeMetaData lookupAttr)
	{
		Iterable<AttributeMetaData> lookupAttrs = entity.getEntities(LOOKUP_ATTRIBUTES, AttributeMetaData.class);
		entity.set(LOOKUP_ATTRIBUTES, concat(lookupAttrs, singletonList(lookupAttr)));
	}

	/**
	 * Get all tags for this entity
	 *
	 * @return entity tags
	 */
	@Override
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
	@Override
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
	@Override
	public void addTag(Tag tag)
	{
		entity.set(TAGS, concat(getTags(), singletonList(tag)));
	}

	/**
	 * Add a tag for this entity
	 *
	 * @param tag entity tag
	 */
	@Override
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

		EntityMetaDataImpl that = (EntityMetaDataImpl) o;

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
		return "EntityMetaData{" +
				"entity=" + entity +
				'}';
	}

	private AttributeMetaData getAttributeRec(String attrName, Iterable<AttributeMetaData> attrs)
	{
		for (AttributeMetaData attr : attrs)
		{
			if (attr.getName().equals(attrName))
			{
				return attr;
			}
			if (attr.getDataType().getEnumType() == COMPOUND)
			{
				getAttributeRec(attrName, attr.getAttributeParts());
			}
		}
		return null;
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
	@Override
	public Iterable<AttributeMetaData> getOwnAtomicAttributes()
	{
		List<AttributeMetaData> atomicAttrs = new ArrayList<>();
		getOwnAtomicAttributesRec(getOwnAttributes(), atomicAttrs);
		return atomicAttrs;
	}

	private void getOwnAtomicAttributesRec(Iterable<AttributeMetaData> attrs, List<AttributeMetaData> atomicAttrs)
	{
		attrs.forEach(attr -> {
			if (attr.getDataType().getEnumType() == COMPOUND)
			{
				getOwnAtomicAttributesRec(attr.getAttributeParts(), atomicAttrs);
			}
			else
			{
				atomicAttrs.add(attr);
			}
		});
	}
}