package org.molgenis.data.elasticsearch.request;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
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
	 * Writes a query to a {@link SearchRequestBuilder}.
	 * 
	 * @param searchRequestBuilder
	 * @param entityName
	 * @param searchType
	 * @param query
	 * @param aggAttr1
	 *            First Field to aggregate on
	 * @param aggAttr2
	 *            Second Field to aggregate on
	 * @param entityMetaData
	 */
	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, String entityName,
			SearchType searchType, Query<Entity> query, AttributeMetaData aggAttr1, AttributeMetaData aggAttr2,
			AttributeMetaData aggAttrDistinct, EntityMetaData entityMetaData)
	{
		searchRequestBuilder.setSearchType(searchType);

		// Document type
		if (entityName != null)
		{
			searchRequestBuilder.setTypes(entityName);
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
}
