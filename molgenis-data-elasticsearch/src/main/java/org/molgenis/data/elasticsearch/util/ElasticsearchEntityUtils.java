package org.molgenis.data.elasticsearch.util;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class ElasticsearchEntityUtils
{
	private ElasticsearchEntityUtils()
	{
	}

	public static String toElasticsearchId(Object entityId)
	{
		return entityId.toString();
	}

	public static Stream<String> toElasticsearchIds(Stream<Object> entityIds)
	{
		return entityIds.map(ElasticsearchEntityUtils::toElasticsearchId);
	}

	static Object toEntityId(String elasticsearchId)
	{
		// TODO we do not know which type to return (e.g. String, Integer)
		return elasticsearchId;
	}

	static Iterable<Object> toEntityIds(Iterable<String> elasticsearchIds)
	{
		return stream(elasticsearchIds.spliterator(), false).map(ElasticsearchEntityUtils::toEntityId)
				.collect(toList());
	}

	public static String toElasticsearchId(Entity entity, EntityType entityType)
	{
		String idAttributeName = entityType.getIdAttribute().getName();
		Object entityId = entity.get(idAttributeName);
		return toElasticsearchId(entityId);
	}
}
