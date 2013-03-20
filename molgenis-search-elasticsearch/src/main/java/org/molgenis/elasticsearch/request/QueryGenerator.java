package org.molgenis.elasticsearch.request;

import static org.molgenis.framework.db.QueryRule.Operator.AND;
import static org.molgenis.framework.db.QueryRule.Operator.EQUALS;
import static org.molgenis.framework.db.QueryRule.Operator.GREATER;
import static org.molgenis.framework.db.QueryRule.Operator.GREATER_EQUAL;
import static org.molgenis.framework.db.QueryRule.Operator.LESS;
import static org.molgenis.framework.db.QueryRule.Operator.LESS_EQUAL;
import static org.molgenis.framework.db.QueryRule.Operator.LIKE;
import static org.molgenis.framework.db.QueryRule.Operator.NOT;
import static org.molgenis.framework.db.QueryRule.Operator.OR;
import static org.molgenis.framework.db.QueryRule.Operator.SEARCH;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.framework.db.QueryRule.Operator;

/**
 * Sets the Query of the SearchRequestBuilder object.
 * 
 * @author erwin
 * 
 */
public class QueryGenerator extends AbstractQueryRulePartGenerator
{
	private static final List<Operator> SUPPORTED_OPERATORS = Arrays.asList(AND, OR, NOT, EQUALS, LIKE, LESS,
			LESS_EQUAL, GREATER, GREATER_EQUAL, SEARCH);

	public QueryGenerator()
	{
		super(SUPPORTED_OPERATORS);
	}

	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder)
	{
		String queryString = LuceneQueryStringBuilder.buildQueryString(queryRules);
		searchRequestBuilder.setQuery(QueryBuilders.queryString(queryString));
	}

}
