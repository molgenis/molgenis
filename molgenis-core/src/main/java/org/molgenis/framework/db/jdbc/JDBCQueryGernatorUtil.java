package org.molgenis.framework.db.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.molgenis.fieldtypes.DecimalField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.LongField;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.TextField;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.model.elements.Field;

/**
 * Contains all (static) function convert queryRule(s) to SQL compatible string
 * 
 * @author
 */
public class JDBCQueryGernatorUtil
{
	/**
	 * Helper method for creating a where clause from QueryRule...rules.
	 * 
	 * @param mapper
	 *            mapper that is used to extract metadata to create the
	 * @param isNested
	 *            wether this whereclause will be nested inside another clause,
	 *            e.g (A AND B) OR C. If nested then the word "where" will not
	 *            be included in the returned string so this method can be used
	 *            recursively.
	 * @param withOffset
	 *            whether this whereclause should be calculated including offset
	 *            and limit
	 * @param rules
	 *            query rules to be translated into sql where clause.
	 * @return sql where clause. FIXME: remove the 'withOffset' part?
	 * @throws DatabaseException
	 */
	public static String createWhereSql(Mapper<?> mapper, boolean isNested, boolean withOffset, QueryRule... rules)
			throws DatabaseException
	{
		StringBuilder where_clause = new StringBuilder("");
		QueryRule previousRule = new QueryRule(Operator.AND);
		if (rules != null)
		{
			for (QueryRule r : rules)
			{
				// logger.debug(r);
				// skip OR and AND operators
				if (r.getOperator().equals(Operator.OR) || r.getOperator().equals(Operator.AND))
				{
					previousRule = r;
				}
				else
				{
					QueryRule rule = new QueryRule(r); // copy because of side
					// effects
					// logger.debug(rule);

					// String tablePrefix = "";
					if (mapper != null)
					{
						rule.setField(mapper.getTableFieldName(rule.getField()));
					}

					if (rule.getOperator() == Operator.LAST || rule.getOperator() == Operator.LIMIT
							|| rule.getOperator() == Operator.OFFSET || rule.getOperator() == Operator.SORTASC
							|| rule.getOperator() == Operator.SORTDESC)
					{

					}
					else if (rule.getOperator() == QueryRule.Operator.SEARCH)
					{
						// naive implementation, should use hibernate search
						// when it comes
						// available!
						List<QueryRule> searchRules = new ArrayList<QueryRule>();

						try
						{
							boolean addOr = false;

							// try create big OR filter for all fields and all
							// search elements
							// todo: enable string term concat using quotes
							for (String term : rule.getValue().toString().split(" "))
							{
								List<QueryRule> termRules = new ArrayList<QueryRule>();

								// create different query rule depending on type
								List<Field> fields = mapper.getDatabase().getMetaData()
										.getEntity(mapper.create().getClass().getSimpleName()).getAllFields();
								for (Field f : fields)
								{
									if (f.getType() instanceof StringField || f.getType() instanceof TextField)
									{
										termRules.add(new QueryRule(f.getName(), Operator.LIKE, term.trim()));
										termRules.add(new QueryRule(Operator.OR));
									}
								}

								// add as big X or Y or Z subquery to our rules
								searchRules.add(new QueryRule(termRules));
								if (addOr) searchRules.add(new QueryRule(Operator.OR));
								addOr = true;
							}
						}
						catch (Exception e)
						{
							throw new DatabaseException(e);
						}

						// add to rules
						where_clause.append("(");
						where_clause.append(createWhereSql(mapper, true, false,
								searchRules.toArray(new QueryRule[searchRules.size()])));
						where_clause.append(")");

					}
					else if (rule.getOperator() == QueryRule.Operator.NESTED
							|| (rule.getOperator() == QueryRule.Operator.NOT && rule.getNestedRules() != null && rule
									.getNestedRules().length > 0))
					{
						QueryRule[] nestedrules = rule.getNestedRules();
						if (nestedrules.length > 0)
						{
							if (where_clause.length() > 0)
							{
								if (previousRule != null && Operator.OR.equals(previousRule.getOperator()))
								{
									where_clause.append(" OR ");
								}
								else
								{
									where_clause.append(" AND ");
								}
							}

							if (rule.getOperator() == QueryRule.Operator.NOT)
							{
								where_clause.append("NOT");
							}
							where_clause.append("(");
							where_clause.append(createWhereSql(mapper, true, false, nestedrules));
							where_clause.append(")");
						}
					}
					// experimental: subqery
					else if (rule.getOperator() == QueryRule.Operator.IN_SUBQUERY)
					{
						if (where_clause.length() > 0)
						{
							if (previousRule != null && Operator.OR.equals(previousRule.getOperator()))
							{
								where_clause.append(" OR ");
							}
							else
							{
								where_clause.append(" AND ");
							}
						}
						where_clause.append(rule.getField() + " IN(" + rule.getValue() + ")");
					}
					else if (rule.getOperator() == QueryRule.Operator.IN)
					{
						// only add if nonempty condition???
						if (rule.getValue() == null
								|| (rule.getValue() instanceof List<?> && ((List<?>) rule.getValue()).size() == 0)
								|| (rule.getValue() instanceof Object[] && ((Object[]) rule.getValue()).length == 0)) throw new DatabaseException(
								"empty 'in' clause for rule " + rule);
						{
							if (where_clause.length() > 0)
							{
								if (previousRule != null && Operator.OR.equals(previousRule.getOperator()))
								{
									where_clause.append(" OR ");
								}
								else
								{
									where_clause.append(" AND ");
								}
							}

							// where_clause.append(tablePrefix + rule.getField()
							// +
							// " IN(");
							where_clause.append(rule.getField() + " IN(");

							Object[] values = new Object[0];
							if (rule.getValue() instanceof List<?>)
							{
								values = ((List<?>) rule.getValue()).toArray();
							}
							else
							{
								values = (Object[]) rule.getValue();
							}

							for (int i = 0; i < values.length; i++)
							{
								if (i > 0) where_clause.append(",");
								if (mapper != null && omitQuotes(mapper.getFieldType(rule.getField())))
								{
									// where_clause.append(values[i]
									// .toString());
									where_clause.append("" + escapeSql(values[i]) + "");
								}
								else
								{
									where_clause.append("'" + escapeSql(values[i]) + "'");
								}
							}
							where_clause.append(") ");
						}
					}
					else
					{
						// where clause
						String operator = "";
						switch (rule.getOperator())
						{
							case EQUALS:
								operator = "=";
								break;
							case JOIN:
								operator = "=";
								break;
							case NOT:
								operator = "!=";
								break;
							case LIKE:
								operator = "LIKE";
								break;
							case LESS:
								operator = "<";
								break;
							case GREATER:
								operator = ">";
								break;
							case LESS_EQUAL:
								operator = "<=";
								break;
							case GREATER_EQUAL:
								operator = ">=";
								break;
						}
						// if (rule.getField() != "" && operator != "" &&
						// rule.getValue() != "")
						// {
						if (where_clause.length() > 0)
						{
							if (previousRule != null && Operator.OR.equals(previousRule.getOperator()))
							{
								where_clause.append(" OR ");
							}
							else
							{
								where_clause.append(" AND ");
							}
						}
						if (Boolean.TRUE.equals(rule.getValue())) rule.setValue("1");
						if (Boolean.FALSE.equals(rule.getValue())) rule.setValue("0");
						Object value = rule.getValue() == null ? "NULL" : escapeSql(rule.getValue());

						if (!value.equals("NULL") && rule.getOperator() == Operator.LIKE
								&& (mapper == null || !omitQuotes(mapper.getFieldType(rule.getField()))))
						{
							if (!value.toString().trim().startsWith("%") && !value.toString().trim().endsWith("%"))
							{
								value = "%" + value + "%";
							}
						}

						// if
						// (omitQuotes(columnInfoMap.get(rule.getField()).getType()))
						// where_clause.append(tablePrefix + rule.getField() +
						// " " +
						// operator + " " + value + "");
						// else
						// where_clause.append(tablePrefix + rule.getField() +
						// " " +
						// operator + " '" + value + "'");
						if (rule.getOperator().equals(Operator.JOIN))
						{
							where_clause.append(rule.getField() + " " + operator + " " + value + "");
						}
						else
						{
							if ("NULL".equals(value) && operator.equals("="))
							{
								where_clause.append(rule.getField() + " IS NULL");
							}
							else if ("NULL".equals(value) && operator.equals("!="))
							{
								where_clause.append(rule.getField() + " IS NOT NULL");
							}
							else
							{
								where_clause.append(rule.getField() + " " + operator + " '" + value + "'");
							}
						}
					}
					previousRule = null;
				}
			}
		}
		String result = where_clause.toString();
		if (!isNested && where_clause.length() > 0) result = " WHERE " + result;
		return result + createSortSql(mapper, rules) + createLimitSql(withOffset, rules);
	}

	/** Helper method for creating a sort clause */
	private static String createSortSql(Mapper<?> mapper, QueryRule... rules)
	{
		return createSortSql(mapper, false, rules);
	}

	private static boolean omitQuotes(FieldType t)
	{
		return t instanceof LongField || t instanceof IntField || t instanceof DecimalField;

		// t.equals(Type.LONG) || t.equals(Type.INT) || t.equals(Type.DECIMAL);
		// return t instanceof LongField || t instanceof IntField|| t instanceof
		// DecimalField;

	}

	/**
	 * Helper method for creating a sort clause
	 * 
	 * @param mapper
	 *            mapper that is used to extract metadata to create the
	 * @param reverseSorting
	 *            to reverese sorting order. This is used when trying to find
	 *            the "last records" in a sorted list by instead finding the
	 *            "first records" in the reversly ordered list.
	 * @param rules
	 *            query rules to be translated into sql order by clause.
	 * @return sql with sort clause
	 */
	public static String createSortSql(Mapper<?> mapper, boolean reverseSorting, QueryRule rules[])
	{
		// copy parameter into local temp so we can change it
		StringBuilder sort_clauseBuilder = new StringBuilder();
		if (rules != null)
		{
			Boolean revSort = reverseSorting;
			for (QueryRule rule : rules)
			{
				if (rule.getOperator() == Operator.LAST)
				{
					revSort = !revSort;
					break;
				}
			}

			for (QueryRule r : rules)
			{
				QueryRule rule = new QueryRule(r); // copy because of
													// sideeffects

				// limit clause
				if ((rule.getOperator() == Operator.SORTASC && !revSort)
						|| (revSort && rule.getOperator() == Operator.SORTDESC))
				{
					if (mapper != null) rule.setValue(mapper.getTableFieldName(rule.getValue().toString()));
					sort_clauseBuilder.append(rule.getValue().toString()).append(" ASC,");
				}
				else if ((rule.getOperator() == QueryRule.Operator.SORTDESC && !revSort)
						|| (revSort && rule.getOperator() == Operator.SORTASC))
				{
					if (mapper != null) rule.setValue(mapper.getTableFieldName(rule.getValue().toString()));
					sort_clauseBuilder.append(rule.getValue().toString()).append(" DESC,");
				}
			}
		}
		if (sort_clauseBuilder.length() > 0) return " ORDER BY "
				+ sort_clauseBuilder.substring(0, sort_clauseBuilder.lastIndexOf(","));
		return sort_clauseBuilder.toString();
	}

	/**
	 * Helper method for creating a limit clause
	 * 
	 * @param withOffset
	 *            Indicate whether offset is to be used. If false the limit
	 *            clause is kept empty.
	 * @param rules
	 *            query rules to be translated into sql order by clause.
	 * @return sql for limit,offset
	 */
	public static String createLimitSql(boolean withOffset, QueryRule[] rules)
	{
		String limit_clause = "";
		String offset_clause = "";
		if (rules != null)
		{
			for (QueryRule rule : rules)
			{
				// limit clause
				if (rule.getOperator() == QueryRule.Operator.LIMIT)
				{
					limit_clause = " LIMIT " + rule.getValue();
				}
				else if (rule.getOperator() == QueryRule.Operator.OFFSET)
				{
					offset_clause = " OFFSET " + rule.getValue();
				}
			}
		}
		if (withOffset || offset_clause.equals("")) return limit_clause + offset_clause;
		return "";
	}

	/**
	 * Helper method for creating an escaped sql string for a value.
	 * <p>
	 * This can be used by createXXXsql methods to prevend sql-injection in data
	 * values.
	 * 
	 * @param value
	 *            to be escaped
	 */
	public static String escapeSql(Object value)
	{
		if (value != null && value.toString().contains("Specific diagnosis Mutation"))
		{
			System.out.println("trouble");
		}
		if (value != null) return StringEscapeUtils.escapeSql(value.toString());
		return null;
		// return sql.toString().replace("'", "''");
	}
}
