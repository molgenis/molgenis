package org.molgenis.elasticsearch.request;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet.ComparatorType;
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
			SearchType searchType, Query query, List<String> fieldsToReturn, String aggregateField1,
			String aggregateField2)
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

		// Aggregates
		if (StringUtils.isNotBlank(aggregateField1) || StringUtils.isNotBlank(aggregateField2))
		{
			StringBuilder sb = new StringBuilder();
			if (StringUtils.isNotBlank(aggregateField1))
			{
				sb.append(aggregateField1);
				if (StringUtils.isNotBlank(aggregateField2))
				{
					sb.append("~");
				}
			}

			if (StringUtils.isNotBlank(aggregateField2))
			{
				sb.append(aggregateField2);
			}

			searchRequestBuilder.addFacet(FacetBuilders.termsFacet(sb.toString()).fields(sb.toString()).allTerms(true)
					.size(Integer.MAX_VALUE).order(ComparatorType.TERM));
		}
	}

	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, String entityName, SearchType searchType,
			Query query, List<String> fieldsToReturn, String aggregateField1, String aggregateField2)
	{
		buildSearchRequest(searchRequestBuilder, entityName == null ? null : Arrays.asList(entityName), searchType,
				query, fieldsToReturn, aggregateField1, aggregateField2);
	}
}
