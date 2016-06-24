package org.molgenis.data.elasticsearch.request;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;

/**
 * Builds a ElasticSearch search request
 * 
 * @author erwin
 * 
 */
public class SearchRequestGenerator
{
	private final List<? extends QueryPartGenerator> queryGenerators;
	private final AggregateQueryGenerator aggregateQueryGenerator;

	public SearchRequestGenerator()
	{
		aggregateQueryGenerator = new AggregateQueryGenerator();
		queryGenerators = Arrays.asList(new QueryGenerator(), new SortGenerator(), new LimitOffsetGenerator(),
				new SourceFilteringGenerator());
	}

	/**
	 * Add the 'searchType', 'fields', 'types' and 'query' of the SearchRequestBuilder
	 * 
	 * @param searchRequestBuilder
	 * @param entityNames
	 * @param searchType
	 * @param query
	 * @param aggAttr1
	 *            First Field to aggregate on
	 * @param aggAttr2
	 *            Second Field to aggregate on
	 * @param entityMetaData
	 */
	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, List<String> entityNames,
			SearchType searchType, Query query, AttributeMetaData aggAttr1, AttributeMetaData aggAttr2,
			AttributeMetaData aggAttrDistinct, EntityMetaData entityMetaData)
	{
		searchRequestBuilder.setSearchType(searchType);

		// Document type
		if (entityNames != null)
		{
			searchRequestBuilder.setTypes(entityNames.toArray(new String[entityNames.size()]));
		}

		// Generate query
		if (query != null)
		{
			for (QueryPartGenerator generator : queryGenerators)
			{
				generator.generate(searchRequestBuilder, query, entityMetaData);
			}
		}

		// Aggregates
		if (aggAttr1 != null || aggAttr2 != null)
		{
			aggregateQueryGenerator.generate(searchRequestBuilder, aggAttr1, aggAttr2, aggAttrDistinct);
		}

	}

	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, String entityName, SearchType searchType,
			Query query, AttributeMetaData aggregateField1, AttributeMetaData aggregateField2,
			AttributeMetaData aggregateFieldDistinct, EntityMetaData entityMetaData)
	{
		buildSearchRequest(searchRequestBuilder, entityName == null ? null : Arrays.asList(entityName), searchType,
				query, aggregateField1, aggregateField2, aggregateFieldDistinct, entityMetaData);
	}
}
