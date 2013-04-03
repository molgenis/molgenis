package org.molgenis.elasticsearch.request;

import static org.molgenis.framework.db.QueryRule.Operator.SORTASC;
import static org.molgenis.framework.db.QueryRule.Operator.SORTDESC;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;

/**
 * Adds Sort to the SearchRequestBuilder object.
 * 
 * @author erwin
 * 
 */
public class SortGenerator extends AbstractQueryRulePartGenerator
{
	private static final List<Operator> SUPPORTED_OPERATORS = Arrays.asList(SORTASC, SORTDESC);

	public SortGenerator()
	{
		super(SUPPORTED_OPERATORS);
	}

	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder)
	{
		for (QueryRule queryRule : queryRules)
		{
			if (queryRule.getValue() == null)
			{
				throw new IllegalArgumentException(
						"Missing value for Sorting, for sorting QueryRule.value should be set to the fieldname where to sort on");
			}

			// Use the sort mapping for sorting
			String sortField = queryRule.getValue().toString() + ".sort";

			if (queryRule.getOperator() == Operator.SORTASC)
			{
				searchRequestBuilder.addSort(sortField, SortOrder.ASC);
			}
			else if (queryRule.getOperator() == Operator.SORTDESC)
			{
				searchRequestBuilder.addSort(sortField, SortOrder.DESC);
			}
		}
	}

}
