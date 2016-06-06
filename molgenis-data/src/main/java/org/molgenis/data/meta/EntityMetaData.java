package org.molgenis.data.meta;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.removeAll;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
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
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.support.AttributeMetaDataUtils.getI18nAttributeName;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;

import com.google.common.collect.Iterables;

/**
 * EntityMetaData defines the structure and attributes of an Entity. Attributes are unique. Other software components
 * can use this to interact with Entity and/or to configure backends and frontends, including Repository instances.
 */
public class EntityMetaData extends SystemEntity
{
	protected EntityMetaData()
	{

	}

	public EntityMetaData(Entity entity)
	{
		super(entity);
	}

	public EntityMetaData(EntityMetaDataMetaData entityMetaDataMetaData)
	{
		init(entityMetaDataMetaData);
	}

	public EntityMetaData(String simpleName, EntityMetaDataMetaData entityMetaDataMetaData)
	{
		this(simpleName, null, entityMetaDataMetaData);
	}

	public EntityMetaData(String simpleName, String packageName, EntityMetaDataMetaData entityMetaDataMetaData)
	{
		init(entityMetaDataMetaData);
		set(SIMPLE_NAME, simpleName);
		set(FULL_NAME, packageName != null ? packageName + PACKAGE_SEPARATOR + simpleName : simpleName);
		setAbstract(false);
	}

	@Deprecated
	public EntityMetaData(String simpleName)
	{
		this(simpleName, Entity.class);
	}

	@Deprecated
	public EntityMetaData(String simpleName, Package package_)
	{
		this(simpleName, Entity.class, package_);
	}

	@Deprecated
	public EntityMetaData(String simpleName, Class<? extends Entity> entityClass)
	{
		this(simpleName, entityClass, null);
	}

	@Deprecated
	public EntityMetaData(String simpleName, Class<? extends Entity> entityClass, Package package_)
	{
		init(getApplicationContext().getBean(EntityMetaDataMetaData.class));
		String fullName;
		if (package_ != null)
		{
			fullName = package_.getName() + PACKAGE_SEPARATOR + simpleName;
		}
		else
		{
			fullName = simpleName;
		}
		set(SIMPLE_NAME, simpleName);
		set(FULL_NAME, fullName);
		set(PACKAGE, package_);
		setAbstract(false);
	}

	/**
	 * Copy-factory (instead of copy-constructor to avoid accidental method overloading to {@link #EntityMetaData(Entity)})
	 *
	 * @param entityMeta entity meta data
	 * @return deep copy of entity meta data
	 */
	public static EntityMetaData newInstance(EntityMetaData entityMeta)
	{
		Entity entityCopy = MapEntity.newInstance(entityMeta);
		return new EntityMetaData(entityCopy);
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
	public String getSimpleName()
	{
		return getString(SIMPLE_NAME);
	}

	public EntityMetaData setSimpleName(String simpleName)
	{
		set(SIMPLE_NAME, simpleName);
		updateFullName();
		return this;
	}

	/**
	 * Optional human readable longer label
	 *
	 * @return entity label
	 */
	public String getLabel()
	{
		String label = getString(LABEL);
		return label != null ? label : getString(FULL_NAME);
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
		idAttr.setReadOnly(true);
		idAttr.setUnique(true);
		idAttr.setNillable(false);

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
	public AttributeMetaData getLabelAttribute(String languageCode)
	{
		AttributeMetaData labelAttr = getLabelAttribute();
		AttributeMetaData i18nLabelAttr = getAttribute(labelAttr.getName() + '-' + languageCode);
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
		set(LABEL_ATTRIBUTE, lookupAttrs);
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
		Iterable<AttributeMetaData> atomicAttrs = getOwnAtomicAttributes();
		EntityMetaData extends_ = getExtends();
		if (extends_ != null)
		{
			atomicAttrs = concat(atomicAttrs, extends_.getAtomicAttributes());
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
	 * Get attribute by name (case insensitive), returns null if not found
	 *
	 * @return attribute or <tt>null</tt>
	 */
	public AttributeMetaData getAttribute(String attrName)
	{
		return getAttributeRec(attrName, getAttributes());
	}

	public void addAttribute(AttributeMetaData attr, AttributeRole... attrTypes)
	{
		Iterable<AttributeMetaData> attrs = getEntities(ATTRIBUTES, AttributeMetaData.class);
		set(ATTRIBUTES, concat(attrs, singletonList(attr)));
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

	public void addAttributes(Iterable<AttributeMetaData> attrs)
	{
		attrs.forEach(this::addAttribute);
	}

	/**
	 * Returns whether this entity has a attribute with expression
	 *
	 * @return whether this entity has a attribute with expression
	 */
	public boolean hasAttributeWithExpression()
	{
		return stream(getAtomicAttributes().spliterator(), false).anyMatch(attr -> attr.getExpression() != null);
	}

	public void removeAttribute(AttributeMetaData attr)
	{
		// FIXME does not remove attr if attr is located in a compound attr
		Iterable<AttributeMetaData> existingAttrs = getEntities(ATTRIBUTES, AttributeMetaData.class);
		List<AttributeMetaData> filteredAttrs = stream(existingAttrs.spliterator(), false)
				.filter(existingAttr -> !existingAttr.getName().equals(attr.getName())).collect(Collectors.toList());
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
				AttributeMetaData attributeRec = getAttributeRec(attrName, attr.getAttributeParts());
				if (attributeRec != null)
				{
					return attributeRec;
				}
			}
		}
		return null;
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

	private void getOwnAllAttributesRec(Iterable<AttributeMetaData> attrs, List<AttributeMetaData> allAttrs)
	{
		attrs.forEach(attr -> {
			if (attr.getDataType().getEnumType() == COMPOUND)
			{
				getOwnAllAttributesRec(attr.getAttributeParts(), allAttrs);
			}
			allAttrs.add(attr);
		});
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

	public enum AttributeRole
	{
		ROLE_ID, ROLE_LABEL, ROLE_LOOKUP
	}
}