package org.molgenis.data.meta;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.meta.RootSystemPackage.PACKAGE_SYSTEM;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.molgenis.data.Entity;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class SystemEntityMetaData implements EntityMetaData
{
	private AttributeMetaDataFactory attributeMetaDataFactory;

	private final String entityName;
	private final String packageName;
	private EntityMetaData entityMetaData;

	@Override
	public void addAttribute(AttributeMetaData attr, AttributeRole... attrTypes)
	{
		entityMetaData.addAttribute(attr, attrTypes);
	}

	public SystemEntityMetaData(String entityName)
	{
		this(entityName, PACKAGE_SYSTEM);
	}

	public SystemEntityMetaData(String entityName, String packageName)
	{
		this.entityName = requireNonNull(entityName);
		this.packageName = requireNonNull(packageName);
		if (!packageName.startsWith(PACKAGE_SYSTEM))
		{
			throw new IllegalArgumentException(
					format("Entity [%s] must be located in package [%s] instead of [%s]", entityName, PACKAGE_SYSTEM,
							packageName));
		}
	}

	public void bootstrap(EntityMetaDataMetaData entityMetaDataMetaData)
	{
		entityMetaData = new EntityMetaDataImpl(entityName, packageName, entityMetaDataMetaData);
		init();
	}

	public abstract void init();

	@Override
	public AttributeMetaData addAttribute(String attrName, AttributeRole... attrTypes)
	{
		return addAttribute(attrName, null, attrTypes);
	}

	@Override
	public void addAttributes(Iterable<AttributeMetaData> attrs)
	{
		entityMetaData.addAttributes(attrs);
	}

	@Override
	public void addLookupAttribute(AttributeMetaData lookupAttr)
	{
		entityMetaData.addLookupAttribute(lookupAttr);
	}

	@Override
	public void addTag(Tag tag)
	{
		entityMetaData.addTag(tag);
	}

	@Override
	public Iterable<AttributeMetaData> getAtomicAttributes()
	{
		return entityMetaData.getAtomicAttributes();
	}

	@Override
	public AttributeMetaData getAttribute(String attrName)
	{
		return entityMetaData.getAttribute(attrName);
	}

	@Override
	public Iterable<AttributeMetaData> getAttributes()
	{
		return entityMetaData.getAttributes();
	}

	@Override
	public String getBackend()
	{
		return entityMetaData.getBackend();
	}

	@Override
	public String getDescription()
	{
		return entityMetaData.getDescription();
	}

	@Override
	public String getDescription(String languageCode)
	{
		return entityMetaData.getDescription(languageCode);
	}

	@Override
	public EntityMetaData getExtends()
	{
		return entityMetaData.getExtends();
	}

	@Override
	public AttributeMetaData getIdAttribute()
	{
		return entityMetaData.getIdAttribute();
	}

	@Override
	public String getLabel()
	{
		return entityMetaData.getLabel();
	}

	@Override
	public String getLabel(String languageCode)
	{
		return entityMetaData.getLabel(languageCode);
	}

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		return entityMetaData.getLabelAttribute();
	}

	@Override
	public AttributeMetaData getLabelAttribute(String languageCode)
	{
		return entityMetaData.getLabelAttribute(languageCode);
	}

	@Override
	public AttributeMetaData getLookupAttribute(String lookupAttrName)
	{
		return entityMetaData.getLookupAttribute(lookupAttrName);
	}

	@Override
	public Iterable<AttributeMetaData> getLookupAttributes()
	{
		return entityMetaData.getLookupAttributes();
	}

	@Override
	public String getName()
	{
		return packageName + PACKAGE_SEPARATOR + entityName;
	}

	@Override
	public Iterable<AttributeMetaData> getOwnAtomicAttributes()
	{
		return entityMetaData.getOwnAtomicAttributes();
	}

	@Override
	public Iterable<AttributeMetaData> getOwnAttributes()
	{
		return entityMetaData.getOwnAttributes();
	}

	@Override
	public AttributeMetaData getOwnIdAttribute()
	{
		return entityMetaData.getOwnIdAttribute();
	}

	@Override
	public AttributeMetaData getOwnLabelAttribute()
	{
		return entityMetaData.getOwnLabelAttribute();
	}

	@Override
	public AttributeMetaData getOwnLabelAttribute(String languageCode)
	{
		return entityMetaData.getOwnLabelAttribute(languageCode);
	}

	@Override
	public Iterable<AttributeMetaData> getOwnLookupAttributes()
	{
		return entityMetaData.getOwnLookupAttributes();
	}

	@Override
	public Package getPackage()
	{
		return entityMetaData.getPackage();
	}

	@Override
	public String getSimpleName()
	{
		return entityName;
	}

	@Override
	public Iterable<Tag> getTags()
	{
		return entityMetaData.getTags();
	}

	@Override
	public boolean hasAttributeWithExpression()
	{
		return entityMetaData.hasAttributeWithExpression();
	}

	@Override
	public boolean isAbstract()
	{
		return entityMetaData.isAbstract();
	}

	@Override
	public void removeAttribute(AttributeMetaData attr)
	{
		entityMetaData.removeAttribute(attr);
	}

	@Override
	public void removeTag(Tag tag)
	{
		entityMetaData.removeTag(tag);
	}

	@Override
	public EntityMetaData setAbstract(boolean abstract_)
	{
		return entityMetaData.setAbstract(abstract_);
	}

	@Override
	public EntityMetaData setBackend(String backend)
	{
		return entityMetaData.setBackend(backend);
	}

	@Override
	public EntityMetaData setDescription(String description)
	{
		return entityMetaData.setDescription(description);
	}

	@Override
	public EntityMetaData setDescription(String languageCode, String description)
	{
		return entityMetaData.setDescription(languageCode, description);
	}

	@Override
	public EntityMetaData setExtends(EntityMetaData extends_)
	{
		return entityMetaData.setExtends(extends_);
	}

	@Override
	public EntityMetaData setIdAttribute(AttributeMetaData idAttr)
	{
		return entityMetaData.setIdAttribute(idAttr);
	}

	@Override
	public EntityMetaData setLabel(String label)
	{
		return entityMetaData.setLabel(label);
	}

	@Override
	public EntityMetaData setLabel(String languageCode, String label)
	{
		return entityMetaData.setLabel(languageCode, label);
	}

	@Override
	public EntityMetaData setLabelAttribute(AttributeMetaData labelAttr)
	{
		return entityMetaData.setLabelAttribute(labelAttr);
	}

	@Override
	public EntityMetaData setLookupAttributes(Iterable<AttributeMetaData> lookupAttrs)
	{
		return entityMetaData.setLookupAttributes(lookupAttrs);
	}

	@Override
	public EntityMetaData setName(String fullName)
	{
		return entityMetaData.setName(fullName);
	}

	@Override
	public EntityMetaData setOwnAttributes(Iterable<AttributeMetaData> attrs)
	{
		return entityMetaData.setOwnAttributes(attrs);
	}

	@Override
	public EntityMetaData setPackage(Package package_)
	{
		return entityMetaData.setPackage(package_);
	}

	@Override
	public EntityMetaData setSimpleName(String simpleName)
	{
		return entityMetaData.setSimpleName(simpleName);
	}

	@Override
	public EntityMetaData setTags(Iterable<Tag> tags)
	{
		return entityMetaData.setTags(tags);
	}

	@Override
	public Object get(String attributeName)
	{
		return entityMetaData.get(attributeName);
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return entityMetaData.getAttributeNames();
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return entityMetaData.getBoolean(attributeName);
	}

	@Override
	public Date getDate(String attributeName)
	{
		return entityMetaData.getDate(attributeName);
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return entityMetaData.getDouble(attributeName);
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		return entityMetaData.getEntities(attributeName);
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		return entityMetaData.getEntities(attributeName, clazz);
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		return entityMetaData.getEntity(attributeName);
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		return entityMetaData.getEntity(attributeName, clazz);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData.getEntityMetaData();
	}

	@Override
	public Object getIdValue()
	{
		return entityMetaData.getIdValue();
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return entityMetaData.getInt(attributeName);
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		return entityMetaData.getIntList(attributeName);
	}

	@Override
	public String getLabelValue()
	{
		return entityMetaData.getLabelValue();
	}

	@Override
	public List<String> getList(String attributeName)
	{
		return entityMetaData.getList(attributeName);
	}

	@Override
	public Long getLong(String attributeName)
	{
		return entityMetaData.getLong(attributeName);
	}

	@Override
	public String getString(String attributeName)
	{
		return entityMetaData.getString(attributeName);
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		return entityMetaData.getTimestamp(attributeName);
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		return entityMetaData.getUtilDate(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		entityMetaData.set(attributeName, value);
	}

	@Override
	public void set(Entity values)
	{
		entityMetaData.set(values);
	}

	public AttributeMetaData addAttribute(String attrName, AttributeMetaData parentAttr, AttributeRole... attrTypes)
	{
		AttributeMetaData attr = attributeMetaDataFactory.create();
		attr.setName(attrName);
		if (parentAttr != null)
		{
			parentAttr.addAttributePart(attr);
			// FIXME assign roles, see super.addAttribute(AttributeMetaData attr, AttributeRole... attrTypes)
		}
		else
		{
			entityMetaData.addAttribute(attr, attrTypes);
		}
		return attr;
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setAttributeMetaDataFactory(AttributeMetaDataFactory attributeMetaDataFactory)
	{
		this.attributeMetaDataFactory = requireNonNull(attributeMetaDataFactory);
	}

	@Override
	public Iterable<AttributeMetaData> getAllAttributes()
	{
		return entityMetaData.getAllAttributes();
	}

	@Override
	public Iterable<AttributeMetaData> getOwnAllAttributes()
	{
		return entityMetaData.getOwnAllAttributes();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof EntityMetaData)) return false;

		EntityMetaData that = (EntityMetaData) o;

		return entityMetaData.equals(that);
	}

	@Override
	public int hashCode()
	{
		return entityMetaData.hashCode();
	}

	@Override
	public String toString()
	{
		return entityMetaData.toString();
	}
}
