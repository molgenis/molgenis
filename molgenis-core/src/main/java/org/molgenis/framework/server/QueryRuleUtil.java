package org.molgenis.framework.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;

public class QueryRuleUtil
{
	public static String toRESTstring(QueryRule... rules)
	{
		return toRESTstring(Arrays.asList(rules));
	}

	public static String toRESTstring(List<QueryRule> rules)
	{
		StringBuffer filters = new StringBuffer();

		QueryRule previousRule = null;
		for (QueryRule r : rules)
		{
			if (rules.indexOf(r) > 0)
			{
				if (QueryRule.Operator.OR.equals(previousRule.getOperator()))
				{
					filters.append("\\|");
				}
				else
				{
					filters.append("\\&");
				}

			}
			if (r.getField() != null) filters.append(r.getField());
			filters.append(r.getOperator());

			if (r.getValue() instanceof String) filters.append("'" + r.getValue() + "'");
			else
				filters.append(r.getValue());

			previousRule = r;
		}

		// escape
		return filters.toString().replace("=", "\\=");
	}

	public static List<QueryRule> fromRESTstring(String RESTstring)
	{
		List<QueryRule> rules = new ArrayList<QueryRule>();

		if (RESTstring == null || RESTstring.equals("")) return null;

		String[] ruleStrings = RESTstring.replace("\\=", "=").split("&");

		// incomplete impl
		for (String rule : ruleStrings)
		{
			String field = null;
			Operator operator = null;
			String value = null;
			// need the longest operator string match
			int operatorLength = 0;
			for (Operator o : Operator.values())
			{
				// value should not be splittable
				// need escaping scheme with ""
				int index = rule.indexOf(o.toString());
				if (index > -1 && o.toString().length() > operatorLength)
				{
					operatorLength = o.toString().length();

					field = rule.substring(0, index);
					operator = o;
					value = rule.substring(index + operatorLength, rule.length());

					// System.out.println("operator:" + o + ", value:" + value);

					// check if value is escaped string
					if (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'')
					{
						value = value.substring(1, value.length() - 1);
					}
				}
			}
			if (field == null || field.equals("")) rules.add(new QueryRule(operator, value));
			else
				rules.add(new QueryRule(field, operator, value));
		}

		return rules;
	}

}
