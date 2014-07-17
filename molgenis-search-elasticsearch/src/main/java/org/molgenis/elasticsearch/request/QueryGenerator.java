package org.molgenis.elasticsearch.request;

import static org.molgenis.data.QueryRule.Operator.AND;
import static org.molgenis.data.QueryRule.Operator.NESTED;
import static org.molgenis.data.QueryRule.Operator.OR;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BaseQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;

/**
 * Sets the Query of the SearchRequestBuilder object.
 * 
 * @author erwin
 * 
 */
public class QueryGenerator implements QueryPartGenerator
{
	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query query, EntityMetaData entityMetaData)
	{
		if (hasMrefField(query.getRules(), entityMetaData))
		{
			BaseQueryBuilder queryBuilder = recursiveBuildQuery(query.getRules(), entityMetaData);
			searchRequestBuilder.setQuery(queryBuilder);
		}
		else
		{
			searchRequestBuilder.setQuery(QueryBuilders.queryString(
					LuceneQueryStringBuilder.buildQueryString(query.getRules())).defaultOperator(Operator.AND));
		}
	}

	private BaseQueryBuilder recursiveBuildQuery(List<QueryRule> queryRules, EntityMetaData entityMetaData)
	{
		// Check the number of queryRules, if there is only one queryRule, use
		// elasticsearch query_string builder directly
		if (queryRules.size() == 1)
		{
			return generateQuery(queryRules.get(0), entityMetaData);
		}
		else
		{
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			BaseQueryBuilder previousQueryBuilder = null;
			QueryRule previousOperatorRule = new QueryRule(AND);
			for (QueryRule queryRule : queryRules)
			{
				if (queryRule.getOperator().equals(AND) || queryRule.getOperator().equals(OR))
				{
					previousOperatorRule.setOperator(queryRule.getOperator());
					break;
				}
			}

			for (QueryRule queryRule : queryRules)
			{
				if (queryRule.getOperator().equals(AND) || queryRule.getOperator().equals(OR))
				{
					previousOperatorRule = queryRule;
				}
				else
				{
					previousQueryBuilder = generateQuery(queryRule, entityMetaData);
				}
				// Add current query depending on what operator is in the
				// previous
				// operator query
				if (previousQueryBuilder != null && previousOperatorRule != null
						&& previousOperatorRule.getOperator().equals(AND))
				{
					boolQueryBuilder.must(previousQueryBuilder);
					previousOperatorRule = null;
					previousQueryBuilder = null;
				}
				else if (previousQueryBuilder != null && previousOperatorRule != null
						&& previousOperatorRule.getOperator().equals(OR))
				{
					boolQueryBuilder.should(previousQueryBuilder);
					previousOperatorRule = null;
					previousQueryBuilder = null;
				}
			}
			return boolQueryBuilder;
		}
	}

	private BaseQueryBuilder generateQuery(QueryRule queryRule, EntityMetaData entityMetaData)
	{
		BaseQueryBuilder previousQueryBuilder = null;
		if (queryRule.getOperator().equals(NESTED))
		{
			previousQueryBuilder = recursiveBuildQuery(queryRule.getNestedRules(), entityMetaData);
		}
		else if (entityMetaData != null
				&& entityMetaData.getAttribute(queryRule.getField()).getDataType().getEnumType().toString()
						.equalsIgnoreCase(MolgenisFieldTypes.MREF.toString()))
		{
			EntityMetaData refEntity = entityMetaData.getAttribute(queryRule.getField()).getRefEntity();
			previousQueryBuilder = QueryBuilders.nestedQuery(
					queryRule.getField(),
					QueryBuilders.queryString(queryRule.getField() + "." + refEntity.getLabelAttribute().getName()
							+ ":" + queryRule.getValue()));
		}
		else
		{
			previousQueryBuilder = QueryBuilders.queryString(LuceneQueryStringBuilder.buildQueryString(Arrays
					.asList(queryRule)));
		}
		return previousQueryBuilder;
	}

	private boolean hasMrefField(List<QueryRule> queryRules, EntityMetaData entityMetaData)
	{
		if (entityMetaData == null || queryRules == null) return false;

		for (QueryRule queryRule : queryRules)
		{
			if (queryRule.getField() != null)
			{
				if (entityMetaData.getAttribute(queryRule.getField()).getDataType().getEnumType().toString()
						.equalsIgnoreCase(MolgenisFieldTypes.MREF.toString()))
				{
					return true;
				}
			}
			if (hasMrefField(queryRule.getNestedRules(), entityMetaData)) return true;
		}
		return false;
	}
}
