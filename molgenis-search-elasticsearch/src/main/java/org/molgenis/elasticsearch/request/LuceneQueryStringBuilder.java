package org.molgenis.elasticsearch.request;

import static org.molgenis.data.QueryRule.Operator.AND;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.QueryRule.Operator.GREATER;
import static org.molgenis.data.QueryRule.Operator.GREATER_EQUAL;
import static org.molgenis.data.QueryRule.Operator.LESS;
import static org.molgenis.data.QueryRule.Operator.LESS_EQUAL;
import static org.molgenis.data.QueryRule.Operator.LIKE;
import static org.molgenis.data.QueryRule.Operator.NOT;
import static org.molgenis.data.QueryRule.Operator.OR;
import static org.molgenis.data.QueryRule.Operator.SEARCH;

import java.util.List;
import java.util.regex.Pattern;

import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;

/**
 * Builds a Lucene query from molgenis QueryRules
 * 
 * @author erwin
 * 
 */
public class LuceneQueryStringBuilder
{
	// The characters that need to be escaped are: && || + - ! ( ) { } [ ] " ~ *
	// / \ ? :
	// ^ was not included because it needs to be used for query boosting
	private static final String LUCENE_ESCAPE_CHARS_VALUE = "[-&+!\\|\\(\\){}\\[\\]\"\\~\\*\\?:\\\\\\/]";
	private static final Pattern LUCENE_PATTERN_VALUE = Pattern.compile(LUCENE_ESCAPE_CHARS_VALUE);

	// Field also needs escaping of whitespace (TODO test each char if it's
	// really needed to be escaped)
	private static final String LUCENE_ESCAPE_CHARS_FIELD = "[-&+!\\|\\(\\){}\\[\\]\\^\"\\~\\*\\?:\\s\\\\]";
	private static final Pattern LUCENE_PATTERN_FIELD = Pattern.compile(LUCENE_ESCAPE_CHARS_FIELD);

	private static final String REPLACEMENT_STRING = "\\\\$0";

	/**
	 * Builds a lucene query string
	 * 
	 * @param queryRules
	 * @return the lucene query
	 */
	public static String buildQueryString(List<QueryRule> queryRules)
	{
		if (queryRules.isEmpty())
		{
			return "*:*";
		}

		StringBuilder sb = new StringBuilder();
		QueryRule previousRule = null;

		for (QueryRule queryRule : queryRules)
		{
			if (queryRule.getOperator() == OR || queryRule.getOperator() == AND)
			{
				previousRule = queryRule;
			}
			else if (queryRule.getOperator() == EQUALS || queryRule.getOperator() == NOT
					|| queryRule.getOperator() == LIKE || queryRule.getOperator() == LESS
					|| queryRule.getOperator() == LESS_EQUAL || queryRule.getOperator() == GREATER
					|| queryRule.getOperator() == GREATER_EQUAL || queryRule.getOperator() == SEARCH)
			{
				if (previousRule != null)
				{
					sb.append(previousRule);
				}

				if (queryRule.getOperator() == NOT)
				{
					sb.append("-");
				}

				Object value = getValue(queryRule);
				if (((value == null) || (value.equals(""))) && (queryRule.getOperator() == Operator.EQUALS))
				{
					sb.append("_missing_:" + queryRule.getField());
				}
				else
				{

					if (queryRule.getField() != null)
					{
						sb.append(escapeField(queryRule.getField())).append(":");
					}

					switch (queryRule.getOperator())
					{
						case EQUALS:
						case NOT:
						{
							sb.append(value);
							break;
						}

						case LIKE:
							sb.append("*").append(value).append("*");
							break;

						case LESS:
							sb.append("{* TO ").append(value).append("}");
							break;

						case LESS_EQUAL:
							sb.append("[* TO ").append(value).append("]");
							break;

						case GREATER:
							sb.append("{").append(value).append(" TO *}");
							break;

						case GREATER_EQUAL:
							sb.append("[").append(value).append(" TO *]");
							break;

						case SEARCH:
							sb.append(value);
							break;

						default:
							throw new IllegalArgumentException("Operator [" + queryRule.getOperator()
									+ "] not supported");
					}
				}

				previousRule = null;
			}
		}

		return sb.toString();
	}

	// Get the value from the QueryRule for use with lucene
	private static Object getValue(QueryRule queryRule)
	{
		Object value = null;
		if (queryRule.getValue() != null)
		{
			value = queryRule.getValue() instanceof String ? escapeValue((String) queryRule.getValue()) : queryRule
					.getValue();
		}

		return value;
	}

	/**
	 * Escape a value for use with lucene
	 * 
	 * @param value
	 * @return the escaped value
	 */
	public static String escapeValue(String value)
	{
		return LUCENE_PATTERN_VALUE.matcher(value).replaceAll(REPLACEMENT_STRING);
	}

	/**
	 * Escape a fieldname for use with lucene
	 * 
	 * @param name
	 * @return the escaped fieldname
	 */
	public static String escapeField(String name)
	{
		return LUCENE_PATTERN_FIELD.matcher(name).replaceAll(REPLACEMENT_STRING);
	}

}
