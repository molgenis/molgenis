package org.molgenis.elasticsearch.request;

import static org.molgenis.framework.db.QueryRule.Operator.DIS_MAX;
import static org.molgenis.framework.db.QueryRule.Operator.SHOULD;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BaseQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;

public class DisMaxQueryGenerator extends AbstractQueryRulePartGenerator
{
	private static final List<Operator> SUPPORTED_OPERATORS = Arrays.asList(SHOULD, DIS_MAX);

	public DisMaxQueryGenerator()
	{
		super(SUPPORTED_OPERATORS);
	}

	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder)
	{
		if (queryRules.size() > 0) searchRequestBuilder.setQuery(buildQueryString(queryRules.get(0)));
	}

	public BaseQueryBuilder buildQueryString(QueryRule query)
	{
		if (query.getOperator().equals(SHOULD))
		{
			BoolQueryBuilder builder = QueryBuilders.boolQuery();
			for (QueryRule subQuery : query.getNestedRules())
			{
				builder.should(buildQueryString(subQuery));
			}
			return builder;
		}
		else if (query.getOperator().equals(DIS_MAX))
		{
			DisMaxQueryBuilder builder = QueryBuilders.disMaxQuery();
			for (QueryRule subQuery : query.getNestedRules())
			{
				builder.add(buildQueryString(subQuery));
			}
			builder.tieBreaker(0);
			return builder;
		}
		else
		{
			String value = query.getValue().toString();
			boolean boost = false;
			if (value.contains("(^3)"))
			{
				query.setValue(value.substring(0, value.length() - 4));
				boost = true;
			}
			QueryStringQueryBuilder builder = new QueryStringQueryBuilder(
					LuceneQueryStringBuilder.buildQueryString(Arrays.asList(query)));
			if (boost) builder.boost((float) 3.0);
			return builder;
		}
	}
}
