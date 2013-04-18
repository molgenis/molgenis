package org.molgenis.omx.filter.decorators;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.service.MolgenisUserService;
import org.molgenis.omx.filter.DataSetFilter;

/**
 * decorator for DataSetFilter, Checks for every read, update and delete operation if the user requesting the operation
 * matches the user owning the DataSetFilter on which the operation is requested
 */
public class DataSetFilterDecorator<E extends DataSetFilter> extends MapperDecorator<E>
{

	private MolgenisUserService userService;

	public DataSetFilterDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		for (E entity : entities)
			entity.setUserId(getCurrentUser());
		return super.add(entities);
	}

	@Override
	public E create()
	{
		E dataSetFilter = super.create();
		if (dataSetFilter != null)
		{
			// TODO remove try/catch when Mapper.java create() throws DatabaseException
			try
			{
				dataSetFilter.setUserId(getCurrentUser());
			}
			catch (DatabaseException e)
			{
				throw new RuntimeException(e);
			}
		}
		return dataSetFilter;
	}

	@Override
	public List<E> createList(int i)
	{
		List<E> dataSetFilters = super.createList(i);
		if (dataSetFilters != null)
		{
			try
			{
				for (E dataSetFilter : dataSetFilters)
					dataSetFilter.setUserId(getCurrentUser());
			}
			catch (DatabaseException e)
			{
				throw new RuntimeException(e);
			}
		}
		return dataSetFilters;
	}

	@Override
	public int count(QueryRule... rules) throws DatabaseException
	{
		rules = addUserRule(rules);
		return super.count(rules);
	}

	@Override
	public List<E> find(QueryRule... rules) throws DatabaseException
	{
		rules = addUserRule(rules);
		return super.find(rules);
	}

	@Override
	public void find(TupleWriter writer, QueryRule... rules) throws DatabaseException
	{
		rules = addUserRule(rules);
		super.find(writer, rules);
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		checkEntitiesPermission(entities);
		return super.update(entities);
	}

	@Override
	public int remove(List<E> entities) throws DatabaseException
	{
		checkEntitiesPermission(entities);
		return super.remove(entities);
	}

	@Override
	public void find(TupleWriter writer, List<String> fieldsToExport, QueryRule[] rules) throws DatabaseException
	{
		rules = addUserRule(rules);
		super.find(writer, fieldsToExport, rules);
	}

	@Override
	public String createFindSqlInclRules(QueryRule[] rules) throws DatabaseException
	{
		rules = addUserRule(rules);
		return super.createFindSqlInclRules(rules);
	}

	@Override
	public E findById(Object id) throws DatabaseException
	{
		MolgenisUser user = getCurrentUser();
		E entity = super.findById(id);
		if (!hasEntityPermission(entity) && !user.getSuperuser())
		{
			throw new DatabaseAccessException("No permission on DataSetFilter");
		}
		return entity;
	}

	@Override
	public List<E> findByExample(E example) throws DatabaseException
	{
		List<E> entities = super.findByExample(example);
		List<E> filteredEntities = filterEntities(entities);
		return filteredEntities;
	}

	private List<E> filterEntities(List<E> entities) throws DatabaseException
	{
		MolgenisUser user = getCurrentUser();
		List<E> filteredEntities = new ArrayList<E>();
		if (!user.getSuperuser())
		{
			for (DataSetFilter filter : entities)
			{
				if (hasEntityPermission(filter))
				{
					filteredEntities.add((E) filter);
				}
			}
		}
		else
		{
			filteredEntities = entities;
		}
		return filteredEntities;
	}

	private MolgenisUser getCurrentUser() throws DatabaseException
	{
		return getMolgenisUserService().findById(getDatabase().getLogin().getUserId());
	}

	public MolgenisUserService getMolgenisUserService()
	{
		if (userService == null)
		{
			userService = MolgenisUserService.getInstance(getDatabase());
		}
		return userService;
	}

	public void setMolgenisUserService(MolgenisUserService service)
	{
		this.userService = service;
	}

	public QueryRule[] addUserRule(QueryRule[] array) throws DatabaseException
	{
		MolgenisUser user = getCurrentUser();
		if (user.getSuperuser())
		{
			return array;
		}

		QueryRule rule = new QueryRule("userId", Operator.EQUALS, user.getId());
		return addRule(array, rule);
	}

	public QueryRule[] addRule(QueryRule[] array, QueryRule rule)
	{
		QueryRule[] anotherArray = new QueryRule[array.length + 1];
		System.arraycopy(array, 0, anotherArray, 0, array.length);
		anotherArray[array.length] = rule;

		return anotherArray;
	}

	private void checkEntitiesPermission(List<E> entities) throws DatabaseException
	{
		MolgenisUser user = getCurrentUser();
		if (!user.getSuperuser())
		{
			for (DataSetFilter filter : entities)
			{
				if (!hasEntityPermission(filter))
				{
					throw new DatabaseAccessException("No permission on DataSetFilter");
				}
			}
		}
	}

	private boolean hasEntityPermission(DataSetFilter filter) throws DatabaseException
	{
		MolgenisUser user = getCurrentUser();
		if (!user.getId().equals(filter.getUserId_Id()))
		{
			return false;
		}
		return true;
	}

	@Override
	public int update(TupleReader reader) throws DatabaseException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int remove(TupleReader reader) throws DatabaseException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<E> toList(TupleReader reader, int limit) throws DatabaseException
	{
		throw new UnsupportedOperationException();
	}
}
