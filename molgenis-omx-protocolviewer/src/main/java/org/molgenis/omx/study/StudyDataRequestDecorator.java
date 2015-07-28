package org.molgenis.omx.study;

import java.util.List;

import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;

/**
 * decorator for StudyDataRequest, Checks for every read, update and delete operation if the user requesting the
 * operation matches the user owning the StudyDataRequest on which the operation is requested
 */
public class StudyDataRequestDecorator<E extends StudyDataRequest> extends MapperDecorator<E>
{
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

	public QueryRule[] addUserRule(QueryRule[] array) throws DatabaseException
	{
		MolgenisUser user = getCurrentUser();
		if (user.getSuperuser())
		{
			return array;
		}

		QueryRule rule = new QueryRule(StudyDataRequest.MOLGENISUSER, Operator.EQUALS, user);
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

	private MolgenisUser getCurrentUser() throws DatabaseException
	{
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		if (applicationContext == null)
		{
			throw new RuntimeException(new ApplicationContextException("missing required application context"));
		}
		MolgenisUserService molgenisUserService = applicationContext.getBean(MolgenisUserService.class);
		if (molgenisUserService == null)
		{
			throw new RuntimeException(new ApplicationContextException("missing required MolgenisUserService bean"));
		}
		return molgenisUserService.getCurrentUser();
	}

}