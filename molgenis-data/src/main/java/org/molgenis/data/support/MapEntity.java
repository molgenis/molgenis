package org.molgenis.data.support;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.util.EntityUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Simple Entity implementation based on a Map
 */
public class MapEntity extends AbstractEntity
{
	private static final long serialVersionUID = -8283375007931769373L;
	private EntityMetaData entityMetaData;
	private Map<String, Object> values = new LinkedCaseInsensitiveMap<>();
	private String idAttributeName = null;

	public MapEntity()
	{
	}

	public MapEntity(Entity other)
	{
		set(other);
	}

	public MapEntity(Entity other, EntityMetaData metaData)
	{
		set(other, metaData);
	}

	public MapEntity(String idAttributeName)
	{
		this.idAttributeName = idAttributeName;
	}

	public MapEntity(Map<String, Object> values)
	{
		this.values = values;
	}

	public MapEntity(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	public MapEntity(EntityMetaData metaData)
	{
		this.entityMetaData = requireNonNull(metaData);
	}

	/**
	 * Copy-factory
	 *
	 * @param entity entity to copy
	 * @return deep copy of entity
	 */
	public static Entity newInstance(Entity entity)
	{
		EntityMetaData entityMeta = entity.getEntityMetaData();
		Entity entityCopy = new MapEntity(entityMeta);
		// include atomic attributes from the 'extends' hierarchy
		entityMeta.getAtomicAttributes().forEach(attr -> {
			Object value;
			String attrName = attr.getName();
			FieldTypeEnum attrType = attr.getDataType().getEnumType();
			switch (attrType)
			{
				case BOOL:
					value = entity.getBoolean(attrName);
					break;
				case CATEGORICAL:
				case FILE:
				case XREF:
					Entity refEntity = entity.getEntity(attrName);
					value = refEntity != null ? newInstance(refEntity) : null;
					break;
				case CATEGORICAL_MREF:
				case MREF:
					Iterable<Entity> refEntities = entity.getEntities(attrName);
					// create entity copies now instead of on demand because refEntities could change
					value = stream(refEntities.spliterator(), false).map(MapEntity::newInstance).collect(toList());
					break;
				case COMPOUND:
					throw new RuntimeException("Compound attribute is not atomic");
				case DATE:
				case DATE_TIME:
					value = entity.getDate(attrName);
					break;
				case DECIMAL:
					value = entity.getDouble(attrName);
					break;
				case EMAIL:
				case ENUM:
				case HTML:
				case HYPERLINK:
				case SCRIPT:
				case STRING:
				case TEXT:
					value = entity.getString(attrName);
					break;
				case INT:
					value = entity.getInt(attrName);
					break;
				case LONG:
					value = entity.getLong(attrName);
					break;
				default:
					throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
			}
			entityCopy.set(attrName, value);
		});
		return entityCopy;
	}

	public void set(Entity other, EntityMetaData metaData)
	{
		this.entityMetaData = metaData;
		this.idAttributeName = entityMetaData.getIdAttribute().getName();
		List<String> otherAttributes = new ArrayList<>();
		for (AttributeMetaData attributeMetaData : other.getEntityMetaData().getAtomicAttributes())
		{
			otherAttributes.add(attributeMetaData.getName());
		}
		for (AttributeMetaData attribute : metaData.getAtomicAttributes())
		{
			if (Iterables.contains(otherAttributes, attribute.getName()))
				set(attribute.getName(), other.get(attribute.getName()));
		}
	}

	@Override
	public Object get(String attributeName)
	{
		return values.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	@Override
	public void set(Entity other)
	{
		for (String attributeName : other.getAttributeNames())
		{
			set(attributeName, other.get(attributeName));
		}
	}

	@Override
	public Object getIdValue()
	{
		if (getIdAttributeName() == null)
		{
			return null;
		}

		return get(getIdAttributeName());
	}

	@Override
	public Object getLabelValue()
	{
		return null;
	}

	public String getIdAttributeName()
	{
		String idAttributeName;
		if (this.idAttributeName != null)
		{
			return this.idAttributeName;
		}
		else
		{
			AttributeMetaData idAttribute = entityMetaData.getIdAttribute();
			if (idAttribute != null)
			{
				idAttributeName = idAttribute.getName();
			}
			else
			{
				idAttributeName = null;
			}
		}
		return idAttributeName;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		if (entityMetaData != null)
		{
			return Iterables.transform(entityMetaData.getAttributes(), new Function<AttributeMetaData, String>()
			{
				@Override
				public String apply(AttributeMetaData input)
				{
					return input.getName();
				}
			});
		}
		return values.keySet();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Entity)) return false;
		return EntityUtils.equals(this, (Entity) o);
	}

	@Override
	public int hashCode()
	{
		return EntityUtils.hashCode(this);
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append(getEntityMetaData().getName()).append("{").append("values=").append(values)
				.append('}').toString();
	}
}
