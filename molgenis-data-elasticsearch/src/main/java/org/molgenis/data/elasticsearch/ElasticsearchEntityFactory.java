package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.elasticsearch.index.SourceToEntityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates entities from Elasticsearch document sources and vice versa.
 */
@Component
public class ElasticsearchEntityFactory
{
	private final EntityManager entityManager;
	private final SourceToEntityConverter sourceToEntityConverter;
	private final EntityToSourceConverter entityToSourceConverter;

	@Autowired
	public ElasticsearchEntityFactory(EntityManager entityManager, SourceToEntityConverter sourceToEntityConverter,
			EntityToSourceConverter entityToSourceConverter)
	{
		this.entityManager = requireNonNull(entityManager);
		this.sourceToEntityConverter = requireNonNull(sourceToEntityConverter);
		this.entityToSourceConverter = requireNonNull(entityToSourceConverter);
	}

	/**
	 * Creates an entity from the given Elasticsearch document source. In case
	 * 
	 * @param entityMeta
	 * @param source
	 * @param fetch
	 *            fetch (can be null)
	 * @return
	 */
	public Entity create(EntityMetaData entityMeta, Map<String, Object> source, Fetch fetch)
	{
		Entity entity = sourceToEntityConverter.convert(source, entityMeta);
		if (fetch != null)
		{
			return entityManager.createEntityForPartialEntity(entity, fetch);
		}
		else
		{
			return entity;
		}
	}

	public Map<String, Object> create(EntityMetaData entityMeta, Entity entity)
	{
		return entityToSourceConverter.convert(entity, entityMeta);
	}

	EntityManager getEntityManager()
	{
		return entityManager;
	}
}
