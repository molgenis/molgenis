/**
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved Changelog:
 * <ul>
 * <li>2005-05-03; 1.0.0; A Lubbers; Creation.
 * <li>2005-11-29; 1.0.0; RA Scheltema; Merge from the old java invengine-style
 * to the new. Also added documentation.
 * <li>2007-05-14; 1.2.0; MA Swertz, updated to work with mapper.
 * </ul>
 */

package org.molgenis.framework.db;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * With this class an equation model can be described for a database-field (eg a
 * column). By combining this description into a single class a convenient way
 * for passing rules to the
 * {@link org.molgenis.framework.db.Database#find(Class, QueryRule[])
 * Database#find(Class, QueryRule[])}.
 * 
 * <pre>
 * QueryRule rule = new QueryRule(&quot;Name&quot;, QueryRule.Operator.EQUALS, &quot;richard&quot;);
 * database.find(Person.class, rule);
 * </pre>
 * 
 * @author Alrik Lubbers
 * @author Richard Scheltema
 * @author Morris Swertz
 */
public class QueryRule
{
	/** The operator being applied to the field and value */
	protected Operator operator;

	/** The field-name (eq column-name) in the database */
	protected String field = null;

	/** The value to compare entries of the field in the database with */
	protected Object value = null;

	/** Place to store nested rules */
	// FIXME: why not put this in value?
	private QueryRule[] nestedRules;

	public QueryRule()
	{

	}

	public QueryRule(QueryRule copy)
	{
		operator = copy.operator;
		field = copy.field;
		value = copy.value;
		if (copy.nestedRules != null)
		{
			nestedRules = new QueryRule[copy.nestedRules.length];
			for (int i = 0; i < copy.nestedRules.length; i++)
			{
				nestedRules[i] = new QueryRule(copy.nestedRules[i]);
			}
		}
	}

	/**
	 * Different types of rules that can be applied.
	 */
	public enum Operator
	{
		/** search all fields */
		SEARCH("search"),
		/** 'field' equal to 'value' */
		EQUALS("="),
		/** 'field' in 'value' (value being a list). */
		IN("IN"),
		/** 'field in (value)' with value being a subquery */
		IN_SUBQUERY("IN_SUB"),
		/** 'content of subQuery 8 */
		SUBQUERY("SUBQUERY"),
		/** 'field' less-than 'value' */
		LESS("<"),
		/** 'field' equal-or-less-than 'value' */
		LESS_EQUAL("<="),
		/** 'field' greater-than 'value' */
		GREATER(">"),
		/** 'field' equal-or-greater-than 'value' */
		GREATER_EQUAL(">="),
		/** 'field' equal to '%value%' (% is a wildcard) */
		LIKE("LIKE"),
		/** 'field' not-equal to 'value' */
		NOT("!="),
		/**
		 * limit results to 'value' elements (value being an int). The paramater
		 * 'field' is ommitted.
		 */
		LIMIT("LIMIT"),
		/**
		 * show results from value-th element (value being an int offset
		 * starting from 1. The paramater 'field' is ommitted.
		 */
		OFFSET("OFFSET"),
		/**
		 * order the result by 'field', ascending. The parameter 'value' is
		 * ommitted.
		 */
		SORTASC("SORTASC"),
		/**
		 * order the result by 'field', descending. The parameter 'value' is
		 * ommitted.
		 */
		SORTDESC("SORTDESC"),
		/**
		 * AND operation
		 */
		AND("AND"),
		/**
		 * OR operation
		 */
		OR("OR"),
		/**
		 * indicates that 'value' is a nested array of QueryRule. The parameter
		 * 'field' is ommitted.
		 */
		NESTED(""),
		/** show the last elements from the list, so LIMIT from the end */
		LAST(""),
		/** enables the joining of two fields; value is a fieldname */
		JOIN("JOIN");

		private String label;

		/**
		 * Translate String label of the operator to Operator.
		 * 
		 * @param name
		 *            of the operator
		 */
		Operator(String label)
		{
			this.label = label;
		}

		/**
		 * Get the String label of the Operator.
		 */
		@Override
		public String toString()
		{
			return label;
		}
	}

	// constructor
	/**
	 * Standard constructor.
	 * <p>
	 * With this constructor the field, operator and value are set in one go, so
	 * there is no need for additional statements.
	 * 
	 * @param field
	 *            The field-name.
	 * @param operator
	 *            The operator to use for comparing entries in the field with
	 *            the value.
	 * @param value
	 *            The value.
	 */
	public QueryRule(String field, Operator operator, Object value)
	{
		if (operator == Operator.LIMIT || operator == Operator.OFFSET || operator == Operator.SORTASC
				|| operator == Operator.SORTDESC || operator == Operator.LAST || operator == Operator.AND
				|| operator == Operator.OR)
		{
			throw new IllegalArgumentException("QueryRule(): Operator." + operator
					+ " cannot be used with two arguments");
		}
		this.field = field;
		this.operator = operator;
		this.value = value;
	}

	/**
	 * Constructor to create a nested rule set.
	 * 
	 * @param rules
	 *            to be nested.
	 */
	public QueryRule(QueryRule... rules)
	{
		operator = Operator.NESTED;
		nestedRules = rules;
	}

	/**
	 * Specific constructor for rules that do not apply to a field such as LIMIT
	 * and OFFSET.
	 * 
	 * @param operator
	 * @param value
	 */
	public QueryRule(Operator operator, Object value)
	{
		if (operator == Operator.LIMIT || operator == Operator.OFFSET || operator == Operator.SORTASC
				|| operator == Operator.SORTDESC || operator == Operator.SEARCH)
		{
			this.operator = operator;
			this.value = value;
		}
		else
		{
			throw new IllegalArgumentException("QueryRule(): Operator." + operator
					+ " cannot be used with one argument");
		}
	}

	public QueryRule(Operator operator, QueryRule nestedRules)
	{
		if (operator == Operator.NOT || operator == Operator.IN_SUBQUERY)
		{
			this.operator = operator;
			this.nestedRules = new QueryRule[]
			{ nestedRules };
		}
		else
		{
			throw new IllegalArgumentException("QueryRule(): Operator." + operator
					+ " cannot be used with one argument");
		}
	}

	/**
	 * Specific constructor for rules that don't have a value or field such as
	 * LAST
	 */
	public QueryRule(Operator operator)
	{
		if (operator == Operator.LAST || operator == Operator.AND || operator == Operator.OR)
		{
			this.operator = operator;
		}
		else
		{
			throw new IllegalArgumentException("QueryRule(): Operator '" + operator
					+ "' cannot be used without arguments");
		}
	}

	public QueryRule(List<QueryRule> rules)
	{
		this(rules.toArray(new QueryRule[rules.size()]));
	}

	public QueryRule(String field, Operator equals, String value)
	{
		this(field, equals, (Object) value);
	}

	/**
	 * Returns the field-name set for this rule.
	 * 
	 * @return The field-name.
	 */
	public String getField()
	{
		return field;
	}

	/**
	 * Returns the field-name as a JPA Attribute
	 */
	public String getJpaAttribute()
	{
		if (!StringUtils.isEmpty(field))
		{
			return field.substring(0, 1).toLowerCase() + field.substring(1);
		}
		return field;
	}

	/**
	 * Sets a new field-name for this rule.
	 * 
	 * @param field
	 *            The new field-name.
	 */
	public void setField(String field)
	{
		this.field = field;
	}

	/**
	 * Returns the operator set for this rule.
	 * 
	 * @return The operator.
	 */
	public Operator getOperator()
	{
		return operator;
	}

	/**
	 * Sets a new operator for this rule.
	 * 
	 * @param operator
	 *            The new operator.
	 */
	public void setOperator(Operator operator)
	{
		this.operator = operator;
	}

	/**
	 * Returns the value set for this rule.
	 * 
	 * @return The value.
	 */
	public Object getValue()
	{
		return value;
	}

	/**
	 * Sets a new value for this rule.
	 * 
	 * @param value
	 *            The new value.
	 */
	public void setValue(Object value)
	{
		this.value = value;
	}

	/**
	 * Convenience function to return value as nested rule array.
	 * 
	 * @return Nested rule set
	 */
	public QueryRule[] getNestedRules()
	{
		return nestedRules;
	}

	@Override
	/**
	 * 
	 */
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder();
		if (this.getOperator().equals(Operator.NESTED))
		{
			strBuilder.append('(');

			for (final QueryRule rule : this.getNestedRules())
			{
				strBuilder.append(rule.toString());
			}
			strBuilder.append(')');
		}
		else
		{
			strBuilder.append(this.getField() == null ? " " : (this.getField() + " "));
			strBuilder.append(this.getOperator()).append(value == null ? " " : " '" + value + "'");
		}
		return strBuilder.toString();
	}

	public static QueryRule eq(String name, Object value)
	{
		return new QueryRule(name, Operator.EQUALS, value);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + Arrays.hashCode(nestedRules);
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		QueryRule other = (QueryRule) obj;
		if (field == null)
		{
			if (other.field != null) return false;
		}
		else if (!field.equals(other.field)) return false;
		if (!Arrays.equals(nestedRules, other.nestedRules)) return false;
		if (operator != other.operator) return false;
		if (value == null)
		{
			if (other.value != null) return false;
		}
		else if (!value.equals(other.value)) return false;
		return true;
	}
}
