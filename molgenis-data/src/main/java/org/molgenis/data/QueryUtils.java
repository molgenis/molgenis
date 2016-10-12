package org.molgenis.data;

import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.StreamSupport.stream;

public class QueryUtils
{
	public static boolean containsOperator(Query<Entity> q, Operator operator)
	{
		return containsAnyOperator(q, EnumSet.of(operator));
	}

	public static boolean containsAnyOperator(Query<Entity> q, Set<Operator> operators)
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

	public static boolean containsComputedAttribute(Query<Entity> query, EntityType entityType)
	{
		return (containsComputedAttribute(query.getSort(), entityType) || containsComputedAttribute(query.getRules(),
				entityType));
	}

	public static boolean containsComputedAttribute(Sort sort, EntityType entityType)
	{
		return ((sort != null) && !stream(sort.spliterator(), false)
				.allMatch(order -> !entityType.getAttribute(order.getAttr()).hasExpression()));
	}

	public static boolean containsComputedAttribute(Iterable<QueryRule> rules, EntityType entityType)
	{
		for (QueryRule rule : rules)
		{
			List<QueryRule> nestedRules = rule.getNestedRules();
			if (!nestedRules.isEmpty() && containsComputedAttribute(nestedRules, entityType))
			{
				return true;
			}
			Attribute attribute = entityType.getAttribute(rule.getField());
			if (attribute != null && attribute.hasExpression())
			{
				return true;
			}
		}

		return false;
	}
}
