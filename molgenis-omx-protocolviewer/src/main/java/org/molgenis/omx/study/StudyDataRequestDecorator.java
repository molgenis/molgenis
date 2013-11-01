package org.molgenis.omx.study;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;

/**
 * decorator for DataSetFilter, Checks for every read, update and delete operation if the user requesting the operation
 * matches the user owning the DataSetFilter on which the operation is requested
 */
public class StudyDataRequestDecorator<E extends StudyDataRequest> extends CrudRepositoryDecorator<E>
{
	public StudyDataRequestDecorator(CrudRepository<E> generatedRepository)
	{
		super(generatedRepository);
	}

	@Override
	public long count(Query q)
	{
		q = addUserRule(q);
		return super.count(q);
	}

	@Override
	public void update(E entity)
	{
		checkEntityPermission(entity);
		super.update(entity);
	}

	@Override
	public void update(Iterable<E> entities)
	{
		checkEntitiesPermission(entities);
		super.update(entities);
	}

	@Override
	public Iterable<E> findAll(Query q)
	{
		q = addUserRule(q);
		return super.findAll(q);
	}

	@Override
	public void delete(E entity)
	{
		checkEntityPermission(entity);
		super.delete(entity);
	}

	@Override
	public E findOne(Query q)
	{
		q = addUserRule(q);
		return super.findOne(q);
	}

	@Override
	public void delete(Iterable<E> entities)
	{
		checkEntitiesPermission(entities);
		super.delete(entities);
	}

	@Override
	public void deleteById(Integer id)
	{
		findOne(id);
		super.deleteById(id);
	}

	@Override
	public E findOne(Integer id)
	{
		MolgenisUser user = getCurrentUser();
		E entity = super.findOne(id);
		if (!hasEntityPermission(entity) && !user.getSuperuser())
		{
			throw new MolgenisDataException("No permission on DataSetFilter");
		}

		return entity;
	}

	@Override
	public void deleteById(Iterable<Integer> ids)
	{
		findAll(ids);
		super.deleteById(ids);
	}

	@Override
	public Iterable<E> findAll(Iterable<Integer> ids)
	{
		Iterable<E> entities = super.findAll(ids);
		MolgenisUser user = getCurrentUser();

		if (!user.getSuperuser())
		{
			for (E entity : entities)
			{
				if (!hasEntityPermission(entity))
				{
					throw new MolgenisDataException("No permission on DataSetFilter");
				}
			}
		}

		return entities;
	}

	@Override
	public void deleteAll()
	{
		Iterable<E> entities = super.findAll(new QueryImpl());
		checkEntitiesPermission(entities);

		super.deleteAll();
	}

	public Query addUserRule(Query q)
	{
		MolgenisUser user = getCurrentUser();
		if (user.getSuperuser())
		{
			return q;
		}

		QueryImpl result = new QueryImpl(q.getRules());
		result.addRule(new QueryRule("MolgenisUser", Operator.EQUALS, user));

		return result;
	}

	private void checkEntitiesPermission(Iterable<E> entities)
	{
		MolgenisUser user = getCurrentUser();
		if (!user.getSuperuser())
		{
			for (E request : entities)
			{
				checkEntityPermission(request);
			}
		}
	}

	private void checkEntityPermission(E entity)
	{
		if (!hasEntityPermission(entity))
		{
			throw new DatabaseAccessException("No permission on DataSetFilter");
		}
	}

	private boolean hasEntityPermission(StudyDataRequest request)
	{
		MolgenisUser user = getCurrentUser();
		if (!user.getId().equals(request.getMolgenisUser().getId()))
		{
			return false;
		}
		return true;
	}

	private MolgenisUser getCurrentUser()
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