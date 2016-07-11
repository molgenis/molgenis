package org.molgenis.data;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

public class QueryUtils
{
	public static boolean containsOperator(Query q, Operator operator)
	{
		return containsAnyOperator(q, EnumSet.of(operator));
	}

	public static boolean containsAnyOperator(Query q, Set<Operator> operators)
	{
		return containsAnyOperator(q.getRules(), operators);
	}

	public static boolean containsAnyOperator(List<QueryRule> rules, Set<Operator> operators)
	{
		for (QueryRule rule : rules)
		{
			if (!rule.getNestedRules().isEmpty() && containsAnyOperator(rule.getNestedRules(), operators))
			{
				return true;
			}

			if (operators.contains(rule.getOperator()))
			{
				return true;
			}
		}

		return false;
	}

	public static boolean containsComputedAttribute(List<QueryRule> rules, EntityMetaData entityMetaData)
	{
		for (QueryRule rule : rules)
		{
			List<QueryRule> nestedRules = rule.getNestedRules();
			if (!nestedRules.isEmpty() && containsComputedAttribute(nestedRules, entityMetaData))
			{
				return true;
			}

			AttributeMetaData amd = entityMetaData.getAttribute(rule.getField());
			if (amd != null && amd.getExpression() != null)
			{
				return true;
			}
		}

		return false;
	}
}
