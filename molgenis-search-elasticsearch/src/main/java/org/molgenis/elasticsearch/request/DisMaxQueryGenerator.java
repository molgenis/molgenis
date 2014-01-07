package org.molgenis.elasticsearch.request;

import static org.molgenis.data.QueryRule.Operator.DIS_MAX;
import static org.molgenis.data.QueryRule.Operator.SHOULD;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BaseQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;

public class DisMaxQueryGenerator implements QueryPartGenerator
{

	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query query)
	{
		if (!query.getRules().isEmpty())
		{
			// SHOULD and DIS_MAX or always the first QueryRule
			if (query.getRules().get(0).getOperator() == SHOULD || query.getRules().get(0).getOperator() == DIS_MAX)
			{
				searchRequestBuilder.setQuery(buildQueryString(query.getRules().get(0)));
			}
		}
	}

	public BaseQueryBuilder buildQueryString(QueryRule queryRule)
	{
		if (queryRule.getOperator().equals(SHOULD))
		{
			BoolQueryBuilder builder = QueryBuilders.boolQuery();
			for (QueryRule subQuery : queryRule.getNestedRules())
			{
				builder.should(buildQueryString(subQuery));
			}
			return builder;
		}
		else if (queryRule.getOperator().equals(DIS_MAX))
		{
			DisMaxQueryBuilder builder = QueryBuilders.disMaxQuery();
			for (QueryRule subQuery : queryRule.getNestedRules())
			{
				builder.add(buildQueryString(subQuery));
			}
			builder.tieBreaker((float) 0.0);
			if (queryRule.getValue() != null)
			{
				builder.boost(Float.parseFloat(queryRule.getValue().toString()));
			}
			return builder;
		}
		else
		{
			return QueryBuilders.fieldQuery(queryRule.getField(), queryRule.getValue());
		}
	}
}