package org.molgenis.omx.study;

import java.util.Iterator;

import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.SecurityUtils;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;

/**
 * decorator for StudyDataRequest, Checks for every read, update and delete operation if the user requesting the
 * operation matches the user owning the StudyDataRequest on which the operation is requested
 */
public class StudyDataRequestDecorator<E extends StudyDataRequest> extends CrudRepositoryDecorator<E>
{
	public StudyDataRequestDecorator(CrudRepositoryDecorator<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public long count(Query q)
	{
		addUserRule(q);
		return super.count(q);
	}

	@Override
	public void update(Iterable<E> entities)
	{
		for (StudyDataRequest request : entities)
		{
			checkEntitiesPermission(request);
		}

		super.update(entities);
	}

	@Override
	public Iterable<E> findAll(Query q)
	{
		addUserRule(q);
		return super.findAll(q);
	}

	@Override
	public void delete(E entity)
	{
		checkEntitiesPermission(entity);
		super.delete(entity);
	}

	@Override
	public E findOne(Query q)
	{
		addUserRule(q);
		return super.findOne(q);
	}

	@Override
	public void delete(Iterable<E> entities)
	{
		for (StudyDataRequest request : entities)
		{
			checkEntitiesPermission(request);
		}

		super.delete(entities);
	}

	@Override
	public void deleteById(Integer id)
	{
		E entity = super.findOne(id);
		checkEntitiesPermission(entity);

		super.deleteById(id);
	}

	@Override
	public Iterator<E> iterator()
	{
		return findAll(new QueryImpl()).iterator();
	}

	@Override
	public E findOne(Integer id)
	{
		E entity = super.findOne(id);
		checkEntitiesPermission(entity);

		return entity;
	}

	@Override
	public void deleteById(Iterable<Integer> ids)
	{
		for (E entity : super.findAll(ids))
		{
			checkEntitiesPermission(entity);
		}

		super.deleteById(ids);
	}

	@Override
	public Iterable<E> findAll(Iterable<Integer> ids)
	{
		Iterable<E> all = super.findAll(ids);
		for (E entity : all)
		{
			checkEntitiesPermission(entity);
		}

		return all;
	}

	@Override
	public void deleteAll()
	{
		super.delete(findAll(new QueryImpl()));
	}

	public void addUserRule(Query q)
	{
		MolgenisUser user = getCurrentUser();
		if (user.getSuperuser())
		{
			return;
		}

		QueryRule rule = new QueryRule(StudyDataRequest.MOLGENISUSER, Operator.EQUALS, user);
		addRule(q, rule);
	}

	public void addRule(Query q, QueryRule r)
	{
		q.getRules().add(r);
	}

	private void checkEntitiesPermission(StudyDataRequest request)
	{
		MolgenisUser user = getCurrentUser();
		if (!user.getSuperuser())
		{
			if (!hasEntityPermission(request))
			{
				throw new DatabaseAccessException("No permission on DataSetFilter");
			}
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
		return molgenisUserService.getUser(SecurityUtils.getCurrentUsername());
	}

}