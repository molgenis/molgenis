package org.molgenis.framework.tupletable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;

import com.google.common.collect.Iterables;

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
		if (Operator.LIMIT.equals(rule.getOperator()) || Operator.LIMIT.equals(rule.getOperator()))
		{
			throw new TableException(
					"TupleTable doesn't support LIMIT or OFFSET QueryRules; use setLimit and setOffset instead");
		}

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

	@Override
	public QueryRule getSortRule()
	{
		final QueryRule sortAsc = getRule(Operator.SORTASC);
		if (sortAsc != null)
		{
			return sortAsc;
		}
		else
		{
			return getRule(Operator.SORTDESC);
		}
	}

	private QueryRule getRule(final Operator operator)
	{
		return Iterables.find(filters, new com.google.common.base.Predicate<QueryRule>()
		{
			@Override
			public boolean apply(@Nullable
			QueryRule arg0)
			{
				return arg0 != null ? arg0.getOperator() == operator : false;
			}
		});
	}
}
