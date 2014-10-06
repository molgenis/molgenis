package org.molgenis.data.elasticsearch;

import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.UnknownAttributeException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Elasticsearch entity containing ids for referenced entities
 */
public class ElasticsearchDocumentNestedEntity extends ElasticsearchDocumentEntity
{
	private static final long serialVersionUID = 1L;

	public ElasticsearchDocumentNestedEntity(Map<String, Object> source, EntityMetaData entityMetaData,
			SearchService elasticSearchService)
	{
		super(source, entityMetaData, elasticSearchService);
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		Object refEntityId = getSource().get(attributeName);
		if (refEntityId == null) return null;

		AttributeMetaData attribute = getEntityMetaData().getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		return getElasticsearchService().get(refEntityId, attribute.getRefEntity());
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		Object refEntityIdsObj = getSource().get(attributeName);
		if (refEntityIdsObj == null) return null;

		if (!(refEntityIdsObj instanceof Iterable<?>))
		{
			throw new RuntimeException("Expected Iterable<Object> instead of ["
					+ refEntityIdsObj.getClass().getSimpleName() + "]");
		}
		@SuppressWarnings("unchecked")
		Iterable<Object> refEntityIds = (Iterable<Object>) refEntityIdsObj;

		final AttributeMetaData attribute = getEntityMetaData().getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		return Iterables.transform(refEntityIds, new Function<Object, Entity>()
		{
			@Override
			public Entity apply(Object refEntityId)
			{
				return getElasticsearchService().get(refEntityId, attribute.getRefEntity());
			}
		});
	}
}
