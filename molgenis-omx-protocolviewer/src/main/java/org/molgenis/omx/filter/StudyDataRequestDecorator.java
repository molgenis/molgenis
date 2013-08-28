package org.molgenis.omx.filter;

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

/**
 * decorator for DataSetFilter, Checks for every read, update and delete operation if the user requesting the operation
 * matches the user owning the DataSetFilter on which the operation is requested
 */
public class StudyDataRequestDecorator<E extends StudyDataRequest> extends MapperDecorator<E>
{

	private MolgenisUserService userService;

	public StudyDataRequestDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
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

		QueryRule rule = new QueryRule("MolgenisUser", Operator.EQUALS, user);
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
			for (StudyDataRequest request : entities)
			{
				if (!hasEntityPermission(request))
				{
					throw new DatabaseAccessException("No permission on DataSetFilter");
				}
			}
		}
	}

	private boolean hasEntityPermission(StudyDataRequest request) throws DatabaseException
	{
		MolgenisUser user = getCurrentUser();
		if (!user.getId().equals(request.getMolgenisUser().getId()))
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