package org.molgenis.elasticsearch.request;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.molgenis.data.Query;

/**
 * Builds a ElasticSearch search request
 * 
 * @author erwin
 * 
 */
public class SearchRequestGenerator
{
	private final List<? extends QueryPartGenerator> generators = Arrays.asList(new QueryGenerator(),
			new SortGenerator(), new LimitOffsetGenerator(), new DisMaxQueryGenerator());

	/**
	 * Add the 'searchType', 'fields', 'types' and 'query' of the SearchRequestBuilder
	 * 
	 * @param entityName
	 *            , can be null
	 * @param searchType
	 * @param queryRules
	 *            , the queryRules to use, throws IllegalArgumentException if an invalid QueryRule is used
	 * 
	 * @param fieldsToReturn
	 *            , can be null
	 * 
	 * @return SearchRequestBuilder
	 */
	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, List<String> entityNames,
			SearchType searchType, Query query, List<String> fieldsToReturn)
	{
		searchRequestBuilder.setSearchType(searchType);

		// Document type
		if (entityNames != null)
		{
			searchRequestBuilder.setTypes(entityNames.toArray(new String[entityNames.size()]));
		}

		// Fields
		if ((fieldsToReturn != null) && !fieldsToReturn.isEmpty())
		{
			searchRequestBuilder.addFields(fieldsToReturn.toArray(new String[fieldsToReturn.size()]));
		}

		// Generate query
		for (QueryPartGenerator generator : generators)
		{
			generator.generate(searchRequestBuilder, query);
		}
	}

	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, String entityName, SearchType searchType,
			Query query, List<String> fieldsToReturn)
	{
		buildSearchRequest(searchRequestBuilder, entityName == null ? null : Arrays.asList(entityName), searchType,
				query, fieldsToReturn);
	}
}
