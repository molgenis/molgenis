package org.molgenis.elasticsearch.request;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;

/**
 * Builds a ElasticSearch search request
 * 
 * @author erwin
 * 
 */
public class SearchRequestGenerator
{
	private final List<? extends QueryRulePartGenerator> generators = Arrays.asList(new QueryGenerator(),
			new SortGenerator(), new LimitOffsetGenerator(), new DisMaxQueryGenerator());
	private final SearchRequestBuilder searchRequestBuilder;

	public SearchRequestGenerator(SearchRequestBuilder searchRequestBuilder)
	{
		if (searchRequestBuilder == null)
		{
			throw new IllegalArgumentException("SearchRequestBuilder is null");
		}

		this.searchRequestBuilder = searchRequestBuilder;
	}

	/**
	 * Add the 'searchType', 'fields', 'types' and 'query' of the
	 * SearchRequestBuilder
	 * 
	 * @param entityName
	 *            , can be null
	 * @param searchType
	 * @param queryRules
	 *            , the queryRules to use, throws IllegalArgumentException if an
	 *            invalid QueryRule is used
	 * 
	 * @param fieldsToReturn
	 *            , can be null
	 * 
	 * @return SearchRequestBuilder
	 */
	public SearchRequestBuilder buildSearchRequest(List<String> entityNames, SearchType searchType,
			List<QueryRule> queryRules, List<String> fieldsToReturn)
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

		// Add queryrules to generators
		if (queryRules != null)
		{
			for (QueryRule queryRule : queryRules)
			{
				QueryRulePartGenerator generator = findGeneratorForOperator(queryRule.getOperator());

				if (generator == null)
				{
					throw new IllegalArgumentException("Operator [" + queryRule.getOperator()
							+ "] not implemented for elasticsearch");
				}

				generator.addQueryRule(queryRule);
			}
		}

		// Generate query
		for (QueryRulePartGenerator generator : generators)
		{
			generator.generate(searchRequestBuilder);
		}

		return searchRequestBuilder;
	}

	public SearchRequestBuilder buildSearchRequest(String entityName, SearchType searchType,
			List<QueryRule> queryRules, List<String> fieldsToReturn)
	{
		return buildSearchRequest(entityName == null ? null : Arrays.asList(entityName), searchType, queryRules,
				fieldsToReturn);
	}

	private QueryRulePartGenerator findGeneratorForOperator(Operator operator)
	{
		for (QueryRulePartGenerator generator : generators)
		{
			if (generator.supportsOperator(operator))
			{
				return generator;
			}
		}

		return null;
	}
}
