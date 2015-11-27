package org.molgenis.data.support;

import static java.util.stream.StreamSupport.stream;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.util.CaseInsensitiveLinkedHashMap;

import com.google.common.collect.Lists;

public class DefaultEntityMetaData extends AbstractEntityMetaData implements EditableEntityMetaData
{
	private String simpleName;
	private final Map<String, AttributeMetaData> attributes = new CaseInsensitiveLinkedHashMap<AttributeMetaData>();
	private final Class<? extends Entity> entityClass;
	private String label;
	private boolean abstract_ = false;
	private String description;
	private EntityMetaData extends_;
	private Package pack;
	private String backend;

	public DefaultEntityMetaData(String simpleName)
	{
		this(simpleName, Entity.class);
	}

	public DefaultEntityMetaData(String simpleName, Package p)
	{
		this(simpleName, Entity.class, p);
	}

	public DefaultEntityMetaData(String simpleName, Class<? extends Entity> entityClass)
	{
		if (simpleName == null) throw new IllegalArgumentException("Name cannot be null");
		if (entityClass == null) throw new IllegalArgumentException("EntityClass cannot be null");
		this.simpleName = simpleName;
		this.entityClass = entityClass;
	}

	public DefaultEntityMetaData(String simpleName, Class<? extends Entity> entityClass, Package pack)
	{
		if (simpleName == null) throw new IllegalArgumentException("Name cannot be null");
		if (entityClass == null) throw new IllegalArgumentException("EntityClass cannot be null");
		this.simpleName = simpleName;
		this.entityClass = entityClass;
		this.pack = pack;
	}

	public DefaultEntityMetaData(String newName, EntityMetaData entityMetaData)
	{
		this(entityMetaData);
		this.simpleName = newName;
	}

	/**
	 * Copy-constructor
	 * 
	 * @param entityMetaData
	 */
	public DefaultEntityMetaData(EntityMetaData entityMetaData)
	{
		this.simpleName = entityMetaData.getSimpleName();
		this.pack = entityMetaData.getPackage();
		this.entityClass = entityMetaData.getEntityClass();
		this.label = entityMetaData.getLabel();
		this.abstract_ = entityMetaData.isAbstract();
		this.description = entityMetaData.getDescription();
		EntityMetaData extends_ = entityMetaData.getExtends();
		this.extends_ = extends_ != null ? new DefaultEntityMetaData(extends_) : null;
		this.backend = entityMetaData.getBackend();

		Iterable<AttributeMetaData> attributes = entityMetaData.getAttributes();
		if (attributes != null)
		{
			addAllAttributeMetaData(attributes);
		}
		AttributeMetaData idAttribute = entityMetaData.getIdAttribute();
		if (idAttribute != null)
		{
			setIdAttribute(idAttribute.getName());
		}
		AttributeMetaData labelAttribute = entityMetaData.getLabelAttribute();
		if (labelAttribute != null)
		{
			setLabelAttribute(labelAttribute.getName());
		}
	}

	@Override
	public Package getPackage()
	{
		return pack;
	}

	@Override
	public EditableEntityMetaData setPackage(Package pack)
	{
		this.pack = pack;
		return this;
	}

	@Override
	public String getSimpleName()
	{
		return simpleName;
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
	public void addAttributeMetaData(AttributeMetaData attributeMetaData)
	{
		if (attributeMetaData == null) throw new IllegalArgumentException("AttributeMetaData cannot be null");
		if (attributeMetaData.getName() == null)
			throw new IllegalArgumentException("Name of the AttributeMetaData cannot be null");
		if (attributeMetaData.isLabelAttribute()) setLabelAttribute(attributeMetaData.getName());
		if (attributeMetaData.isIdAtrribute()) setIdAttribute(attributeMetaData.getName());

		attributes.put(attributeMetaData.getName().toLowerCase(), attributeMetaData);
	}

	public void removeAttributeMetaData(AttributeMetaData attributeMetaData)
	{
		attributes.remove(attributeMetaData.getName());
	}

	public void addAllAttributeMetaData(Iterable<AttributeMetaData> attributeMetaDataList)
	{
		attributeMetaDataList.forEach(this::addAttributeMetaData);
	}

	@Override
	public List<AttributeMetaData> getAttributes()
	{
		Set<AttributeMetaData> result = new LinkedHashSet<>();
		if (getExtends() != null)
		{
			for (AttributeMetaData att : getExtends().getAttributes())
			{
				result.add(att);
			}
		}
		result.addAll(attributes.values());
		return Collections.unmodifiableList(Lists.newArrayList(result));
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
	public Class<? extends Entity> getEntityClass()
	{
		return entityClass;
	}

	@Override
	public DefaultAttributeMetaData addAttribute(String name)
	{
		DefaultAttributeMetaData result = new DefaultAttributeMetaData(name);
		this.addAttributeMetaData(result);
		return result;
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
	public boolean hasAttributeWithExpression()
	{
		return stream(getAtomicAttributes().spliterator(), false).anyMatch(attr -> attr.getExpression() != null);
	}

}