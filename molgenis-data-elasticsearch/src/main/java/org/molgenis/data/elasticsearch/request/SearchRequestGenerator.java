package org.molgenis.data.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.Arrays;
import java.util.List;

/**
 * Builds a ElasticSearch search request
 *
 * @author erwin
 */
public class SearchRequestGenerator
{
	private final List<? extends QueryPartGenerator> queryGenerators;
	private final AggregateQueryGenerator aggregateQueryGenerator;

	public SearchRequestGenerator(DocumentIdGenerator documentIdGenerator)
	{
		aggregateQueryGenerator = new AggregateQueryGenerator(documentIdGenerator);
		queryGenerators = Arrays.asList(new QueryGenerator(documentIdGenerator), new SortGenerator(documentIdGenerator),
				new LimitOffsetGenerator());
	}

	/**
	 * Writes a query to a {@link SearchRequestBuilder}.
	 *
	 * @param searchRequestBuilder
	 * @param documentType
	 * @param searchType
	 * @param aggAttr1             First Field to aggregate on
	 * @param aggAttr2             Second Field to aggregate on
	 * @param entityType
	 */
	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, String documentType,
			SearchType searchType,
			Query<Entity> query, Attribute aggAttr1, Attribute aggAttr2,
			Attribute aggAttrDistinct, EntityType entityType)
	{
		searchRequestBuilder.setSearchType(searchType);

		// Document type
		if (documentType != null)
		{
			searchRequestBuilder.setTypes(documentType);
		}

		// Generate query
		if (query != null)
		{
			for (QueryPartGenerator generator : queryGenerators)
			{
				generator.generate(searchRequestBuilder, query, entityType);
			}
		}

		// Aggregates
		if (aggAttr1 != null || aggAttr2 != null)
		{
			aggregateQueryGenerator.generate(searchRequestBuilder, aggAttr1, aggAttr2, aggAttrDistinct);
		}
	}
}
