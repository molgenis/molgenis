package org.molgenis.framework.tupletable.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.tupletable.AbstractFilterableTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.EntityTuple;
import org.molgenis.util.tuple.Tuple;

/**
 * Wrap an Entity (that is stored in a database) into a TupleTable
 */
public class EntityTable extends AbstractFilterableTupleTable
{
	// class to query
	private Class<? extends Entity> entityClass;

	// copy of the fields from meta database
	private List<Field> columns;

	/**
	 * Constructor
	 * 
	 * @param database
	 *            containing the entity
	 * @param entityClass
	 *            class of entities to query
	 */
	public EntityTable(final Database database, final Class<? extends Entity> entityClass)
	{
		super();

		this.setDb(database);

		if (entityClass == null)
		{
			throw new NullPointerException("entityClass can't be null");
		}

		this.entityClass = entityClass;
	}

	@Override
	public List<Field> getAllColumns() throws TableException
	{
		if (columns != null) return columns;

		try
		{
			columns = getDb().getMetaData().getEntity(entityClass.getSimpleName()).getAllFields();
			return columns;
		}
		catch (Exception e)
		{
			throw new TableException(e);
		}
	}

	@Override
	public List<Tuple> getRows()
	{
		try
		{
			Query<? extends Entity> q = getDb().query(entityClass);
			if (this.getLimit() > 0)
			{
				q.limit(this.getLimit());
			}
			if (this.getOffset() > 0)
			{
				q.offset(this.getOffset());
			}
			if (getFilters().size() > 0)
			{
				q.addRules(getFilters().toArray(new QueryRule[getFilters().size()]));
			}
			List<? extends Entity> entities = q.find();

			List<Tuple> result = new ArrayList<Tuple>();
			for (Entity entity : entities)
			{
				result.add(new EntityTuple(entity));
			}
			return result;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Iterator<Tuple> iterator()
	{
		// should be optimized
		return this.getRows().iterator();
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
			if (getFilters().size() > 0) return getDb().count(entityClass,
					getFilters().toArray(new QueryRule[getFilters().size()]));
			else
				return getDb().count(entityClass);
		}
		catch (DatabaseException e)
		{
			throw new TableException(e);
		}
	}

	/**
	 * very bad: bypasses all security and connection management
	 */
	private Database db;

	public void setDb(Database db)
	{
		if (db == null) throw new NullPointerException("database cannot be null in setDb(db)");
		this.db = db;
	}

	public Database getDb()
	{
		// try
		// {
		// db = DatabaseFactory.create();
		// }
		// catch (DatabaseException e)
		// {
		// throw new RuntimeException(e);
		// }
		return this.db;
	}
}
