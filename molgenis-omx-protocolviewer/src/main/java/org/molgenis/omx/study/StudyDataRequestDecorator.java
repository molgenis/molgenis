package org.molgenis.omx.study;

import java.util.Iterator;
import java.util.List;

import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Query;
import org.molgenis.data.support.ConvertingIterable;
import org.molgenis.data.support.QueryImpl;
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
public class StudyDataRequestDecorator extends CrudRepositoryDecorator
{
	public StudyDataRequestDecorator(CrudRepositoryDecorator crudRepositoryDecorator)
	{
		super(crudRepositoryDecorator);
	}

	@Override
	public long count()
	{
		return count(new QueryImpl());
	}

	@Override
	public long count(Query q)
	{
		addUserRule(q);
		return super.count(q);
	}

	@Override
	public Integer add(Entity entity)
	{
		checkEntitiesPermission(entity);
		return super.add(entity);
	}

	@Override
	public void add(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
		{
			checkEntitiesPermission(entity);
		}
		super.add(entities);
	}

	@Override
	public void update(Entity entity)
	{
		checkEntitiesPermission(entity);
		super.update(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
		{
			checkEntitiesPermission(entity);
		}
		super.update(entities);
	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		for (Entity entity : entities)
		{
			checkEntitiesPermission(entity);
		}
		super.update(entities, dbAction, keyName);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		addUserRule(q);
		return super.findAll(q);
	}

	@Override
	public void delete(Entity entity)
	{
		checkEntitiesPermission(entity);
		super.delete(entity);
	}

	@Override
	public Entity findOne(Query q)
	{
		addUserRule(q);
		return super.findOne(q);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		for (Entity request : entities)
		{
			checkEntitiesPermission(request);
		}

		super.delete(entities);
	}

	@Override
	public void deleteById(Integer id)
	{
		Entity entity = super.findOne(id);
		checkEntitiesPermission(entity);

		super.deleteById(id);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return findAll(new QueryImpl()).iterator();
	}

	@Override
	public Entity findOne(Integer id)
	{
		Entity entity = super.findOne(id);
		checkEntitiesPermission(entity);

		return entity;
	}

	@Override
	public void deleteById(Iterable<Integer> ids)
	{
		for (Entity entity : super.findAll(ids))
		{
			checkEntitiesPermission(entity);
		}

		super.deleteById(ids);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Integer> ids)
	{
		Iterable<Entity> entities = super.findAll(ids);
		for (Entity entity : entities)
		{
			checkEntitiesPermission(entity);
		}

		return entities;
	}

	@Override
	public void deleteAll()
	{
		super.delete(findAll(new QueryImpl()));
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		return findAll(new QueryImpl(), clazz);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(q));
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(ids));
	}

	public void addUserRule(Query q)
	{
		MolgenisUser user = getCurrentUser();
		if (user.getSuperuser())
		{
			return;
		}
		q.eq(StudyDataRequest.MOLGENISUSER, user);
	}

	private void checkEntitiesPermission(Entity request)
	{
		MolgenisUser user = getCurrentUser();
		if (!user.getSuperuser())
		{
			if (!hasEntityPermission(request))
			{
				throw new MolgenisDataAccessException("No permission on DataSetFilter");
			}
		}
	}

	private boolean hasEntityPermission(Entity request)
	{
		MolgenisUser user = getCurrentUser();
		StudyDataRequest sdr = (StudyDataRequest) request;

		if (!user.getId().equals(sdr.getMolgenisUser().getId()))
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