package org.molgenis.data.elasticsearch.generator;

import org.molgenis.data.elasticsearch.generator.model.FieldMapping;
import org.molgenis.data.elasticsearch.generator.model.Mapping;
import org.molgenis.data.elasticsearch.generator.model.MappingType;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * Generates Elasticsearch mappings from entity types.
 */
@Component
class MappingGenerator
{
	private final DocumentIdGenerator documentIdGenerator;

	MappingGenerator(DocumentIdGenerator documentIdGenerator)
	{
		this.documentIdGenerator = requireNonNull(documentIdGenerator);
	}

	Mapping createMapping(EntityType entityType)
	{
		String type = documentIdGenerator.generateId(entityType);
		int maxIndexingDepth = entityType.getIndexingDepth();
		List<FieldMapping> fieldMappings = createFieldMappings(entityType, 0, maxIndexingDepth);
		return Mapping.create(type, fieldMappings);
	}

	private List<FieldMapping> createFieldMappings(EntityType entityType, int depth, int maxDepth)
	{
		Stream<Attribute> attributeStream = stream(entityType.getAtomicAttributes().spliterator(), false);
		return attributeStream.map(attribute -> createFieldMapping(attribute, depth, maxDepth)).collect(toList());
	}

	private FieldMapping createFieldMapping(Attribute attribute, int depth, int maxDepth)
	{
		String fieldName = documentIdGenerator.generateId(attribute);
		MappingType mappingType = toMappingType(attribute, depth, maxDepth);
		List<FieldMapping> nestedFieldMappings =
				mappingType == MappingType.NESTED ? createFieldMappings(attribute.getRefEntity(), depth + 1,
						maxDepth) : null;
		return FieldMapping.create(fieldName, mappingType, nestedFieldMappings);
	}

	private MappingType toMappingType(Attribute attribute, int depth, int maxDepth)
	{
		AttributeType attributeType = attribute.getDataType();
		switch (attributeType)
		{

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
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				return MappingType.TEXT;
			case INT:
				return MappingType.INTEGER;
			case LONG:
				return MappingType.LONG;
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type '%s'", attributeType));
			default:
				throw new UnexpectedEnumException(attributeType);
		}
	}

	private MappingType toMappingTypeReferenceAttribute(Attribute referenceAttribute, int depth, int maxDepth)
	{
		if (depth < maxDepth)
		{
			return MappingType.NESTED;
		}
		else
		{
			Attribute refLabelAttribute = referenceAttribute.getRefEntity().getLabelAttribute();
			return toMappingType(refLabelAttribute, depth + 1, maxDepth);
		}
	}
}
