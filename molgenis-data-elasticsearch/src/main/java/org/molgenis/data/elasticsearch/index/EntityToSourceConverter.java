package org.molgenis.data.elasticsearch.index;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.EntityWithComputedAttributes;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Converts entities to Elasticsearch documents
 */
@Component
public class EntityToSourceConverter
{
	/**
	 * Converts entity to Elasticsearch document
	 * 
	 * @param entity
	 * @param entityMetaData
	 * @return
	 */
	public Map<String, Object> convert(Entity entity, EntityMetaData entityMetaData)
	{
		return convert(entity, entityMetaData, true);
	}

	/**
	 * Converts entity to Elasticsearch document
	 * 
	 * @param entity
	 * @param entityMetaData
	 * @return
	 */
	private Map<String, Object> convert(Entity entity, EntityMetaData entityMetaData, boolean nestRefs)
	{
		Map<String, Object> doc = new HashMap<String, Object>();

		for (AttributeMetaData attributeMetaData : entityMetaData.getAtomicAttributes())
		{
			String attrName = attributeMetaData.getName();
			Object value = convertAttribute(entity, attributeMetaData, nestRefs);
			doc.put(attrName, value);
		}

		return doc;
	}

	public Object convertAttribute(Entity entity, AttributeMetaData attributeMetaData, final boolean nestRefs)
	{
		if (attributeMetaData.getExpression() != null)
		{
			entity = new EntityWithComputedAttributes(entity);
		}

		Object value;

		String attrName = attributeMetaData.getName();
		FieldTypeEnum dataType = attributeMetaData.getDataType().getEnumType();
		switch (dataType)
		{
			case BOOL:
				value = entity.getBoolean(attrName);
				break;
			case DECIMAL:
				value = entity.getDouble(attrName);
				break;
			case INT:
				value = entity.getInt(attrName);
				break;
			case LONG:
				value = entity.getLong(attrName);
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
			case DATE:
				Date date = entity.getDate(attrName);
				value = date != null ? MolgenisDateFormat.getDateFormat().format(date) : null;
				break;
			case DATE_TIME:
				Date dateTime = entity.getDate(attrName);
				value = dateTime != null ? MolgenisDateFormat.getDateTimeFormat().format(dateTime) : null;
				break;
			case CATEGORICAL:
			case XREF:
			case FILE:
			{
				Entity xrefEntity = entity.getEntity(attrName);
				if (xrefEntity != null)
				{
					EntityMetaData xrefEntityMetaData = attributeMetaData.getRefEntity();
					if (nestRefs)
					{
						value = convert(xrefEntity, xrefEntityMetaData, false);
					}
					else
					{
						value = convertAttribute(xrefEntity, xrefEntityMetaData.getIdAttribute(), false);
					}
				}
				else
				{
					value = null;
				}
				break;
			}
			case CATEGORICAL_MREF:
			case MREF:
			{
				final Iterable<Entity> refEntities = entity.getEntities(attrName);
				if (refEntities != null && !Iterables.isEmpty(refEntities))
				{
					final EntityMetaData refEntityMetaData = attributeMetaData.getRefEntity();
					value = Lists.newArrayList(Iterables.transform(refEntities, new Function<Entity, Object>()
					{
						@Override
						public Object apply(Entity refEntity)
						{
							if(refEntity != null) {
								if (nestRefs) {
									return convert(refEntity, refEntityMetaData, false);
								} else {
									return convertAttribute(refEntity, refEntityMetaData.getIdAttribute(), false);
								}
							}
							return null;
						}
					}));
				}
				else
				{
					value = null;
				}
				break;
			}
			case COMPOUND:
				throw new RuntimeException("Compound attribute is not an atomic attribute");
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
		return value;
	}

	public Object convertAttributeValue(Object inputValue, Entity entity, AttributeMetaData attributeMetaData,
			final boolean nestRefs)
	{
		Object value;

		FieldTypeEnum dataType = attributeMetaData.getDataType().getEnumType();
		switch (dataType)
		{
			case BOOL:
			case DECIMAL:
			case INT:
			case LONG:
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				value = inputValue;
				break;
			case DATE:
				value = inputValue != null ? MolgenisDateFormat.getDateFormat().format(inputValue) : null;
				break;
			case DATE_TIME:
				value = inputValue != null ? MolgenisDateFormat.getDateTimeFormat().format(inputValue) : null;
				break;
			case CATEGORICAL:
			case XREF:
			case FILE:
			{
				Entity xrefEntity = (Entity) inputValue;
				if (xrefEntity != null)
				{
					EntityMetaData xrefEntityMetaData = attributeMetaData.getRefEntity();
					if (nestRefs)
					{
						value = convert(xrefEntity, xrefEntityMetaData, false);
					}
					else
					{
						value = convertAttribute(xrefEntity, xrefEntityMetaData.getLabelAttribute(), false);
					}
				}
				else
				{
					value = null;
				}
				break;
			}
			case CATEGORICAL_MREF:
			case MREF:
			{
				final Iterable<Entity> refEntities = (Iterable<Entity>) inputValue;
				if (refEntities != null && !Iterables.isEmpty(refEntities))
				{
					final EntityMetaData refEntityMetaData = attributeMetaData.getRefEntity();
					value = Lists.newArrayList(Iterables.transform(refEntities, new Function<Entity, Object>()
					{
						@Override
						public Object apply(Entity refEntity)
						{
							if (nestRefs)
							{
								return convert(refEntity, refEntityMetaData, false);
							}
							else
							{
								return convertAttribute(refEntity, refEntityMetaData.getLabelAttribute(), false);
							}
						}
					}));
				}
				else
				{
					value = null;
				}
				break;
			}
			case COMPOUND:
				throw new RuntimeException("Compound attribute is not an atomic attribute");
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
		return value;
	}
}
