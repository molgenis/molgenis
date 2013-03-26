package org.molgenis.elasticsearch.request;

import static org.molgenis.framework.db.QueryRule.Operator.LIMIT;
import static org.molgenis.framework.db.QueryRule.Operator.OFFSET;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;

/**
 * Sets the 'from' and 'size' fields of the SearchRequestBuilder object.
 * 
 * @author erwin
 * 
 */
public class LimitOffsetGenerator extends AbstractQueryRulePartGenerator
{
	private static final List<Operator> SUPPORTED_OPERATORS = Arrays.asList(LIMIT, OFFSET);

	public LimitOffsetGenerator()
	{
		super(SUPPORTED_OPERATORS);
	}

	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder)
	{
		for (QueryRule queryRule : queryRules)
		{
			if (queryRule.getOperator() == OFFSET)
			{
				if (queryRule.getValue() == null)
				{
					throw new IllegalArgumentException("Missing value for offset queryrule value");
				}

				if (!(queryRule.getValue() instanceof Number))
				{
					throw new IllegalArgumentException("QueryRule value for offset must be of type integer");
				}

				searchRequestBuilder.setFrom(((Number) queryRule.getValue()).intValue());
			}
			else if (queryRule.getOperator() == LIMIT)
			{
				if (queryRule.getValue() == null)
				{
					throw new IllegalArgumentException("Missing value for limit queryrule value");
				}

				if (!(queryRule.getValue() instanceof Number))
				{
					throw new IllegalArgumentException("QueryRule value for limit must be a number");
				}

				searchRequestBuilder.setSize(((Number) queryRule.getValue()).intValue());
			}
		}
	}
}
