package org.molgenis.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.molgenis.data.Query;

/**
 * Sets the 'from' and 'size' fields of the SearchRequestBuilder object.
 * 
 * @author erwin
 * 
 */
public class LimitOffsetGenerator implements QueryPartGenerator
{

	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query query)
	{
		if (query.getOffset() > 0)
		{
			searchRequestBuilder.setFrom(query.getOffset());
		}

		if (query.getPageSize() > 0)
		{
			searchRequestBuilder.setSize(query.getPageSize());
		}
	}
}
