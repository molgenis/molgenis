package org.molgenis.data.elasticsearch.generator;

import org.molgenis.data.elasticsearch.generator.model.FieldMapping;
import org.molgenis.data.elasticsearch.generator.model.Mapping;
import org.molgenis.data.elasticsearch.generator.model.MappingType;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Component
class MappingGenerator
{
	private static final int MAX_INDEXING_DEPTH = 2;

	private final DocumentIdGenerator documentIdGenerator;

	MappingGenerator(DocumentIdGenerator documentIdGenerator)
	{
		this.documentIdGenerator = requireNonNull(documentIdGenerator);
	}

	Mapping createMapping(EntityType entityType)
	{
		String type = documentIdGenerator.generateId(entityType);
		List<FieldMapping> fieldMappings = createFieldMappings(entityType, 1, MAX_INDEXING_DEPTH);
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
		boolean analyzeNGrams = isAnalyzeNGrams(attribute);
		List<FieldMapping> nestedFieldMappings =
				mappingType == MappingType.NESTED ? createFieldMappings(attribute.getRefEntity(), depth + 1,
						maxDepth) : null;
		return FieldMapping.create(fieldName, mappingType, analyzeNGrams, nestedFieldMappings);
	}

	private boolean isAnalyzeNGrams(Attribute attribute)
	{
		AttributeType attributeType = attribute.getDataType();
		switch (attributeType)
		{
			case BOOL:
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case FILE:
			case HTML:
			case HYPERLINK:
			case INT:
			case LONG:
			case MREF:
			case ONE_TO_MANY:
			case SCRIPT:
			case XREF:
				return false;
			case EMAIL:
			case ENUM:
			case STRING:
			case TEXT:
				return true;
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type '%s'", attributeType));
			default:
				throw new RuntimeException(format("Unknown attribute type '%s'", attributeType));
		}
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
				if (depth < maxDepth)
				{
					return MappingType.NESTED;
				}
				else
				{
					Attribute refLabelAttribute = attribute.getRefEntity().getLabelAttribute();
					return toMappingType(refLabelAttribute, depth + 1, maxDepth);
				}
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
				throw new RuntimeException(format("Unknown attribute type '%s'", attributeType));
		}
	}
}
