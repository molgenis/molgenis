package org.molgenis.elasticsearch.request;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.molgenis.data.Query;
import org.molgenis.elasticsearch.index.MappingsBuilder;

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
			searchRequestBuilder.setSize(0);

			TermsBuilder termsBuilder;
			if (StringUtils.isNotBlank(aggregateField1) && StringUtils.isNotBlank(aggregateField2))
			{
				termsBuilder = new TermsBuilder(aggregateField1).size(Integer.MAX_VALUE).field(
						aggregateField1 + '.' + MappingsBuilder.FIELD_NOT_ANALYZED);
				TermsBuilder subTermsBuilder = new TermsBuilder(aggregateField2).size(Integer.MAX_VALUE).field(
						aggregateField2 + '.' + MappingsBuilder.FIELD_NOT_ANALYZED);
				termsBuilder.subAggregation(subTermsBuilder);
			}
			else if (StringUtils.isNotBlank(aggregateField1))
			{
				termsBuilder = new TermsBuilder(aggregateField1).size(Integer.MAX_VALUE).field(
						aggregateField1 + '.' + MappingsBuilder.FIELD_NOT_ANALYZED);
			}
			else
			{
				termsBuilder = new TermsBuilder(aggregateField2).size(Integer.MAX_VALUE).field(
						aggregateField2 + '.' + MappingsBuilder.FIELD_NOT_ANALYZED);
			}
			searchRequestBuilder.addAggregation(termsBuilder);
		}
	}

	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, String entityName, SearchType searchType,
			Query query, List<String> fieldsToReturn, String aggregateField1, String aggregateField2)
	{
		buildSearchRequest(searchRequestBuilder, entityName == null ? null : Arrays.asList(entityName), searchType,
				query, fieldsToReturn, aggregateField1, aggregateField2);
	}
}
