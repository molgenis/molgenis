package org.molgenis.framework.db;

import java.text.ParseException;
import java.util.List;

import org.molgenis.fieldtypes.FieldType;
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.util.Entity;

public class MapperDecorator<E extends Entity> implements Mapper<E>
{
	private Mapper<E> mapper;

	public MapperDecorator(Mapper<E> generatedMapper)
	{
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
	public int add(TupleReader reader, TupleWriter writer) throws DatabaseException
	{
		return mapper.add(reader, writer);
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
	public void find(TupleWriter writer, QueryRule... rules) throws DatabaseException
	{
		mapper.find(writer, rules);
	}

	@Override
	public Database getDatabase()
	{
		return mapper.getDatabase();
	}

	@Override
	public int remove(TupleReader reader) throws DatabaseException
	{
		return mapper.remove(reader);
	}

	@Override
	public List<E> toList(TupleReader reader, int limit) throws DatabaseException
	{
		return mapper.toList(reader, limit);
	}

	@Override
	public int update(TupleReader reader) throws DatabaseException
	{
		return mapper.update(reader);
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
	public void find(TupleWriter writer, List<String> fieldsToExport, QueryRule[] rules) throws DatabaseException
	{
		mapper.find(writer, fieldsToExport, rules);
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
	public List<E> findByExample(E example) throws DatabaseException
	{
		return this.mapper.findByExample(example);
	}

	@Override
	public final int executeAdd(List<? extends E> entities) throws DatabaseException
	{
		return this.mapper.executeAdd(entities);
	}

	@Override
	public final int executeUpdate(List<? extends E> entities) throws DatabaseException
	{
		return this.mapper.executeUpdate(entities);
	}

	@Override
	public final int executeRemove(List<? extends E> entities) throws DatabaseException
	{
		return this.mapper.executeRemove(entities);
	}

	@Override
	public List<E> createList(int i)
	{
		return this.mapper.createList(i);
	}

}
