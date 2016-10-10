package org.molgenis.data.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;

/**
 * Sets the 'from' and 'size' fields of the SearchRequestBuilder object.
 *
 * @author erwin
 */
public class LimitOffsetGenerator implements QueryPartGenerator
{

	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query<Entity> query, EntityType entityType)
	{
		if (query.getOffset() > 0)
		{
			searchRequestBuilder.setFrom(query.getOffset());
		}
		else
		{
			searchRequestBuilder.setFrom(0);
		}

		if (query.getPageSize() > 0)
		{
			searchRequestBuilder.setSize(query.getPageSize());
		}
	}
}
