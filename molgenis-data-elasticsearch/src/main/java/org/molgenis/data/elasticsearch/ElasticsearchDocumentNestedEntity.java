package org.molgenis.data.elasticsearch;

import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

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
		// FIXME
		// Object value = refSource.get(attributeName);
		// if (value == null) return null;
		//
		// elasticSearchService.getById(documentType, id)ById(documentType, id)
		// AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		// if (attribute == null) throw new UnknownAttributeException(attributeName);
		//
		// return new ElasticsearchRefEntity((Map<String, Object>) value, attribute.getRefEntity());
		return null;
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		// FIXME
		// Object value = refSource.get(attributeName);
		// if (value == null) return Collections.emptyList();
		//
		// final AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		// if (attribute == null) throw new UnknownAttributeException(attributeName);
		//
		// return Iterables.transform((List<Map<String, Object>>) value, new Function<Map<String, Object>, Entity>()
		// {
		// @Override
		// public Entity apply(Map<String, Object> refSource)
		// {
		// return new ElasticsearchRefEntity(refSource, attribute.getRefEntity());
		// }
		// });
		return null;
	}
}
