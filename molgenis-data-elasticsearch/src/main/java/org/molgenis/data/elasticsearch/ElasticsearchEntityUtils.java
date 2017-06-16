package org.molgenis.data.elasticsearch;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.support.EntityTypeUtils.isReferenceType;

public class ElasticsearchEntityUtils
{
	private ElasticsearchEntityUtils()
	{
	}

	public static String toElasticsearchId(Object entityId)
	{
		return entityId.toString();
	}

	static Stream<String> toElasticsearchIds(Stream<Object> entityIds)
	{
		return entityIds.map(ElasticsearchEntityUtils::toElasticsearchId);
	}

	static Object toEntityId(String elasticsearchId)
	{
		// TODO we do not know which type to return (e.g. String, Integer)
		return elasticsearchId;
	}

	static Stream<Object> toEntityIds(Stream<String> documentIdStream)
	{
		return documentIdStream.map(ElasticsearchEntityUtils::toEntityId);
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

	public static boolean isNestedType(Attribute attr)
	{
		return isReferenceType(attr);
	}
}
