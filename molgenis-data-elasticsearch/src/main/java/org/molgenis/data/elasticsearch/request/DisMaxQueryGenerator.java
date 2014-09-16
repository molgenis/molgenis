package org.molgenis.data.elasticsearch.request;

import static org.molgenis.data.QueryRule.Operator.DIS_MAX;
import static org.molgenis.data.QueryRule.Operator.SHOULD;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BaseQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;

public class DisMaxQueryGenerator implements QueryPartGenerator
{
	private static final String LUCENE_ESCAPE_CHARS_VALUE = "[-&+!\\|\\(\\){}\\[\\]\"\\*\\?:\\\\\\/]";
	private static final Pattern LUCENE_PATTERN_VALUE = Pattern.compile(LUCENE_ESCAPE_CHARS_VALUE);
	private static final String REPLACEMENT_STRING = "\\\\$0";

	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query query, EntityMetaData entityMetaData)
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
			String value = escapeValue(queryRule.getValue() != null ? queryRule.getValue().toString() : StringUtils.EMPTY);
			StringBuilder queryStringBuilder = new StringBuilder();
			queryStringBuilder.append(queryRule.getField()).append(":(").append(value).append(')');
			return QueryBuilders.queryString(queryStringBuilder.toString());
		}
	}

	public static String escapeValue(String value)
	{
		return LUCENE_PATTERN_VALUE.matcher(value).replaceAll(REPLACEMENT_STRING);
	}
}