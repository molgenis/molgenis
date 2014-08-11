package org.molgenis.elasticsearch.request;

import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;

/**
 * Sets the Query of the SearchRequestBuilder object.
 * 
 * @author erwin
 * 
 */
public class QueryGenerator implements QueryPartGenerator
{
	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query query, EntityMetaData entityMetaData)
	{
		if (hasMrefField(query.getRules(), entityMetaData))
		{
			QueryGeneratorHelper queryGeneratorHelper = new QueryGeneratorHelper(query.getRules(), entityMetaData);
			searchRequestBuilder.setQuery(queryGeneratorHelper.generateQuery());
		}
		else
		{
			searchRequestBuilder.setQuery(QueryBuilders.queryString(
					LuceneQueryStringBuilder.buildQueryString(query.getRules())).defaultOperator(Operator.AND));
		}
	}

	private boolean hasMrefField(List<QueryRule> queryRules, EntityMetaData entityMetaData)
	{
		if (entityMetaData == null || queryRules == null) return false;

		for (QueryRule queryRule : queryRules)
		{
			if (queryRule.getField() != null)
			{
				if (entityMetaData.getAttribute(queryRule.getField()).getDataType().getEnumType().toString()
						.equalsIgnoreCase(MolgenisFieldTypes.MREF.toString()))
				{
					return true;
				}
			}
			if (hasMrefField(queryRule.getNestedRules(), entityMetaData)) return true;
		}
		return false;
	}
}
