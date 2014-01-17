package org.molgenis.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.data.Query;

/**
 * Sets the Query of the SearchRequestBuilder object.
 * 
 * @author erwin
 * 
 */
public class QueryGenerator implements QueryPartGenerator
{
	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query query)
	{
		String queryString = LuceneQueryStringBuilder.buildQueryString(query.getRules());
		searchRequestBuilder.setQuery(QueryBuilders.queryString(queryString));
	}

}
