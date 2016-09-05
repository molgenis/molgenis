package org.molgenis.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * With this class an equation model can be described for a database-field (eg a column).
 */
@XmlRootElement
public class QueryRule
{
	/**
	 * The operator being applied to the field and value
	 */
	@XmlElement
	protected Operator operator;

	/**
	 * The field-name (eq column-name) in the database
	 */
	@XmlElement
	protected String field = null;

	/**
	 * The value to compare entries of the field in the database with
	 */
	@XmlElement
	protected Object value = null;

	protected List<QueryRule> nestedRules;

	public QueryRule()
	{

	}

	public QueryRule(List<QueryRule> nestedRules)
	{
		this.nestedRules = nestedRules;
		operator = Operator.NESTED;
	}

	public QueryRule(QueryRule copy)
	{
		operator = copy.operator;
		field = copy.field;
		value = copy.value;
	}

	/**
	 * Different types of rules that can be applied.
	 */
	public enum Operator
	{
		/**
		 * 'field' like 'value', searches all fields if field is not defined
		 */
		SEARCH("search"),

		/**
		 * 'field' equal to 'value'
		 * <p>
		 * When 'field type' is 'Mref' its results are derived from the 'Contains' behavior. <br>
		 * Examples: <br>
		 * 1. ref1 OR ref2 can result in:
		 * <ul>
		 * <li>re1</li>
		 * <li>ref1, ref2</li>
		 * <li>ref1, ref2, ref3;</li>
		 * <li>ref2</li>
		 * <li>ref2, ref3</li>
		 * </ul>
		 * 2. ref1 AND ref2 can result in:
		 * <ul>
		 * <li>ref1, ref2</li>
		 * <li>ref1, ref2, ref3</li>
		 * </ul>
		 */
		EQUALS("="),

		/**
		 * 'field' in 'value' (value being a list).
		 */
		IN("IN"),

		/**
		 * 'field' less-than 'value'
		 */
		LESS("<"),

		/**
		 * 'field' equal-or-less-than 'value'
		 */
		LESS_EQUAL("<="),

		/**
		 * 'field' greater-than 'value'
		 */
		GREATER(">"),

		/**
		 * 'field' equal-or-greater-than 'value'
		 */
		GREATER_EQUAL(">="),

		/**
		 * 'field' equal-or-greater-than 'from value' and equal-or-less-than 'to value' (value being a list with 'from
		 * value' as first element and 'to value' as second element
		 */
		RANGE("RANGE"),

		/**
		 * 'field' like 'value' (works like equals with wildcard before and after value)
		 */
		LIKE("LIKE"),

		/**
		 * 'field' not-equal to 'value'
		 */
		NOT("!="),

		/**
		 * AND operation
		 */
		AND("AND"),

		/**
		 * OR operation
		 */
		OR("OR"),

		/**
		 * indicates that 'value' is a nested array of QueryRule. The parameter 'field' is ommitted.
		 */
		NESTED(""),

		/**
		 * Boolean query
		 */
		SHOULD("SHOULD"),

		/**
		 * Disjunction max query
		 */
		DIS_MAX("DIS_MAX"),

		/**
		 * Fuzzy match operator
		 */
		FUZZY_MATCH("FUZZY_MATCH"),

		/**
		 * Fuzzy match operator
		 */
		FUZZY_MATCH_NGRAM("FUZZY_MATCH_NGRAM");

		private String label;

		/**
		 * Translate String label of the operator to Operator.
		 *
		 * @param label of the operator
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
	 * With this constructor the field, operator and value are set in one go, so there is no need for additional
	 * statements.
	 *
	 * @param field    The field-name.
	 * @param operator The operator to use for comparing entries in the field with the value.
	 * @param value    The value.
	 */
	public QueryRule(String field, Operator operator, Object value)
	{
		if (operator == Operator.AND || operator == Operator.OR)
		{
			throw new IllegalArgumentException(
					"QueryRule(): Operator." + operator + " cannot be used with two arguments");
		}
		this.field = field;
		this.operator = operator;
		setValue(value);
	}

	/**
	 * Specific constructor for rules that do not apply to a field such as LIMIT and OFFSET.
	 *
	 * @param operator
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public QueryRule(Operator operator, Object value)
	{
		if (operator == Operator.SEARCH)
		{
			this.operator = operator;
			setValue(value);
		}
		else if (Operator.NESTED.equals(operator))
		{
			boolean okay = true;
			if (value instanceof List)
			{
				for (Object o : (List<?>) value)
				{
					if (!(o instanceof QueryRule)) okay = false;
				}
			}
			else
			{
				okay = false;
			}
			if (!okay) throw new IllegalArgumentException("QueryRule(NESTED, value): value should be List<QueryRule>");
			this.nestedRules = (List<QueryRule>) value;
			this.operator = operator;
		}
		else
		{
			throw new IllegalArgumentException(
					"QueryRule(): Operator." + operator + " cannot be used with one argument");
		}
	}

	public QueryRule(Operator operator, QueryRule nestedRules)
	{
		if (operator == Operator.NOT)
		{
			this.operator = operator;
			this.nestedRules = Arrays.asList(nestedRules);
		}
		else
		{
			throw new IllegalArgumentException(
					"QueryRule(): Operator." + operator + " cannot be used with one argument");
		}
	}

	/**
	 * Specific constructor for rules that don't have a value or field such as LAST
	 */
	public QueryRule(Operator operator)
	{
		if (operator == Operator.AND || operator == Operator.OR || operator == Operator.NOT)
		{
			this.operator = operator;
		}
		else
		{
			throw new IllegalArgumentException(
					"QueryRule(): Operator '" + operator + "' cannot be used without arguments");
		}
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
	 * Sets a new field-name for this rule.
	 *
	 * @param field The new field-name.
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
	 * @param operator The new operator.
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
	 * @param value The new value.
	 */
	public void setValue(Object value)
	{
		if (value instanceof Iterable<?>)
		{
			this.value = stream(((Iterable<?>) value).spliterator(), false).map(this::toValue).collect(toList());
		}
		else
		{
			this.value = toValue(value);
		}
	}

	private Object toValue(Object value)
	{
		if (value instanceof Entity)
		{
			return ((Entity) value).getIdValue();
		}
		return value;
	}

	/**
	 * Convenience function to return value as nested rule array.
	 *
	 * @return Nested rule set
	 */
	public List<QueryRule> getNestedRules()
	{
		if (nestedRules == null)
		{
			return Collections.emptyList();
		}

		return nestedRules;
	}

	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder();
		if (field != null)
		{
			strBuilder.append('\'').append(field).append('\'');
		}
		if (operator != null && operator != Operator.NESTED)
		{
			if (strBuilder.length() > 0)
			{
				strBuilder.append(' ');
			}
			strBuilder.append(operator);
		}
		if (operator != Operator.AND && operator != Operator.OR && operator != Operator.NOT
				&& operator != Operator.NESTED && operator != Operator.DIS_MAX && operator != Operator.SHOULD)
		{
			if (strBuilder.length() > 0)
			{
				strBuilder.append(' ');
			}
			if (operator != Operator.IN)
			{
				strBuilder.append('\'').append(value).append('\'');
			}
			else
			{
				strBuilder.append(value);
			}
		}
		if (nestedRules != null && !nestedRules.isEmpty())
		{
			if (strBuilder.length() > 0)
			{
				strBuilder.append(' ');
			}
			strBuilder.append('(');

			for (Iterator<QueryRule> it = nestedRules.iterator(); it.hasNext(); )
			{
				strBuilder.append(it.next());
				if (it.hasNext())
				{
					strBuilder.append(", ");
				}
			}
			strBuilder.append(')');
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
		int result = operator != null ? operator.hashCode() : 0;
		result = 31 * result + (field != null ? field.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		result = 31 * result + (nestedRules != null ? nestedRules.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		QueryRule queryRule = (QueryRule) o;

		if (field != null ? !field.equals(queryRule.field) : queryRule.field != null) return false;
		if (nestedRules != null ? !nestedRules.equals(queryRule.nestedRules) : queryRule.nestedRules != null)
			return false;
		if (operator != queryRule.operator) return false;
		if (value != null ? !value.equals(queryRule.value) : queryRule.value != null) return false;

		return true;
	}
}