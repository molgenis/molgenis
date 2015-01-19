package org.molgenis.security.user;

import java.io.IOException;
import java.util.Iterator;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class MolgenisUserDecorator implements CrudRepository
{
	private final CrudRepository decoratedRepository;

	public MolgenisUserDecorator(CrudRepository decoratedRepository)
	{
		this.decoratedRepository = decoratedRepository;
	}

	@Override
	public void add(Entity entity)
	{
		encodePassword(entity);
		decoratedRepository.add(entity);

		// id is only guaranteed to be generated at flush time
		decoratedRepository.flush();
		addSuperuserAuthority(entity);
	}

	@Override
	public void update(Entity entity)
	{
		updatePassword(entity);
		decoratedRepository.update(entity);

		// id is only guaranteed to be generated at flush time
		decoratedRepository.flush();
		updateSuperuserAuthority(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		Integer nr = decoratedRepository.add(Iterables.transform(entities, new Function<Entity, Entity>()
		{
			@Override
			public Entity apply(Entity entity)
			{
				encodePassword(entity);
				return entity;
			}
		}));

		// id is only guaranteed to be generated at flush time
		decoratedRepository.flush();
		addSuperuserAuthorities(entities);

		return nr;
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		decoratedRepository.update(Iterables.transform(entities, new Function<Entity, Entity>()
		{
			@Override
			public Entity apply(Entity entity)
			{
				updatePassword(entity);
				return entity;
			}
		}));

		// id is only guaranteed to be generated at flush time
		decoratedRepository.flush();
		updateSuperuserAuthorities(entities);
	}

	private void updatePassword(Entity entity)
	{
		MolgenisUser currentUser = findOne(entity.getIdValue(), MolgenisUser.class);
		String currentPassword = currentUser.getPassword();
		String password = entity.getString(MolgenisUser.PASSWORD_);

		if (!password.equals(currentPassword))
		{
			encodePassword(entity);
		}
	}

	private void encodePassword(Entity entity)
	{
		String password = entity.getString(MolgenisUser.PASSWORD_);
		String encodedPassword = getPasswordEncoder().encode(password);
		entity.set(MolgenisUser.PASSWORD_, encodedPassword);
	}

	private void addSuperuserAuthorities(Iterable<? extends Entity> entities)
	{
		// performance improvement: add filtered iterable to repo
		for (Entity entity : entities)
		{
			addSuperuserAuthority(entity);
		}
	}

	private void addSuperuserAuthority(Entity entity)
	{
		Boolean isSuperuser = entity.getBoolean(MolgenisUser.SUPERUSER);
		if (isSuperuser != null && isSuperuser == true)
		{
			MolgenisUser molgenisUser = findOne(entity.getIdValue(), MolgenisUser.class);

			UserAuthority userAuthority = new UserAuthority();
			userAuthority.setMolgenisUser(molgenisUser);
			userAuthority.setRole(SecurityUtils.AUTHORITY_SU);

			getUserAuthorityRepository().add(userAuthority);
		}
	}

	private void updateSuperuserAuthorities(Iterable<? extends Entity> entities)
	{
		// performance improvement: add filtered iterable to repo
		for (Entity entity : entities)
		{
			updateSuperuserAuthority(entity);
		}
	}

	private void updateSuperuserAuthority(Entity entity)
	{
		MolgenisUser molgenisUser = findOne(entity.getIdValue(), MolgenisUser.class);

		CrudRepository userAuthorityRepository = getUserAuthorityRepository();

		UserAuthority suAuthority = userAuthorityRepository.findOne(
				new QueryImpl().eq(UserAuthority.MOLGENISUSER, molgenisUser).and()
						.eq(UserAuthority.ROLE, SecurityUtils.AUTHORITY_SU), UserAuthority.class);

		Boolean isSuperuser = entity.getBoolean(MolgenisUser.SUPERUSER);
		if (isSuperuser != null && isSuperuser == true)
		{
			if (suAuthority == null)
			{

				UserAuthority userAuthority = new UserAuthority();
				userAuthority.setMolgenisUser(molgenisUser);
				userAuthority.setRole(SecurityUtils.AUTHORITY_SU);
				userAuthorityRepository.add(userAuthority);
			}
		}
		else
		{
			if (suAuthority != null)
			{
				userAuthorityRepository.deleteById(suAuthority.getId());
			}
		}
	}

	private PasswordEncoder getPasswordEncoder()
	{
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		if (applicationContext == null)
		{
			throw new RuntimeException(new ApplicationContextException("missing required application context"));
		}
		PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
		if (passwordEncoder == null)
		{
			throw new RuntimeException(new ApplicationContextException("missing required PasswordEncoder bean"));
		}
		return passwordEncoder;
	}

	private CrudRepository getUserAuthorityRepository()
	{
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		if (applicationContext == null)
		{
			throw new RuntimeException(new ApplicationContextException("missing required application context"));
		}
		DataService dataService = applicationContext.getBean(DataService.class);
		if (dataService == null)
		{
			throw new RuntimeException(new ApplicationContextException("missing required DataService bean"));
		}
		CrudRepository userAuthorityRepository = dataService.getRepository(UserAuthority.class.getSimpleName());
		if (userAuthorityRepository == null)
		{
			throw new RuntimeException("missing required UserAuthority repository");
		}
		return userAuthorityRepository;
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepository.getEntityMetaData();
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		return decoratedRepository.iterator(clazz);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public void flush()
	{
		decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepository.clearCache();
	}

	@Override
	public long count()
	{
		return decoratedRepository.count();
	}

	@Override
	public Query query()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count(Query q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		return decoratedRepository.findAll(q, clazz);
	}

	@Override
	public Entity findOne(Query q)
	{
		return decoratedRepository.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return decoratedRepository.findOne(id);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Object> ids, Class<E> clazz)
	{
		return decoratedRepository.findAll(ids, clazz);
	}

	@Override
	public <E extends Entity> E findOne(Object id, Class<E> clazz)
	{
		return decoratedRepository.findOne(id, clazz);
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		return decoratedRepository.findOne(q, clazz);
	}

	@Override
	public void delete(Entity entity)
	{
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		decoratedRepository.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		decoratedRepository.deleteAll();
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepository.aggregate(aggregateQuery);
	}

}
