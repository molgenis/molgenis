package org.molgenis.data.rest;

import java.util.Map;

import org.molgenis.data.DataConverter;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.rsql.MolgenisRSQL;
import org.molgenis.data.support.QueryImpl;

import cz.jirutka.rsql.parser.RSQLParserException;

/**
 * Creates a Query object from a http request. Used by the RestController method that returns csv.
 * 
 * Parameters:
 * 
 * q: the query
 * 
 * attributes: the attributes to return, if not specified returns all attributes
 * 
 * start: the index of the first row, default 0
 * 
 * num: the number of results to return, default 100, max 100000
 * 
 * 
 * Example: /api/v1/csv/person?q=firstName==Piet&attributes=firstName,lastName&start=10&num=100
 */
public class QueryStringParser
{
	private final EntityMetaData entityMetaData;
	private final MolgenisRSQL molgenisRSQL;

	public QueryStringParser(EntityMetaData entityMetaData, MolgenisRSQL molgenisRSQL)
	{
		this.entityMetaData = entityMetaData;
		this.molgenisRSQL = molgenisRSQL;
	}

	public Query parseQueryString(Map<String, String[]> parameterMap) throws RSQLParserException
	{
		QueryImpl q = new QueryImpl();

		for (Map.Entry<String, String[]> entry : parameterMap.entrySet())
		{
			String paramName = entry.getKey();
			String[] paramValues = entry.getValue();

			if ((paramValues != null) && (paramValues.length > 0) && (paramValues[0] != null))
			{
				if (paramName.equalsIgnoreCase("num"))
				{
					q.pageSize(DataConverter.toInt(paramValues[0]));
				}
				else if (paramName.equalsIgnoreCase("start"))
				{
					q.offset(DataConverter.toInt(paramValues[0]));
				}
				else if (paramName.equalsIgnoreCase("q"))
				{
					Query query = molgenisRSQL.createQuery(paramValues[0], entityMetaData);
					for (QueryRule rule : query.getRules())
					{
						q.addRule(rule);
					}
				}
			}
		}

		return q;
	}
}
