package org.molgenis.data.elasticsearch;

import org.elasticsearch.search.SearchHit;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;

public class ElasticsearchSearchHitEntity extends ElasticsearchDocumentEntity
{
	private static final long serialVersionUID = 1L;

	private final SearchHit searchHit;

	public ElasticsearchSearchHitEntity(SearchHit searchHit, EntityMetaData entityMetaData,
			SearchService elasticSearchService, EntityToSourceConverter entityToSourceConverter)
	{
		super(searchHit.getSource(), entityMetaData, elasticSearchService, entityToSourceConverter);
		this.searchHit = searchHit;
	}

	public float getScore()
	{
		return searchHit.getScore();
	}
}
