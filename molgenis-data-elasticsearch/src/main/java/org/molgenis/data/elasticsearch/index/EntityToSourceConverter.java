package org.molgenis.data.elasticsearch.index;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.EntityWithComputedAttributes;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

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
		AttributeType dataType = attributeMetaData.getDataType();
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
					value = Lists.newArrayList(Iterables.transform(refEntities, refEntity -> {
						if (refEntity != null)
						{
							if (nestRefs)
							{
								return convert(refEntity, refEntityMetaData, false);
							}
							else
							{
								return convertAttribute(refEntity, refEntityMetaData.getIdAttribute(), false);
							}
						}
						return null;
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
