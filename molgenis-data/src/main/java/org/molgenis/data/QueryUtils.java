package org.molgenis.data;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.StreamSupport.stream;

public class QueryUtils
{
	private static final char NESTED_ATTRIBUTE_SEPARATOR = '.';

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
		return ((sort != null) && stream(sort.spliterator(), false).anyMatch(
				order -> entityType.getAttribute(order.getAttr()).hasExpression()));
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
			Attribute attribute = getQueryRuleAttribute(rule, entityType);
			if (attribute != null && attribute.hasExpression())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns {@code true} if a given query contains any query rule with a nested attribute field (e.g. refAttr.attr).
	 *
	 * @param q query
	 * @return {@code true} if a given query contains any query rule with a nested attribute field
	 */
	public static boolean containsNestedQueryRuleField(Query<Entity> q)
	{
		return containsNestedQueryRuleFieldRec(q.getRules());
	}

	private static boolean containsNestedQueryRuleFieldRec(List<QueryRule> rules)
	{
		for (QueryRule rule : rules)
		{
			String queryRuleField = rule.getField();
			if (queryRuleField != null && queryRuleField.indexOf(NESTED_ATTRIBUTE_SEPARATOR) != -1)
			{
				return true;
			}

			List<QueryRule> nestedRules = rule.getNestedRules();
			if (nestedRules != null && containsNestedQueryRuleFieldRec(nestedRules))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the attribute for a query rule field.
	 *
	 * @param queryRule  query rule
	 * @param entityType entity type
	 * @return an attribute or {@code null} if the query rule field is {@code null}
	 * @throws UnknownAttributeException if the query rule field does not refer to an attribute
	 */
	public static Attribute getQueryRuleAttribute(QueryRule queryRule, EntityType entityType)
	{
		String queryRuleField = queryRule.getField();
		if (queryRuleField == null)
		{
			return null;
		}

		Attribute attr = null;
		String[] queryRuleFieldTokens = StringUtils.split(queryRuleField, NESTED_ATTRIBUTE_SEPARATOR);
		EntityType entityTypeAtCurrentDepth = entityType;
		for (int depth = 0; depth < queryRuleFieldTokens.length; ++depth)
		{
			String attrName = queryRuleFieldTokens[depth];
			attr = entityTypeAtCurrentDepth.getAttribute(attrName);
			if (attr == null)
			{
				throw new UnknownAttributeException(entityTypeAtCurrentDepth, attrName);
			}
			if (depth + 1 < queryRuleFieldTokens.length)
			{
				entityTypeAtCurrentDepth = attr.getRefEntity();
			}
		}
		return attr;
	}
}
