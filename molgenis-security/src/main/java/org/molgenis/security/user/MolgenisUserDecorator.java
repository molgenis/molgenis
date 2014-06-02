package org.molgenis.security.user;

import java.util.List;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
	}

	@Override
	public void update(Entity entity)
	{
		updatePassword(entity);
		super.update(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		return super.add(Iterables.transform(entities, new Function<Entity, Entity>()
		{
			@Override
			public Entity apply(Entity entity)
			{
				encodePassword(entity);
				return entity;
			}
		}));
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		super.update(Iterables.transform(records, new Function<Entity, Entity>()
		{
			@Override
			public Entity apply(Entity entity)
			{
				updatePassword(entity);
				return entity;
			}
		}));
	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		super.update(Lists.transform(entities, new Function<Entity, Entity>()
		{
			@Override
			public Entity apply(Entity entity)
			{
				updatePassword(entity);
				return entity;
			}
		}), dbAction, keyName);
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
}
