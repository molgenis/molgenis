package org.molgenis.data.rest;

import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rsql.MolgenisRSQL;
import org.molgenis.data.support.QueryImpl;

import java.util.Map;

/**
 * Creates a Query object from a http request. Used by the RestController method that returns csv.
 * <p>
 * Parameters:
 * <p>
 * q: the query
 * <p>
 * attributes: the attributes to return, if not specified returns all attributes
 * <p>
 * start: the index of the first row, default 0
 * <p>
 * num: the number of results to return, default 100, max 100000
 * <p>
 * <p>
 * Example: /api/v1/csv/sys_sec_Token?q=description==Piet&attributes=description,creationDate&start=10&num=100
 */
public class QueryStringParser
{
	private final EntityType entityType;
	private final MolgenisRSQL molgenisRSQL;

	public QueryStringParser(EntityType entityType, MolgenisRSQL molgenisRSQL)
	{
		this.entityType = entityType;
		this.molgenisRSQL = molgenisRSQL;
	}

	public Query<Entity> parseQueryString(Map<String, String[]> parameterMap)
	{
		QueryImpl<Entity> q = new QueryImpl<>();

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
					Query<Entity> query = molgenisRSQL.createQuery(paramValues[0], entityType);
					for (QueryRule rule : query.getRules())
					{
						q.addRule(rule);
					}
				}
			}
		}

		return q;
	}

	public Query<Entity> createQuery(Integer num, Integer start, String queryString)
	{
		QueryImpl<Entity> query = new QueryImpl<>();

		if (num != null)
		{
			query.pageSize(num);
		}
		if (start != null)
		{
			query.offset(start);
		}
		if (queryString != null)
		{
			Query<Entity> q = molgenisRSQL.createQuery(queryString, entityType);
			for (QueryRule rule : q.getRules())
			{
				query.addRule(rule);
			}
		}

		return query;
	}
}
