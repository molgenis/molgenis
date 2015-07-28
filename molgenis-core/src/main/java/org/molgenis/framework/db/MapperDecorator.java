package org.molgenis.framework.db;

import java.text.ParseException;
import java.util.List;

import org.molgenis.fieldtypes.FieldType;
import org.molgenis.util.Entity;

public class MapperDecorator<E extends Entity> implements Mapper<E>
{
	private final Mapper<E> mapper;

	public MapperDecorator(Mapper<E> generatedMapper)
	{
		if (generatedMapper == null) throw new IllegalArgumentException("Mapper is null");
		this.mapper = generatedMapper;
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		return mapper.add(entities);
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		return mapper.update(entities);
	}

	@Override
	public int remove(List<E> entities) throws DatabaseException
	{
		return mapper.remove(entities);
	}

	@Override
	public int count(QueryRule... rules) throws DatabaseException
	{
		return mapper.count(rules);
	}

	@Override
	public List<E> find(QueryRule... rules) throws DatabaseException
	{
		return mapper.find(rules);
	}

	@Override
	public Database getDatabase()
	{
		return mapper.getDatabase();
	}

	@Override
	public FieldType getFieldType(String field)
	{
		return mapper.getFieldType(field);
	}

	@Override
	public String getTableFieldName(String field)
	{
		return mapper.getTableFieldName(field);
	}

	@Override
	public E create()
	{
		return mapper.create();
	}

	@Override
	public void resolveForeignKeys(List<E> enteties) throws ParseException, DatabaseException
	{
		mapper.resolveForeignKeys(enteties);
	}

	@Override
	public String createFindSqlInclRules(QueryRule[] rules) throws DatabaseException
	{
		return mapper.createFindSqlInclRules(rules);
	}

	@Override
	public E findById(Object id) throws DatabaseException
	{
		return this.mapper.findById(id);
	}

	@Override
	public int executeAdd(List<? extends E> entities) throws DatabaseException
	{
		return this.mapper.executeAdd(entities);
	}

	@Override
	public int executeUpdate(List<? extends E> entities) throws DatabaseException
	{
		return this.mapper.executeUpdate(entities);
	}

	@Override
	public int executeRemove(List<? extends E> entities) throws DatabaseException
	{
		return this.mapper.executeRemove(entities);
	}

	@Override
	public List<E> createList(int i)
	{
		return this.mapper.createList(i);
	}

}
