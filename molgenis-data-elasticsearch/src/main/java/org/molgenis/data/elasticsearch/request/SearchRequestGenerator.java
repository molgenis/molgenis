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

import static java.util.Objects.requireNonNull;

/**
 * Builds a ElasticSearch search request
 *
 * @author erwin
 */
public class SearchRequestGenerator
{
	private final DocumentIdGenerator documentIdGenerator;
	private final AggregateQueryGenerator aggregateQueryGenerator;
	private final List<? extends QueryPartGenerator> queryGenerators;

	public SearchRequestGenerator(DocumentIdGenerator documentIdGenerator)
	{
		this.documentIdGenerator = requireNonNull(documentIdGenerator);
		aggregateQueryGenerator = new AggregateQueryGenerator(documentIdGenerator);
		queryGenerators = Arrays.asList(new QueryGenerator(documentIdGenerator), new SortGenerator(documentIdGenerator),
				new LimitOffsetGenerator());
	}

	/**
	 * Writes a query to a {@link SearchRequestBuilder}.
	 *
	 * @param searchRequestBuilder
	 * @param searchType
	 * @param entityType
	 * @param aggAttr1             First Field to aggregate on
	 * @param aggAttr2             Second Field to aggregate on
	 */
	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, SearchType searchType,
			EntityType entityType, Query<Entity> query, Attribute aggAttr1, Attribute aggAttr2,
			Attribute aggAttrDistinct)
	{
		searchRequestBuilder.setSearchType(searchType);

		// Document type
		if (entityType != null)
		{
			String documentType = documentIdGenerator.generateId(entityType);
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
