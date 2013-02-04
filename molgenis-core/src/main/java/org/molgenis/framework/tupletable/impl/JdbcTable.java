package org.molgenis.framework.tupletable.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.tupletable.AbstractFilterableTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.Tuple;

public class JdbcTable extends AbstractFilterableTupleTable
{
	private Database db;
	private List<Tuple> rs;
	private List<Field> columns;
	private final String query;
	private final String countQuery;
	private boolean loaded = false;

	public JdbcTable(Database db, String query, List<QueryRule> rules) throws TableException
	{
		super();
		this.db = db;
		this.query = query;
		this.setFilters(rules);

		final String fromExpression = StringUtils.substringBetween(query, "SELECT", "FROM");
		this.countQuery = StringUtils.replace(query, fromExpression, " COUNT(*) ");
	}

	public JdbcTable(Database db, String query) throws TableException
	{
		this(db, query, new ArrayList<QueryRule>());
	}

	private void load() throws TableException
	{
		if (!loaded)
		{
			loaded = true;
			try
			{
				rs = db.sql(query, getFilters().toArray(new QueryRule[0]));
				columns = loadColumns();
			}
			catch (Exception e)
			{
				throw new TableException(e);
			}
		}
	}

	@Override
	public List<Field> getAllColumns() throws TableException
	{
		load();
		return columns;
	}

	private List<Field> loadColumns() throws TableException
	{
		load();
		if (rs.size() > 0)
		{
			List<Field> columns = new ArrayList<Field>();
			for (String colName : rs.get(0).getColNames())
				columns.add(new Field(colName));
			return columns;
		}
		return new ArrayList<Field>();
	}

	/**
	 * Don't forget to call close after done with Iterator
	 */
	@Override
	public Iterator<Tuple> iterator()
	{
		try
		{
			load();
			return rs.iterator();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws TableException
	{
	}

	@Override
	public int getCount() throws TableException
	{
		try
		{
			final List<Tuple> countSet = db.sql(countQuery, getFilters().toArray(new QueryRule[0]));
			int rowCount = 0;
			if (countSet.size() > 0)
			{
				final Number count = (Number) countSet.get(0).getInt(1);
				rowCount = count.intValue();
			}
			return rowCount;
		}
		catch (Exception ex)
		{
			throw new TableException(ex);
		}
	}

	public void setDb(Database db)
	{
		if (db == null) throw new NullPointerException("database cannot be null in setDb(db)");
		this.db = db;
	}

	public Database getDb()
	{
		return this.db;
	}
}
