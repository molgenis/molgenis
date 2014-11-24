package org.molgenis.security.user;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class MolgenisUserDecorator extends CrudRepositoryDecorator
{
	public MolgenisUserDecorator(CrudRepository decoratedRepository)
	{
		super(decoratedRepository);
	}

	@Override
	public void add(Entity entity)
	{
		encodePassword(entity);
		super.add(entity);

		// id is only guaranteed to be generated at flush time
		super.flush();
		addSuperuserAuthority(entity);
	}

	@Override
	public void update(Entity entity)
	{
		updatePassword(entity);
		super.update(entity);

		// id is only guaranteed to be generated at flush time
		super.flush();
		updateSuperuserAuthority(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		Integer nr = super.add(Iterables.transform(entities, new Function<Entity, Entity>()
		{
			@Override
			public Entity apply(Entity entity)
			{
				encodePassword(entity);
				return entity;
			}
		}));

		// id is only guaranteed to be generated at flush time
		super.flush();
		addSuperuserAuthorities(entities);

		return nr;
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		super.update(Iterables.transform(entities, new Function<Entity, Entity>()
		{
			@Override
			public Entity apply(Entity entity)
			{
				updatePassword(entity);
				return entity;
			}
		}));

		// id is only guaranteed to be generated at flush time
		super.flush();
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
		CrudRepository userAuthorityRepository = dataService.getCrudRepository(UserAuthority.class.getSimpleName());
		if (userAuthorityRepository == null)
		{
			throw new RuntimeException("missing required UserAuthority repository");
		}
		return userAuthorityRepository;
	}

}
