package org.molgenis.framework.db;

import org.apache.commons.lang.StringUtils;
import org.molgenis.util.Entity;

public class SubQueryRule extends QueryRule
{
	private final Class<?> subQueryResultClass;
	private final Class<?> subQueryFromClass;
	private final Operator subQueryOperator;
	private final String subQueryField;

	public SubQueryRule(String fieldName, Operator subQueryOperator, String subQueryField,
			Class<?> subQueryResultClass, Class<? extends Entity> subQueryFromClass, QueryRule... subQueryRules)
	{
		super(fieldName, Operator.SUBQUERY, subQueryRules);
		this.subQueryResultClass = subQueryResultClass;
		this.subQueryFromClass = subQueryFromClass;
		this.subQueryOperator = subQueryOperator;
		this.subQueryField = subQueryField;
	}

	public Class<?> getSubQueryResultClass()
	{
		return subQueryResultClass;
	}

	public Class<?> getSubQueryFromClass()
	{
		return subQueryFromClass;
	}

	public Operator getSubQueryOperator()
	{
		return subQueryOperator;
	}

	public String getSubQueryField()
	{
		return subQueryField;
	}

	public String getSubQueryAttributeJpa()
	{
		if (!StringUtils.isEmpty(subQueryField))
		{
			return subQueryField.substring(0, 1).toLowerCase() + subQueryField.substring(1);
		}
		return subQueryField;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((subQueryField == null) ? 0 : subQueryField.hashCode());
		result = prime * result + ((subQueryOperator == null) ? 0 : subQueryOperator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		SubQueryRule other = (SubQueryRule) obj;
		if (subQueryField == null)
		{
			if (other.subQueryField != null) return false;
		}
		else if (!subQueryField.equals(other.subQueryField)) return false;
		if (subQueryOperator != other.subQueryOperator) return false;
		return true;
	}
}
