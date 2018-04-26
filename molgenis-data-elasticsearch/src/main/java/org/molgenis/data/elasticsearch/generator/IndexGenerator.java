package org.molgenis.data.elasticsearch.generator;

import org.molgenis.data.elasticsearch.generator.model.Index;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Generates Elasticsearch index metadata from entity types.
 */
@Component
class IndexGenerator
{
	private final DocumentIdGenerator documentIdGenerator;

	IndexGenerator(DocumentIdGenerator documentIdGenerator)
	{
		this.documentIdGenerator = requireNonNull(documentIdGenerator);
	}

	Index createIndex(EntityType entityType)
	{
		String indexName = documentIdGenerator.generateId(entityType);
		return Index.create(indexName);
	}
}
