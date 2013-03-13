package org.molgenis.elasticsearch.request;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;

/**
 * Base class for QueryPartGenerator implementations
 * 
 * @author erwin
 * 
 */
public abstract class AbstractQueryRulePartGenerator implements QueryRulePartGenerator
{
	protected List<QueryRule> queryRules = new ArrayList<QueryRule>();
	private final List<Operator> supportedOperators;

	public AbstractQueryRulePartGenerator(List<Operator> supportedOperators)
	{
		if (supportedOperators == null)
		{
			throw new IllegalArgumentException("SupportedOperators is null");
		}

		if (supportedOperators.isEmpty())
		{
			throw new IllegalArgumentException("SupportedOperators is empty");
		}

		this.supportedOperators = supportedOperators;
	}

	@Override
	public boolean supportsOperator(Operator operator)
	{
		return supportedOperators.contains(operator);
	}

	@Override
	public abstract void generate(SearchRequestBuilder searchRequestBuilder);

	@Override
	public void addQueryRule(QueryRule queryRule)
	{
		if (!supportedOperators.contains(queryRule.getOperator()))
		{
			throw new IllegalArgumentException("Operator [" + queryRule.getOperator()
					+ "] not supported by generator [" + getClass().getName() + "] ");
		}

		queryRules.add(queryRule);
	}
}
