package org.molgenis.data.security.model;

import com.google.common.collect.Iterators;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class UserRepositoryDecorator extends AbstractRepositoryDecorator<UserEntity>
{
	private static final int BATCH_SIZE = 1000;

	private final PasswordEncoder passwordEncoder;

	public UserRepositoryDecorator(Repository<UserEntity> delegateRepository, PasswordEncoder passwordEncoder)
	{
		super(delegateRepository);
		this.passwordEncoder = requireNonNull(passwordEncoder);
	}

	@Override
	public void add(UserEntity entity)
	{
		encodePassword(entity);
		delegate().add(entity);
	}

	@Override
	public void update(UserEntity entity)
	{
		updatePassword(entity);
		delegate().update(entity);
	}

	@Override
	public Integer add(Stream<UserEntity> entities)
	{
		AtomicInteger count = new AtomicInteger();
		Iterators.partition(entities.iterator(), BATCH_SIZE).forEachRemaining(users ->
		{
			users.forEach(this::encodePassword);

			Integer batchCount = delegate().add(users.stream());
			count.addAndGet(batchCount);
		});
		return count.get();
	}

	@Override
	public void update(Stream<UserEntity> entities)
	{
		entities = entities.map(entity ->
		{
			updatePassword(entity);
			return entity;
		});
		delegate().update(entities);
	}

	private void updatePassword(UserEntity user)
	{
		UserEntity currentUser = findOneById(user.getId());

		String currentPassword = currentUser.getPassword();
		String password = user.getPassword();
		//password is updated
		if (!currentPassword.equals(password))
		{
			password = passwordEncoder.encode(user.getPassword());
		}
		user.setPassword(password);
	}

	private void encodePassword(UserEntity user)
	{
		String password = user.getPassword();
		String encodedPassword = passwordEncoder.encode(password);
		user.setPassword(encodedPassword);
	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException("Deleting all users is not supported.");
	}
}
