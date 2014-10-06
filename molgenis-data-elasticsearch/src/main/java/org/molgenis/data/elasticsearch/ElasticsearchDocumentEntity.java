package org.molgenis.data.elasticsearch;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.UnknownAttributeException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Elasticsearch entity containing all referenced entities
 */
public class ElasticsearchDocumentEntity extends ElasticsearchEntity
{
	private static final long serialVersionUID = 1L;

	private final SearchService elasticSearchService;

	public ElasticsearchDocumentEntity(Map<String, Object> source, EntityMetaData entityMetaData,
			SearchService elasticSearchService)
	{
		super(source, entityMetaData);
		if (elasticSearchService == null) throw new IllegalArgumentException("elasticSearchService is null");
		this.elasticSearchService = elasticSearchService;
	}

	@Override
	public Object getIdValue()
	{
		return getSource().get(getEntityMetaData().getIdAttribute().getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Entity getEntity(String attributeName)
	{
		Object value = getSource().get(attributeName);
		if (value == null) return null;

		AttributeMetaData attribute = getEntityMetaData().getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		return new ElasticsearchDocumentEntity((Map<String, Object>) value, attribute.getRefEntity(),
				elasticSearchService);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		Object value = getSource().get(attributeName);
		if (value == null) return Collections.emptyList();
        if (value instanceof Iterable) {
            final Iterator iterator = ((Iterable) value).iterator();
            if(iterator.hasNext()){
                if(iterator.next() instanceof Entity)
                    return (Iterable<Entity>) value;
            }
            else return Collections.emptyList();
        }
        final AttributeMetaData attribute = getEntityMetaData().getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		return Iterables.transform((List<Map<String, Object>>) value, new Function<Map<String, Object>, Entity>()
		{
			@Override
			public Entity apply(Map<String, Object> refSource)
			{
				return new ElasticsearchDocumentEntity(refSource, attribute.getRefEntity(), elasticSearchService);
			}
		});
	}

	protected SearchService getElasticsearchService()
	{
		return elasticSearchService;
	}
}
