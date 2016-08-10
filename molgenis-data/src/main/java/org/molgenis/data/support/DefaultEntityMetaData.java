package org.molgenis.data.support;

import com.google.common.collect.Iterables;
import org.molgenis.data.*;
import org.molgenis.data.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;

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
	private AttributeMetaData ownIdAttr;
	private AttributeMetaData ownLabelAttr;
	private Map<String, AttributeMetaData> ownLookupAttrs;

	// bookkeeping to improve performance of getters
	private final AttributeChangeListener attrChangeListener;
	private PackageChangeListener packageChangeListener;
	private transient String cachedName;
	private transient Map<String, AttributeMetaData> cachedAllAttrs;
	private transient List<AttributeMetaData> cachedAtomicAttrs;
	private transient AttributeMetaData cachedIdAttr;
	private transient AttributeMetaData cachedLabelAttr;
	private transient Map<String, AttributeMetaData> cachedLookupAttrs;
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
		this.attributes = new LinkedCaseInsensitiveMap<>();

		this.attrChangeListener = new AttributeChangeListenerImpl(this);
	}

	/**
	 * Copy-constructor
	 *
	 * @param entityMetaData
	 */
	public DefaultEntityMetaData(EntityMetaData entityMetaData)
	{
		this(entityMetaData.getSimpleName(), entityMetaData);
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
		this.attributes = new LinkedCaseInsensitiveMap<>();

		this.attrChangeListener = new AttributeChangeListenerImpl(this);

		addAllAttributeMetaData(entityMetaData.getOwnAttributes());
		this.ownIdAttr = entityMetaData.getOwnIdAttribute();
		this.ownLabelAttr = entityMetaData.getOwnLabelAttribute();
		this.ownLookupAttrs = stream(entityMetaData.getOwnLookupAttributes().spliterator(), false)
				.collect(toMap(AttributeMetaData::getName, Function.<AttributeMetaData>identity(), (u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				}, LinkedCaseInsensitiveMap::new));
		if (ownLookupAttrs.isEmpty()) ownLookupAttrs = null;
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
		clearCache();
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
	public void addAttributeMetaData(AttributeMetaData attr, AttributeRole... attrTypes)
	{
		attr.addChangeListener(attrChangeListener);
		attributes.put(attr.getName(), attr);
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
	public DefaultAttributeMetaData addAttribute(String name, AttributeRole... attrTypes)
	{
		DefaultAttributeMetaData attr = new DefaultAttributeMetaData(name);
		this.addAttributeMetaData(attr, attrTypes);
		return attr;
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
	public AttributeMetaData getOwnIdAttribute()
	{
		return ownIdAttr;
	}

	@Override
	public void setIdAttribute(AttributeMetaData idAttr)
	{
		if (idAttr instanceof DefaultAttributeMetaData)
		{
			DefaultAttributeMetaData editableAttr = (DefaultAttributeMetaData) idAttr;
			editableAttr.setReadOnly(true);
			editableAttr.setUnique(true);
			editableAttr.setNillable(false);
		}
		this.ownIdAttr = requireNonNull(idAttr);
		clearCache();
	}

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		return getCachedLabelAttr();
	}

	@Override
	public AttributeMetaData getOwnLabelAttribute()
	{
		return ownLabelAttr;
	}

	@Override
	public void setLabelAttribute(AttributeMetaData labelAttr)
	{
		this.ownLabelAttr = requireNonNull(labelAttr);
		clearCache();
	}

	@Override
	public AttributeMetaData getLookupAttribute(String attrName)
	{
		return getCachedLookupAttrs().get(attrName);
	}

	@Override
	public Iterable<AttributeMetaData> getLookupAttributes()
	{
		return getCachedLookupAttrs().values();
	}

	@Override
	public Iterable<AttributeMetaData> getOwnLookupAttributes()
	{
		return ownLookupAttrs != null ? unmodifiableCollection(ownLookupAttrs.values()) : emptyList();
	}

	@Override
	public void addLookupAttribute(AttributeMetaData lookupAttr)
	{
		if (this.ownLookupAttrs == null) this.ownLookupAttrs = new LinkedCaseInsensitiveMap<>();
		this.ownLookupAttrs.put(lookupAttr.getName(), lookupAttr);
		clearCache();
	}

	@Override
	public void setLookupAttributes(Stream<AttributeMetaData> lookupAttrs)
	{
		this.ownLookupAttrs = lookupAttrs
				.collect(toMap(AttributeMetaData::getName, Function.<AttributeMetaData>identity(), (u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				}, LinkedCaseInsensitiveMap::new));
		if (ownLookupAttrs.isEmpty()) ownLookupAttrs = null;
		clearCache();
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
		if (getIdAttribute() != null)
			strBuilder.append(" idAttribute='").append(getIdAttribute().getName()).append('\'');
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
			cachedName = EntityMetaDataUtils.buildFullName(package_, simpleName);
		}
		return cachedName;
	}

	private Map<String, AttributeMetaData> getCachedAllAttrs()
	{
		if (cachedAllAttrs == null)
		{
			cachedAllAttrs = new LinkedCaseInsensitiveMap<>();
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
			if (ownIdAttr != null)
			{
				cachedIdAttr = ownIdAttr;
			}
			else if (extends_ != null)
			{
				cachedIdAttr = extends_.getIdAttribute();
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
			if (ownLabelAttr != null)
			{
				cachedLabelAttr = ownLabelAttr;
			}
			else if (extends_ != null)
			{
				cachedLabelAttr = extends_.getLabelAttribute();
			}

			if (cachedLabelAttr == null)
			{
				cachedLabelAttr = getCachedIdAttr();
			}
		}
		return cachedLabelAttr;
	}

	private Map<String, AttributeMetaData> getCachedLookupAttrs()
	{
		if (cachedLookupAttrs == null)
		{
			cachedLookupAttrs = new LinkedCaseInsensitiveMap<>();
			if (ownLookupAttrs != null)
			{
				cachedLookupAttrs.putAll(ownLookupAttrs);
			}
			if (extends_ != null)
			{
				Map<String, AttributeMetaData> extendedLookupAttributes = stream(
						extends_.getLookupAttributes().spliterator(), false)
						.collect(toMap(AttributeMetaData::getName, Function.<AttributeMetaData>identity(), (u, v) ->
						{
							throw new IllegalStateException(String.format("Duplicate key %s", u));
						}, LinkedCaseInsensitiveMap::new));
				cachedLookupAttrs.putAll(extendedLookupAttributes);
			}
		}
		return cachedLookupAttrs;
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
		AttributeMetaData labelAttr = getLabelAttribute();
		String labelAttributeName = labelAttr.getName();

		if (labelAttributeName.contains("-"))
		{
			labelAttributeName = labelAttributeName.substring(0, labelAttributeName.length() - 3);
		}
		AttributeMetaData i18nLabelAttr = getCachedAllAttrs().get(labelAttributeName + '-' + languageCode);
		if (i18nLabelAttr == null)
		{
			i18nLabelAttr = getCachedAllAttrs().get(labelAttributeName);
		}

		return i18nLabelAttr != null ? i18nLabelAttr : labelAttr;
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