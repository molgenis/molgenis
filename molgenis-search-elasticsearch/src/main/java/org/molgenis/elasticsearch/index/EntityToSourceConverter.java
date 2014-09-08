package org.molgenis.elasticsearch.index;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

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
	private Map<String, Object> convert(Entity entity, EntityMetaData entityMetaData, boolean createNestedTypes)
	{
		Map<String, Object> doc = new HashMap<String, Object>();

		for (AttributeMetaData attributeMetaData : entityMetaData.getAtomicAttributes())
		{
			String attrName = attributeMetaData.getName();
			Object value = convertAttribute(entity, attributeMetaData, createNestedTypes);
			doc.put(attrName, value);
		}

		return doc;
	}

	private Object convertAttribute(Entity entity, AttributeMetaData attributeMetaData, boolean createNestedTypes)
	{
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
			{
				// TODO store categorical/xref values as nested types
				// (requires query generator and mapping builder changes)
				Entity xrefEntity = entity.getEntity(attrName);
				if (xrefEntity != null)
				{
					// flatten referenced entity
					value = convertAttribute(xrefEntity, attributeMetaData.getRefEntity().getLabelAttribute(), false);
				}
				else
				{
					value = null;
				}
				break;
			}
			case MREF:
			{
				Iterable<Entity> refEntities = entity.getEntities(attrName);
				if (refEntities != null && !Iterables.isEmpty(refEntities))
				{
					final EntityMetaData refEntityMetaData = attributeMetaData.getRefEntity();

					if (createNestedTypes)
					{
						// TODO ask Chao why a list of nested docs is not working

						// store nested referenced entity
						Map<String, List<Object>> refValueMap = new HashMap<String, List<Object>>();

						for (Entity refEntity : refEntities)
						{
							Map<String, Object> refDoc = convert(refEntity, refEntityMetaData, false);

							// merge doc
							for (Map.Entry<String, Object> entry : refDoc.entrySet())
							{
								Object refAttributeValue = entry.getValue();
								if (refAttributeValue != null)
								{
									String refAttributeName = entry.getKey();
									List<Object> refValue = refValueMap.get(refAttributeName);
									if (refValue == null)
									{
										refValue = new ArrayList<Object>();
										refValueMap.put(refAttributeName, refValue);
									}
									refValue.add(refAttributeValue);
								}
							}
						}

						value = Arrays.asList(refValueMap);
					}
					else
					{
						// store flattened referenced entity
						value = Lists.newArrayList(Iterables.filter(
								Iterables.transform(refEntities, new Function<Entity, Object>()
								{
									@Override
									public Object apply(Entity refEntity)
									{
										return convertAttribute(refEntity, refEntityMetaData.getLabelAttribute(), false);
									}
								}), Predicates.notNull()));
					}
				}
				else
				{
					value = null;
				}
				break;
			}
			case COMPOUND:
				throw new RuntimeException("Compound attribute is not an atomic attribute");
			case FILE:
			case IMAGE:
				throw new MolgenisDataException("Unsupported data type for indexing [" + dataType + "]");
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
		return value;
	}
}
