package org.molgenis.data.elasticsearch.generator;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.molgenis.data.elasticsearch.generator.model.FieldMapping;
import org.molgenis.data.elasticsearch.generator.model.Mapping;
import org.molgenis.data.elasticsearch.generator.model.MappingType;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.IllegalAttributeTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.semantic.Relation;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

/** Generates Elasticsearch mappings from entity types. */
@Component
class MappingGenerator {

  public static final String RDFS_TOKEN = "https://www.w3.org/TR/xmlschema11-2/#token";
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
    List<FieldMapping> nestedFieldMappings =
        mappingType == MappingType.NESTED
            ? createFieldMappings(attribute.getRefEntity(), depth + 1, maxDepth)
            : null;
    var builder =
        FieldMapping.builder()
            .setName(fieldName)
            .setType(mappingType)
            .setNestedFieldMappings(nestedFieldMappings);
    if (mappingType == MappingType.TEXT) {
      Optional<String> analyzer =
          stream(attribute.getTags())
              .map(this::getAnalyzer)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .findFirst();
      analyzer.ifPresent(builder::setAnalyzer);
    }
    return builder.build();
  }

  private Optional<String> getAnalyzer(Tag tag) {
    if (Relation.language.getIRI().equals(tag.getRelationIri())) {
      // TODO: surely this can fail..
      Locale locale = Locale.forLanguageTag(tag.getObjectIri());
      switch (locale.getLanguage()) {
        case "en":
          return Optional.of("english");
        case "nl":
          return Optional.of("dutch");
        case "de":
          return Optional.of("german");
        case "fr":
          return Optional.of("french");
          // TODO: there is more
        default:
          return Optional.empty();
      }
    }
    return Optional.empty();
  }

  private MappingType toMappingType(Attribute attribute, int depth, int maxDepth) {
    AttributeType attributeType = attribute.getDataType();
    switch (attributeType) {
      case BOOL:
        return MappingType.BOOLEAN;
      case CATEGORICAL:
      case CATEGORICAL_MREF:
      case FILE:
      case MREF:
      case ONE_TO_MANY:
      case XREF:
        return toMappingTypeReferenceAttribute(attribute, depth, maxDepth);
      case DATE:
        return MappingType.DATE;
      case DATE_TIME:
        return MappingType.DATE_TIME;
      case DECIMAL:
        return MappingType.DOUBLE;
      case EMAIL:
      case ENUM:
      case HYPERLINK:
        return MappingType.KEYWORD;
      case HTML:
      case SCRIPT:
      case STRING:
      case TEXT:
        return isAToken(attribute) ? MappingType.KEYWORD : MappingType.TEXT;
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

  public static boolean isAToken(Attribute attribute) {
    return stream(attribute.getTags())
        .filter(it -> Relation.isA.getIRI().equals(it.getRelationIri()))
        .map(Tag::getObjectIri)
        .anyMatch(RDFS_TOKEN::equals);
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
