package org.molgenis.data;

import java.util.EnumSet;
import java.util.Set;

import org.molgenis.data.QueryRule.Operator;

public class QueryUtils
{
	public static boolean containsOperator(Query q, Operator operator)
	{
		return containsAnyOperator(q, EnumSet.of(operator));
	}

	public static boolean containsAnyOperator(Query q, Set<Operator> operators)
	{
		boolean searchOperator = q.getRules().stream().anyMatch(e -> {
			return operators.contains(e.getOperator());
		});

		return searchOperator;
	}
}
