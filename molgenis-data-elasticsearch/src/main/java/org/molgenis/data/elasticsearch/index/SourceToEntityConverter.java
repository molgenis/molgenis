package org.molgenis.data.elasticsearch.index;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SourceToEntityConverter
{
	private final DataService dataService;

	@Autowired
	public SourceToEntityConverter(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public Entity convert(Map<String, Object> source, EntityMetaData entityMeta)
	{
		DefaultEntity entity = new DefaultEntity(entityMeta, dataService);
		source.entrySet().forEach(entry -> {
			String attrName = entry.getKey();
			AttributeMetaData attr = entityMeta.getAttribute(attrName);
			if (attr == null)
			{
				throw new UnknownAttributeException(
						"Unknown attribute [" + attrName + "] of entity [" + entityMeta.getName());
			}

			Object entityValue = convert(entityMeta, attr, entry.getValue());
			entity.set(attrName, entityValue);
		});
		return entity;
	}

	/**
	 * Convert a single Elasticsearch document source value to a entity attribute value
	 * 
	 * @param entityMeta
	 * @param attr
	 * @param sourceValue
	 * @return entity attribute value
	 */
	private Object convert(EntityMetaData entityMeta, AttributeMetaData attr, Object sourceValue)
	{
		if (attr.getExpression() != null)
		{
			throw new MolgenisDataException(
					format("Computed attribute [%s] not stored in source document", attr.getName()));
		}

		Object entityValue;
		if (sourceValue != null)
		{
			FieldTypeEnum attrType = attr.getDataType().getEnumType();
			switch (attrType)
			{
				case BOOL:
				case EMAIL:
				case ENUM:
				case HTML:
				case HYPERLINK:
				case SCRIPT:
				case STRING:
				case TEXT:
					// no conversion required
					entityValue = sourceValue;
					break;
				case DECIMAL:
					// https://groups.google.com/forum/#!msg/elasticsearch/jxIY22TmA8U/5T25x9uhBD8J
					entityValue = ((Number) sourceValue).doubleValue();
					break;
				case INT:
					// https://groups.google.com/forum/#!msg/elasticsearch/jxIY22TmA8U/5T25x9uhBD8J
					entityValue = ((Number) sourceValue).intValue();
					break;
				case LONG:
					// https://groups.google.com/forum/#!msg/elasticsearch/jxIY22TmA8U/5T25x9uhBD8J
					entityValue = ((Number) sourceValue).longValue();
					break;
				case CATEGORICAL:
				case FILE:
				case XREF:
					@SuppressWarnings("unchecked")
					Map<String, Object> xrefSource = (Map<String, Object>) sourceValue;
					entityValue = convert(xrefSource, attr.getRefEntity());
					break;
				case CATEGORICAL_MREF:
				case MREF:
					@SuppressWarnings("unchecked")
					Iterable<Map<String, Object>> mrefSources = (Iterable<Map<String, Object>>) sourceValue;
					entityValue = new Iterable<Entity>()
					{
						@Override
						public Iterator<Entity> iterator()
						{
							return StreamSupport.stream(mrefSources.spliterator(), false)
									.map(mrefSource -> convert(mrefSource, attr.getRefEntity())).iterator();
						}
					};
					break;
				case COMPOUND:
					throw new RuntimeException("Compound attribute is not an atomic attribute");
				case DATE:
					try
					{
						entityValue = MolgenisDateFormat.getDateFormat().parse((String) sourceValue);
					}
					catch (Exception e)
					{
						throw new MolgenisDataException(e);
					}
					break;
				case DATE_TIME:
					try
					{
						entityValue = MolgenisDateFormat.getDateTimeFormat().parse((String) sourceValue);
					}
					catch (Exception e)
					{
						throw new MolgenisDataException(e);
					}
					break;
				case IMAGE:
					throw new MolgenisDataException(format("Unsupported data type for indexing [%s]", attrType));
				default:
					throw new RuntimeException(format("Unknown data type [%s]", attrType));
			}
		}
		else
		{
			entityValue = null;
		}
		return entityValue;
	}
}
