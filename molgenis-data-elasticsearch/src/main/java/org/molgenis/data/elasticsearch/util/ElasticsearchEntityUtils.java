package org.molgenis.data.elasticsearch.util;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;

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

	public static Object toEntityId(String elasticsearchId)
	{
		// TODO we do not know which type to return (e.g. String, Integer)
		return elasticsearchId;
	}

	public static Iterable<Object> toEntityIds(Iterable<String> elasticsearchIds)
	{
		return Iterables.transform(elasticsearchIds, new Function<String, Object>()
		{
			@Override
			public Object apply(String elasticsearchId)
			{
				return toEntityId(elasticsearchId);
			}
		});
	}

	public static String toElasticsearchId(Entity entity, EntityType entityType)
	{
		String idAttributeName = entityType.getIdAttribute().getName();
		Object entityId = entity.get(idAttributeName);
		return toElasticsearchId(entityId);
	}
}
