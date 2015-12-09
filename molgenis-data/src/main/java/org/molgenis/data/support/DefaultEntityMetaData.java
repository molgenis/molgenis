package org.molgenis.data.support;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AttributeChangeListener;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.PackageChangeListener;
import org.molgenis.util.CaseInsensitiveLinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

public class DefaultEntityMetaData implements EditableEntityMetaData
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultEntityMetaData.class);

	private final String simpleName;
	private final Class<? extends Entity> entityClass;

	private Package package_;
	private String label;
	private final Map<String, String> labelByLanguageCode = new HashMap<>();
	private boolean abstract_;
	private String description;
	private final Map<String, String> descriptionByLanguageCode = new HashMap<>();
	private EntityMetaData extends_;
	private String backend;
	private final Map<String, AttributeMetaData> attributes;
	private String idAttrName;
	private String labelAttrName;

	// bookkeeping to improve performance of getters
	private final AttributeChangeListener attrChangeListener;
	private PackageChangeListener packageChangeListener;
	private transient String cachedName;
	private transient Map<String, AttributeMetaData> cachedAllAttrs;
	private transient List<AttributeMetaData> cachedAtomicAttrs;
	private transient AttributeMetaData cachedIdAttr;
	private transient AttributeMetaData cachedLabelAttr;
	private transient List<AttributeMetaData> cachedLookupAttrs;
	private transient Boolean cachedHasAttrWithExpression;

	public DefaultEntityMetaData(String simpleName)
	{
		this(simpleName, Entity.class);
	}

	public DefaultEntityMetaData(String simpleName, Package package_)
	{
		this(simpleName, Entity.class, package_);
	}

	public DefaultEntityMetaData(String simpleName, Class<? extends Entity> entityClass)
	{
		this(simpleName, entityClass, null);
	}

	public DefaultEntityMetaData(String simpleName, Class<? extends Entity> entityClass, Package package_)
	{
		this.simpleName = requireNonNull(simpleName);
		this.entityClass = requireNonNull(entityClass);
		setPackage(package_);
		this.attributes = new CaseInsensitiveLinkedHashMap<>();

		this.attrChangeListener = new AttributeChangeListenerImpl(this);
	}

	/**
	 * Copy-constructor
	 * 
	 * @param entityMetaData
	 */
	public DefaultEntityMetaData(EntityMetaData entityMetaData)
	{
		this(entityMetaData.getName(), entityMetaData);
	}

	public DefaultEntityMetaData(String simpleName, EntityMetaData entityMetaData)
	{
		this.simpleName = simpleName;
		this.entityClass = entityMetaData.getEntityClass();
		setPackage(entityMetaData.getPackage());
		this.label = entityMetaData.getLabel();
		for (String languageCode : entityMetaData.getLabelLanguageCodes())
		{
			setLabel(languageCode, entityMetaData.getLabel(languageCode));
		}

		this.abstract_ = entityMetaData.isAbstract();
		this.description = entityMetaData.getDescription();
		for (String languageCode : entityMetaData.getDescriptionLanguageCodes())
		{
			setDescription(languageCode, entityMetaData.getDescription(languageCode));
		}

		EntityMetaData extends_ = entityMetaData.getExtends();
		this.extends_ = extends_ != null ? new DefaultEntityMetaData(extends_) : null;
		this.backend = entityMetaData.getBackend();
		AttributeMetaData idAttr = entityMetaData.getIdAttribute();
		this.idAttrName = idAttr != null ? idAttr.getName() : null;
		AttributeMetaData labelAttr = entityMetaData.getLabelAttribute();
		this.labelAttrName = labelAttr != null ? labelAttr.getName() : null;
		this.attributes = new CaseInsensitiveLinkedHashMap<>();

		this.attrChangeListener = new AttributeChangeListenerImpl(this);

		addAllAttributeMetaData(entityMetaData.getOwnAttributes());
	}

	@Override
	public String getSimpleName()
	{
		return simpleName;
	}

	@Override
	public String getName()
	{
		return getCachedName();
	}

	@Override
	public Package getPackage()
	{
		return package_;
	}

	@Override
	public EditableEntityMetaData setPackage(Package package_)
	{
		// lazy init
		if (packageChangeListener == null)
		{
			this.packageChangeListener = new PackageChangeListenerImpl(this);
		}

		if (this.package_ != null)
		{
			this.package_.removeChangeListener(packageChangeListener.getId());
		}
		if (package_ != null)
		{
			package_.addChangeListener(packageChangeListener);
		}
		this.package_ = package_;
		clearCache();
		return this;
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return entityClass;
	}

	@Override
	public String getLabel()
	{
		return label != null ? label : getSimpleName();
	}

	@Override
	public EditableEntityMetaData setLabel(String label)
	{
		this.label = label;
		return this;
	}

	@Override
	public boolean isAbstract()
	{
		return abstract_;
	}

	@Override
	public EditableEntityMetaData setAbstract(boolean abstract_)
	{
		this.abstract_ = abstract_;
		return this;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public EditableEntityMetaData setDescription(String description)
	{
		this.description = description;
		return this;
	}

	@Override
	public EntityMetaData getExtends()
	{
		return extends_;
	}

	@Override
	public EditableEntityMetaData setExtends(EntityMetaData extends_)
	{
		this.extends_ = extends_;
		return this;
	}

	@Override
	public String getBackend()
	{
		return backend;
	}

	@Override
	public EditableEntityMetaData setBackend(String backend)
	{
		this.backend = backend;
		return this;
	}

	@Override
	public AttributeMetaData getAttribute(String attributeName)
	{
		AttributeMetaData attr = getCachedAllAttrs().get(attributeName);
		if (attr == null && extends_ != null)
		{
			attr = extends_.getAttribute(attributeName);
		}
		return attr;
	}

	@Override
	public Iterable<AttributeMetaData> getAttributes()
	{
		Iterable<AttributeMetaData> attrs = attributes.values();
		if (extends_ != null)
		{
			attrs = Iterables.concat(extends_.getAttributes(), attrs);
		}
		return attrs;
	}

	@Override
	public Iterable<AttributeMetaData> getOwnAttributes()
	{
		return attributes.values();
	}

	@Override
	public Iterable<AttributeMetaData> getAtomicAttributes()
	{
		Iterable<AttributeMetaData> atomicAttrs = getCachedAtomicAttrs();
		if (extends_ != null)
		{
			atomicAttrs = Iterables.concat(extends_.getAtomicAttributes(), atomicAttrs);
		}
		return atomicAttrs;
	}

	@Override
	public Iterable<AttributeMetaData> getOwnAtomicAttributes()
	{
		return getCachedAtomicAttrs();
	}

	@Override
	public String getDescription(String languageCode)
	{
		String description = descriptionByLanguageCode.get(languageCode);
		return description != null ? description : getDescription();
	}

	@Override
	public void addAttributeMetaData(AttributeMetaData attr)
	{
		attr.addChangeListener(attrChangeListener);
		attributes.put(attr.getName(), attr);
		clearCache();
	}

	@Override
	public EditableEntityMetaData setDescription(String languageCode, String description)
	{
		this.descriptionByLanguageCode.put(languageCode, description);
		return this;
	}

	@Override
	public Set<String> getDescriptionLanguageCodes()
	{
		return Collections.unmodifiableSet(descriptionByLanguageCode.keySet());
	}

	@Override
	public void removeAttributeMetaData(AttributeMetaData attr)
	{
		attr.removeChangeListener(attrChangeListener.getId());
		attributes.remove(attr.getName());
		clearCache();
	}

	@Override
	public void addAllAttributeMetaData(Iterable<AttributeMetaData> attrs)
	{
		attrs.forEach(attr -> {
			attr.addChangeListener(attrChangeListener);
			attributes.put(attr.getName(), attr);
		});
		clearCache();
	}

	@Override
	public DefaultAttributeMetaData addAttribute(String name)
	{
		DefaultAttributeMetaData result = new DefaultAttributeMetaData(name);
		this.addAttributeMetaData(result);
		return result;
	}

	@Override
	public boolean hasAttributeWithExpression()
	{
		return getCachedHasAttrWithExpression();
	}

	@Override
	public AttributeMetaData getIdAttribute()
	{
		return getCachedIdAttr();
	}

	@Override
	public void setIdAttribute(String idAttrName)
	{
		this.idAttrName = idAttrName;
		if (this.labelAttrName == null)
		{
			this.labelAttrName = idAttrName;
		}
		clearCache();
	}

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		return getCachedLabelAttr();
	}

	@Override
	public void setLabelAttribute(String labelAttrName)
	{
		this.labelAttrName = labelAttrName;
		if (this.labelAttrName == null)
		{
			this.labelAttrName = idAttrName;
		}
		clearCache();
	}

	@Override
	public Iterable<AttributeMetaData> getLookupAttributes()
	{
		Iterable<AttributeMetaData> lookupAttrs = getCachedLookupAttrs();
		if (extends_ != null)
		{
			lookupAttrs = Iterables.concat(extends_.getLookupAttributes(), lookupAttrs);
		}
		return lookupAttrs;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof EntityMetaData)) return false;
		EntityMetaData other = (EntityMetaData) obj;
		if (getName() == null)
		{
			if (other.getName() != null) return false;
		}
		else if (!getName().equals(other.getName())) return false;
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder("\nEntityMetaData(name='");
		strBuilder.append(this.getName()).append('\'');
		if (isAbstract()) strBuilder.append(" abstract='true'");
		if (getExtends() != null) strBuilder.append(" extends='" + getExtends().getName()).append('\'');
		if (getIdAttribute() != null) strBuilder.append(" idAttribute='").append(getIdAttribute().getName())
				.append('\'');
		if (getDescription() != null) strBuilder.append(" description='")
				.append(getDescription().substring(0, Math.min(25, getDescription().length())))
				.append(getDescription().length() > 25 ? "...'" : "'");
		strBuilder.append(')');
		for (AttributeMetaData att : this.getAttributes())
		{
			strBuilder.append("\n\t").append(att.toString());
		}
		return strBuilder.toString();
	}

	private String getCachedName()
	{
		if (cachedName == null)
		{
			if (package_ != null && !Package.DEFAULT_PACKAGE_NAME.equals(package_.getName()))
			{
				StringBuilder sb = new StringBuilder();
				sb.append(package_.getName());
				sb.append(Package.PACKAGE_SEPARATOR);
				sb.append(simpleName);
				cachedName = sb.toString();
			}
			else
			{
				cachedName = simpleName;
			}
		}
		return cachedName;
	}

	private Map<String, AttributeMetaData> getCachedAllAttrs()
	{
		if (cachedAllAttrs == null)
		{
			cachedAllAttrs = new CaseInsensitiveLinkedHashMap<>();
			fillCachedAllAttrsRec(attributes.values());
		}
		return cachedAllAttrs;
	}

	private void fillCachedAllAttrsRec(Iterable<AttributeMetaData> attrs)
	{
		attrs.forEach(attr -> {
			cachedAllAttrs.put(attr.getName(), attr);
			if (attr.getDataType().getEnumType() == COMPOUND)
			{
				fillCachedAllAttrsRec(attr.getAttributeParts());
			}
		});
	}

	private List<AttributeMetaData> getCachedAtomicAttrs()
	{
		if (cachedAtomicAttrs == null)
		{
			cachedAtomicAttrs = new ArrayList<>();
			fillCachedAtomicAttrsRec(attributes.values());
		}
		return cachedAtomicAttrs;
	}

	private void fillCachedAtomicAttrsRec(Iterable<AttributeMetaData> attrs)
	{
		attrs.forEach(attr -> {

			if (attr.getDataType().getEnumType() == COMPOUND)
			{
				fillCachedAtomicAttrsRec(attr.getAttributeParts());
			}
			else
			{
				cachedAtomicAttrs.add(attr);
			}
		});
	}

	private AttributeMetaData getCachedIdAttr()
	{
		if (cachedIdAttr == null)
		{
			if (idAttrName != null)
			{
				cachedIdAttr = getCachedAllAttrs().get(idAttrName);
				if (cachedIdAttr == null && extends_ != null)
				{
					cachedIdAttr = extends_.getAttribute(idAttrName);
				}
			}
			else
			{
				// if the entity id attribute name was not set search for the id attribute
				Stream<AttributeMetaData> stream = getCachedAllAttrs().values().stream();
				cachedIdAttr = stream.filter(AttributeMetaData::isIdAtrribute).findFirst().orElse(null);

				if (cachedIdAttr == null && extends_ != null)
				{
					cachedIdAttr = extends_.getIdAttribute();
				}
			}

			if (cachedIdAttr == null && !abstract_)
			{
				LOG.warn("missing required id attribute for entity [{}]", getName());
				// throw exception once https://github.com/molgenis/molgenis/issues/1400 is fixed
			}
		}
		return cachedIdAttr;
	}

	private AttributeMetaData getCachedLabelAttr()
	{
		if (cachedLabelAttr == null)
		{
			if (labelAttrName != null)
			{
				cachedLabelAttr = getCachedAllAttrs().get(labelAttrName);
				if (cachedLabelAttr == null && extends_ != null)
				{
					cachedLabelAttr = extends_.getAttribute(idAttrName);
				}
			}
			else
			{
				// if the entity label attribute name was not set search for the label attribute
				Stream<AttributeMetaData> stream = getCachedAllAttrs().values().stream();
				cachedLabelAttr = stream.filter(AttributeMetaData::isLabelAttribute).findFirst().orElse(null);

				if (cachedLabelAttr == null && extends_ != null)
				{
					cachedLabelAttr = extends_.getLabelAttribute();
				}

				if (cachedLabelAttr == null)
				{
					cachedLabelAttr = getCachedIdAttr();
				}
			}
		}
		return cachedLabelAttr;
	}

	private List<AttributeMetaData> getCachedLookupAttrs()
	{
		if (cachedLookupAttrs == null)
		{
			cachedLookupAttrs = new ArrayList<>();
			fillCachedLookupAttrsRec(attributes.values());
		}
		return cachedLookupAttrs;
	}

	private void fillCachedLookupAttrsRec(Iterable<AttributeMetaData> attrs)
	{
		attrs.forEach(attr -> {
			if (attr.isLookupAttribute())
			{
				cachedLookupAttrs.add(attr);
			}
			if (attr.getDataType().getEnumType() == COMPOUND)
			{
				fillCachedLookupAttrsRec(attr.getAttributeParts());
			}
		});
	}

	private boolean getCachedHasAttrWithExpression()
	{
		if (cachedHasAttrWithExpression == null)
		{
			Stream<AttributeMetaData> stream = getCachedAllAttrs().values().stream();
			cachedHasAttrWithExpression = stream.anyMatch(attr -> attr.getExpression() != null);
			if (!cachedHasAttrWithExpression && extends_ != null)
			{
				cachedHasAttrWithExpression = extends_.hasAttributeWithExpression();
			}
		}
		return cachedHasAttrWithExpression;
	}

	private void clearCache()
	{
		cachedName = null;
		cachedAllAttrs = null;
		cachedAtomicAttrs = null;
		cachedIdAttr = null;
		cachedLabelAttr = null;
		cachedLookupAttrs = null;
		cachedHasAttrWithExpression = null;
	}

	private static class AttributeChangeListenerImpl implements AttributeChangeListener
	{
		private final DefaultEntityMetaData entityMeta;

		public AttributeChangeListenerImpl(DefaultEntityMetaData entityMeta)
		{
			this.entityMeta = requireNonNull(entityMeta);
		}

		@Override
		public String getId()
		{
			return entityMeta.getName();
		}

		@Override
		public void onChange(String attrName, AttributeMetaData attr)
		{
			entityMeta.clearCache();
		}
	}

	private static class PackageChangeListenerImpl implements PackageChangeListener
	{
		private final DefaultEntityMetaData entityMeta;

		public PackageChangeListenerImpl(DefaultEntityMetaData entityMeta)
		{
			this.entityMeta = requireNonNull(entityMeta);
		}

		@Override
		public String getId()
		{
			return entityMeta.getName();
		}

		@Override
		public void onChange(Package package_)
		{
			entityMeta.clearCache();
		}
	}

	@Override
	public AttributeMetaData getLabelAttribute(String languageCode)
	{
		for (AttributeMetaData attribute : getAtomicAttributes())
		{
			if (attribute.isLabelAttribute() && attribute.getName().endsWith('-' + languageCode))
			{
				return attribute;
			}
		}

		return getLabelAttribute();
	}

	@Override
	public String getLabel(String languageCode)
	{
		String label = labelByLanguageCode.get(languageCode);
		return label != null ? label : getLabel();
	}

	@Override
	public EditableEntityMetaData setLabel(String languageCode, String label)
	{
		this.labelByLanguageCode.put(languageCode, label);
		return this;
	}

	@Override
	public Set<String> getLabelLanguageCodes()
	{
		return Collections.unmodifiableSet(labelByLanguageCode.keySet());
	}

}