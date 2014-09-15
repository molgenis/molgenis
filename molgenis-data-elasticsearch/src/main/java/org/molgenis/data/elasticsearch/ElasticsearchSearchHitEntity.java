package org.molgenis.data.elasticsearch;

import org.elasticsearch.search.SearchHit;
import org.molgenis.data.EntityMetaData;

public class ElasticsearchSearchHitEntity extends ElasticsearchDocumentEntity
{
	private static final long serialVersionUID = 1L;

	private final SearchHit searchHit;

	public ElasticsearchSearchHitEntity(SearchHit searchHit, EntityMetaData entityMetaData,
			SearchService elasticSearchService)
	{
		super(searchHit.getSource(), entityMetaData, elasticSearchService);
		this.searchHit = searchHit;
	}

	public float getScore()
	{
		return searchHit.getScore();
	}
}
