package org.molgenis.data.elasticsearch;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.meta.model.EntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Creates entities from Elasticsearch document sources and vice versa.
 */
@Component
public class ElasticsearchEntityFactory
{
	private final EntityManager entityManager;
	private final EntityToSourceConverter entityToSourceConverter;

	@Autowired
	public ElasticsearchEntityFactory(EntityManager entityManager, EntityToSourceConverter entityToSourceConverter)
	{
		this.entityManager = requireNonNull(entityManager);
		this.entityToSourceConverter = requireNonNull(entityToSourceConverter);
	}

	/**
	 * Create Elasticsearch document source from entity
	 *
	 * @param entityMeta metadata for the entity
	 * @param entity     the entity to convert to a document source
	 * @return Elasticsearch document source
	 */
	public Map<String, Object> create(EntityMetaData entityMeta, Entity entity)
	{
		return entityToSourceConverter.convert(entity, entityMeta);
	}

	Entity getReference(EntityMetaData entityMeta, Object idObject)
	{
		return entityManager.getReference(entityMeta, idObject);
	}
}
