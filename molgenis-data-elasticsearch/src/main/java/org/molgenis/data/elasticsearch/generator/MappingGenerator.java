package org.molgenis.data.elasticsearch.generator;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.eclipse.rdf4j.model.vocabulary.XMLSchema.TOKEN;
import static org.molgenis.data.QueryUtils.isTaggedType;
import static org.molgenis.data.semantic.Vocabulary.CASE_SENSITIVE;

import java.util.List;
import java.util.stream.Stream;
import org.molgenis.data.elasticsearch.generator.model.FieldMapping;
import org.molgenis.data.elasticsearch.generator.model.Mapping;
import org.molgenis.data.elasticsearch.generator.model.MappingType;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.IllegalAttributeTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

/** Generates Elasticsearch mappings from entity types. */
@Component
class MappingGenerator {
  private final DocumentIdGenerator documentIdGenerator;

  MappingGenerator(DocumentIdGenerator documentIdGenerator) {
    this.documentIdGenerator = requireNonNull(documentIdGenerator);
  }

  Mapping createMapping(EntityType entityType) {
    String type = documentIdGenerator.generateId(entityType);
    int maxIndexingDepth = entityType.getIndexingDepth();
    List<FieldMapping> fieldMappings = createFieldMappings(entityType, 0, maxIndexingDepth);
    return Mapping.create(type, fieldMappings);
  }

  private List<FieldMapping> createFieldMappings(EntityType entityType, int depth, int maxDepth) {
    Stream<Attribute> attributeStream = stream(entityType.getAtomicAttributes());
    return attributeStream
        .map(attribute -> createFieldMapping(attribute, depth, maxDepth))
        .collect(toList());
  }

  private FieldMapping createFieldMapping(Attribute attribute, int depth, int maxDepth) {
    String fieldName = documentIdGenerator.generateId(attribute);
    MappingType mappingType = toMappingType(attribute, depth, maxDepth);
    var result = FieldMapping.builder().setName(fieldName).setType(mappingType);
    if (mappingType == MappingType.NESTED) {
      result.setNestedFieldMappings(
          createFieldMappings(attribute.getRefEntity(), depth + 1, maxDepth));
    }
    if (mappingType == MappingType.KEYWORD && isTaggedType(attribute, CASE_SENSITIVE)) {
      result.setCaseSensitive(true);
    }
    if ("ontologyTermSynonym".equals(attribute.getName())) {
      result.setNeedsNgram(true);
    }
    return result.build();
  }

  private MappingType toMappingType(Attribute attribute, int depth, int maxDepth) {
    MappingType result = getMappingType(attribute);
    if (result == MappingType.NESTED) {
      return toMappingTypeReferenceAttribute(attribute, depth, maxDepth);
    }
    return result;
  }

  public static MappingType getMappingType(Attribute attribute) {
    AttributeType attributeType = attribute.getDataType();
    switch (attribute.getDataType()) {
      case BOOL:
        return MappingType.BOOLEAN;
      case CATEGORICAL:
      case CATEGORICAL_MREF:
      case FILE:
      case MREF:
      case ONE_TO_MANY:
      case XREF:
        return MappingType.NESTED;
      case DATE:
        return MappingType.DATE;
      case DATE_TIME:
        return MappingType.DATE_TIME;
      case DECIMAL:
        return MappingType.DOUBLE;
      case EMAIL:
      case ENUM:
      case HYPERLINK:
      case STRING:
        return isTaggedType(attribute, TOKEN) ? MappingType.KEYWORD : MappingType.TEXT;
      case HTML:
      case SCRIPT:
      case TEXT:
        return MappingType.TEXT;
      case INT:
        return MappingType.INTEGER;
      case LONG:
        return MappingType.LONG;
      case COMPOUND:
        throw new IllegalAttributeTypeException(attributeType);
      default:
        throw new UnexpectedEnumException(attributeType);
    }
  }

  private MappingType toMappingTypeReferenceAttribute(
      Attribute referenceAttribute, int depth, int maxDepth) {
    if (depth < maxDepth) {
      return MappingType.NESTED;
    } else {
      Attribute refLabelAttribute = referenceAttribute.getRefEntity().getLabelAttribute();
      return toMappingType(refLabelAttribute, depth + 1, maxDepth);
    }
  }
}
