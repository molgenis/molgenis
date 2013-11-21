package org.molgenis.framework.tupletable;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.QueryRule;

public abstract class AbstractFilterableTupleTable extends AbstractTupleTable implements FilterableTupleTable
{
	private List<QueryRule> filters = new ArrayList<QueryRule>();

	protected AbstractFilterableTupleTable()
	{
	}

	protected AbstractFilterableTupleTable(List<QueryRule> rules)
	{
		if (rules != null)
		{
			filters = rules;
		}
	}

	@Override
	public void reset()
	{
		super.reset();

		filters = new ArrayList<QueryRule>();
	}

	@Override
	public void setFilters(List<QueryRule> rules) throws TableException
	{
		if (rules == null)
		{
			throw new NullPointerException("rules cannot be null");
		}

		for (final QueryRule r : rules)
		{
			verifyRulesRecursive(r);
		}
		filters = rules;
	}

	private void verifyRulesRecursive(QueryRule rule) throws TableException
	{
		if (rule.getNestedRules() != null) for (QueryRule r : rule.getNestedRules())
		{
			verifyRulesRecursive(r);
		}
	}

	@Override
	public List<QueryRule> getFilters()
	{
		return filters;
	}

}
