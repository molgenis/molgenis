package org.molgenis.data.elasticsearch.generator;

import static java.util.Objects.requireNonNull;
import static org.elasticsearch.common.xcontent.XContentType.JSON;

import com.google.common.collect.Iterables;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentGenerator;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.generator.model.Document;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.IllegalAttributeTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

/** Generates Elasticsearch document sources from entities. */
@Component
class DocumentContentBuilder {
  private final DocumentIdGenerator documentIdGenerator;

  DocumentContentBuilder(DocumentIdGenerator documentIdGenerator) {
    this.documentIdGenerator = requireNonNull(documentIdGenerator);
  }

  Document createDocument(Object entityId) {
    String documentId = toElasticsearchId(entityId);
    return Document.builder().setId(documentId).build();
  }

  /**
   * Create Elasticsearch document source content from entity
   *
   * @param entity the entity to convert to document source content
   * @return Elasticsearch document source content
   */
  Document createDocument(Entity entity) {
    int maxIndexingDepth = entity.getEntityType().getIndexingDepth();
    XContentBuilder contentBuilder;
    try {
      contentBuilder = XContentFactory.contentBuilder(JSON);
      XContentGenerator generator = contentBuilder.generator();
      generator.writeStartObject();
      createRec(entity, generator, 0, maxIndexingDepth);
      generator.writeEndObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String documentId = toElasticsearchId(entity.getIdValue());
    return Document.create(documentId, contentBuilder);
  }

  private void createRec(Entity entity, XContentGenerator generator, int depth, int maxDepth)
      throws IOException {
    for (Attribute attr : entity.getEntityType().getAtomicAttributes()) {
      generator.writeFieldName(documentIdGenerator.generateId(attr));
      createRec(entity, attr, generator, depth, maxDepth);
    }
  }

  private void createRec(
      Entity entity, Attribute attr, XContentGenerator generator, int depth, int maxDepth)
      throws IOException {
    String attrName = attr.getName();
    AttributeType attrType = attr.getDataType();

    switch (attrType) {
      case BOOL:
        Boolean boolValue = entity.getBoolean(attrName);
        writeBoolean(generator, boolValue);
        break;
      case DECIMAL:
        Double doubleValue = entity.getDouble(attrName);
        writeDouble(generator, doubleValue);
        break;
      case INT:
        Integer intValue = entity.getInt(attrName);
        writeInteger(generator, intValue);
        break;
      case LONG:
        Long longValue = entity.getLong(attrName);
        writeLong(generator, longValue);
        break;
      case EMAIL:
      case ENUM:
      case HTML:
      case HYPERLINK:
      case SCRIPT:
      case STRING:
      case TEXT:
        String strValue = entity.getString(attrName);
        writeString(generator, strValue);
        break;
      case DATE:
        LocalDate date = entity.getLocalDate(attrName);
        writeDate(generator, date);
        break;
      case DATE_TIME:
        Instant dateTime = entity.getInstant(attrName);
        writeDateTime(generator, dateTime);
        break;
      case CATEGORICAL:
      case XREF:
      case FILE:
        Entity xrefEntity = entity.getEntity(attrName);
        writeReference(generator, depth, maxDepth, xrefEntity);
        break;
      case CATEGORICAL_MREF:
      case MREF:
      case ONE_TO_MANY:
        Iterable<Entity> mrefEntities = entity.getEntities(attrName);
        writeMultiReference(generator, depth, maxDepth, mrefEntities);
        break;
      case COMPOUND:
        throw new IllegalAttributeTypeException(attrType);
      default:
        throw new UnexpectedEnumException(attrType);
    }
  }

  private void writeMultiReference(
      XContentGenerator generator, int depth, int maxDepth, Iterable<Entity> mrefEntities)
      throws IOException {
    if (!Iterables.isEmpty(mrefEntities)) {
      generator.writeStartArray();
      for (Entity mrefEntity : mrefEntities) {
        createRecReferenceAttribute(generator, depth, maxDepth, mrefEntity);
      }
      generator.writeEndArray();
    } else {
      generator.writeNull();
    }
  }

  private void writeReference(
      XContentGenerator generator, int depth, int maxDepth, Entity xrefEntity) throws IOException {
    if (xrefEntity != null) {
      createRecReferenceAttribute(generator, depth, maxDepth, xrefEntity);
    } else {
      generator.writeNull();
    }
  }

  private void writeString(XContentGenerator generator, String strValue) throws IOException {
    if (strValue != null) {
      generator.writeString(strValue);
    } else {
      generator.writeNull();
    }
  }

  private void writeDate(XContentGenerator generator, LocalDate dateValue) throws IOException {
    if (dateValue != null) {
      generator.writeString(dateValue.toString());
    } else {
      generator.writeNull();
    }
  }

  private void writeDateTime(XContentGenerator generator, Instant dateTimeValue)
      throws IOException {
    if (dateTimeValue != null) {
      generator.writeString(dateTimeValue.toString());
    } else {
      generator.writeNull();
    }
  }

  private void writeLong(XContentGenerator generator, Long longValue) throws IOException {
    if (longValue != null) {
      generator.writeNumber(longValue);
    } else {
      generator.writeNull();
    }
  }

  private void writeInteger(XContentGenerator generator, Integer intValue) throws IOException {
    if (intValue != null) {
      generator.writeNumber(intValue);
    } else {
      generator.writeNull();
    }
  }

  private void writeDouble(XContentGenerator generator, Double doubleValue) throws IOException {
    if (doubleValue != null) {
      generator.writeNumber(doubleValue);
    } else {
      generator.writeNull();
    }
  }

  private void writeBoolean(XContentGenerator generator, Boolean boolValue) throws IOException {
    if (boolValue != null) {
      generator.writeBoolean(boolValue);
    } else {
      generator.writeNull();
    }
  }

  private void createRecReferenceAttribute(
      XContentGenerator generator, int depth, int maxDepth, Entity xrefEntity) throws IOException {
    if (depth < maxDepth) {
      generator.writeStartObject();
      createRec(xrefEntity, generator, depth + 1, maxDepth);
      generator.writeEndObject();
    } else {
      Attribute xrefIdAttr = xrefEntity.getEntityType().getLabelAttribute();
      createRec(xrefEntity, xrefIdAttr, generator, depth + 1, maxDepth);
    }
  }

  private static String toElasticsearchId(Object entityId) {
    return entityId.toString();
  }
}
