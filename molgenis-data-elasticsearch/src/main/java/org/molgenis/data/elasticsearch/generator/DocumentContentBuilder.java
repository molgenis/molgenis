package org.molgenis.data.elasticsearch.generator;

import com.google.common.collect.Iterables;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentGenerator;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.generator.model.Document;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.elasticsearch.common.xcontent.XContentType.JSON;

/**
 * Generates Elasticsearch document sources from entities.
 */
@Component
class DocumentContentBuilder
{
	private final DocumentIdGenerator documentIdGenerator;

	DocumentContentBuilder(DocumentIdGenerator documentIdGenerator)
	{
		this.documentIdGenerator = requireNonNull(documentIdGenerator);
	}

	Document createDocument(Object entityId)
	{
		String documentId = toElasticsearchId(entityId);
		return Document.builder().setId(documentId).build();
	}

	/**
	 * Create Elasticsearch document source content from entity
	 *
	 * @param entity the entity to convert to document source content
	 * @return Elasticsearch document source content
	 */
	Document createDocument(Entity entity)
	{
		int maxIndexingDepth = entity.getEntityType().getIndexingDepth();
		XContentBuilder contentBuilder;
		try
		{
			contentBuilder = XContentFactory.contentBuilder(JSON);
			XContentGenerator generator = contentBuilder.generator();
			generator.writeStartObject();
			createRec(entity, generator, 0, maxIndexingDepth);
			generator.writeEndObject();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		String documentId = toElasticsearchId(entity.getIdValue());
		return Document.create(documentId, contentBuilder);
	}

	private void createRec(Entity entity, XContentGenerator generator, int depth, int maxDepth) throws IOException
	{
		for (Attribute attr : entity.getEntityType().getAtomicAttributes())
		{
			generator.writeFieldName(documentIdGenerator.generateId(attr));
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
				LocalDate date = entity.getLocalDate(attrName);
				if (date != null)
				{
					generator.writeString(date.toString());
				}
				else
				{
					generator.writeNull();
				}
				break;
			case DATE_TIME:
				Instant dateTime = entity.getInstant(attrName);
				if (dateTime != null)
				{
					generator.writeString(dateTime.toString());
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
					createRecReferenceAttribute(generator, depth, maxDepth, xrefEntity);
				}
				else
				{
					generator.writeNull();
				}
				break;
			}
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
			{
				Iterable<Entity> mrefEntities = entity.getEntities(attrName);
				if (!Iterables.isEmpty(mrefEntities))
				{
					generator.writeStartArray();
					for (Entity mrefEntity : mrefEntities)
					{
						createRecReferenceAttribute(generator, depth, maxDepth, mrefEntity);
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
				throw new UnexpectedEnumException(attrType);
		}
	}

	private void createRecReferenceAttribute(XContentGenerator generator, int depth, int maxDepth, Entity xrefEntity)
			throws IOException
	{
		if (depth < maxDepth)
		{
			generator.writeStartObject();
			createRec(xrefEntity, generator, depth + 1, maxDepth);
			generator.writeEndObject();
		}
		else
		{
			Attribute xrefIdAttr = xrefEntity.getEntityType().getLabelAttribute();
			createRec(xrefEntity, xrefIdAttr, generator, depth + 1, maxDepth);
		}
	}

	private static String toElasticsearchId(Object entityId)
	{
		return entityId.toString();
	}
}
