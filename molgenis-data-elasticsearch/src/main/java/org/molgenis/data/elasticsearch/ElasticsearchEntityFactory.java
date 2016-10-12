package org.molgenis.data.elasticsearch;

import com.google.common.collect.Iterables;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentGenerator;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Date;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.elasticsearch.common.xcontent.XContentType.JSON;

/**
 * Creates entities from Elasticsearch document sources and vice versa.
 */
@Component
public class ElasticsearchEntityFactory
{
	private static final int MAX_INDEXING_DEPTH = 1;

	private final EntityManager entityManager;

	@Autowired
	public ElasticsearchEntityFactory(EntityManager entityManager)
	{
		this.entityManager = requireNonNull(entityManager);
	}

	/**
	 * Create Elasticsearch document source content from entity
	 *
	 * @param entity the entity to convert to document source content
	 * @return Elasticsearch document source content
	 */
	public XContentBuilder create(Entity entity)
	{
		try
		{
			XContentBuilder contentBuilder = XContentFactory.contentBuilder(JSON);
			XContentGenerator generator = contentBuilder.generator();
			generator.writeStartObject();
			createRec(entity, generator, 0, MAX_INDEXING_DEPTH);
			generator.writeEndObject();

			return contentBuilder;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void createRec(Entity entity, XContentGenerator generator, int depth, int maxDepth) throws IOException
	{
		for (Attribute attr : entity.getEntityMetaData().getAtomicAttributes())
		{
			generator.writeFieldName(attr.getName());
			createRec(entity, attr, generator, depth, maxDepth);
		}
	}

	private void createRec(Entity entity, Attribute attr, XContentGenerator generator, int depth, int maxDepth)
			throws IOException
	{
		String attrName = attr.getName();
		AttributeType attrType = attr.getDataType();

		switch (attrType)
		{
			case BOOL:
				Boolean boolValue = entity.getBoolean(attrName);
				if (boolValue != null)
				{
					generator.writeBoolean(boolValue);
				}
				else
				{
					generator.writeNull();
				}
				break;
			case DECIMAL:
				Double doubleValue = entity.getDouble(attrName);
				if (doubleValue != null)
				{
					generator.writeNumber(doubleValue);
				}
				else
				{
					generator.writeNull();
				}
				break;
			case INT:
				Integer intValue = entity.getInt(attrName);
				if (intValue != null)
				{
					generator.writeNumber(intValue);
				}
				else
				{
					generator.writeNull();
				}
				break;
			case LONG:
				Long longValue = entity.getLong(attrName);
				if (longValue != null)
				{
					generator.writeNumber(longValue);
				}
				else
				{
					generator.writeNull();
				}
				break;
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				String strValue = entity.getString(attrName);
				if (strValue != null)
				{
					generator.writeString(strValue);
				}
				else
				{
					generator.writeNull();
				}
				break;
			case DATE:
				Date date = entity.getDate(attrName);
				if (date != null)
				{
					String dateValue = MolgenisDateFormat.getDateFormat().format(date);
					generator.writeString(dateValue);
				}
				else
				{
					generator.writeNull();
				}
				break;
			case DATE_TIME:
				Date dateTime = entity.getDate(attrName);
				if (dateTime != null)
				{
					String dateTimeValue = MolgenisDateFormat.getDateTimeFormat().format(dateTime);
					generator.writeString(dateTimeValue);
				}
				else
				{
					generator.writeNull();
				}
				break;
			case CATEGORICAL:
			case XREF:
			case FILE:
			{
				Entity xrefEntity = entity.getEntity(attrName);
				if (xrefEntity != null)
				{
					if (depth < maxDepth)
					{
						generator.writeStartObject();
						createRec(xrefEntity, generator, depth + 1, maxDepth);
						generator.writeEndObject();
					}
					else
					{
						Attribute xrefIdAttr = xrefEntity.getEntityMetaData().getIdAttribute();
						createRec(xrefEntity, xrefIdAttr, generator, depth + 1, maxDepth);
					}
				}
				else
				{
					generator.writeNull();
				}
				break;
			}
			case CATEGORICAL_MREF:
			case MREF:
			{
				Iterable<Entity> mrefEntities = entity.getEntities(attrName);
				if (!Iterables.isEmpty(mrefEntities))
				{
					generator.writeStartArray();
					for (Entity mrefEntity : mrefEntities)
					{
						if (depth < maxDepth)
						{
							generator.writeStartObject();
							createRec(mrefEntity, generator, depth + 1, maxDepth);
							generator.writeEndObject();
						}
						else
						{
							Attribute mrefIdAttr = mrefEntity.getEntityMetaData().getIdAttribute();
							createRec(mrefEntity, mrefIdAttr, generator, depth + 1, maxDepth);
						}
					}
					generator.writeEndArray();
				}
				else
				{
					generator.writeNull();
				}
				break;
			}
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	Entity getReference(EntityMetaData entityMeta, Object idObject)
	{
		return entityManager.getReference(entityMeta, idObject);
	}
}
