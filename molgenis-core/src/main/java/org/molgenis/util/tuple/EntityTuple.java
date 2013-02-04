package org.molgenis.util.tuple;

import java.util.Vector;

import org.molgenis.util.Entity;

/**
 * Tuple backed by an {@link org.molgenis.util.Entity}
 */
public class EntityTuple extends AbstractTuple
{
	private static final long serialVersionUID = 1L;

	private final Entity entity;
	private transient Vector<String> cachedFields;

	public EntityTuple(Entity entity)
	{
		if (entity == null) throw new IllegalArgumentException("entity is null");
		this.entity = entity;
	}

	@Override
	public int getNrCols()
	{
		return getFields().size();
	}

	@Override
	public Iterable<String> getColNames()
	{
		return getFields();
	}

	@Override
	public Object get(String colName)
	{
		return entity.get(colName);
	}

	@Override
	public Object get(int col)
	{
		String colName = getFields().get(col);
		return get(colName);
	}

	private Vector<String> getFields()
	{
		if (cachedFields == null) cachedFields = entity.getFields();
		return cachedFields;
	}
}
