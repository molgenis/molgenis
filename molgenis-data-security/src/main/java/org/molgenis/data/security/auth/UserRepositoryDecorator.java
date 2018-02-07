package org.molgenis.data.security.auth;

import com.google.common.collect.Iterators;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.GroupMemberMetaData.GROUP_MEMBER;

public class UserRepositoryDecorator extends AbstractRepositoryDecorator<User>
{
	private static final int BATCH_SIZE = 1000;

	private final DataService dataService;
	private final PasswordEncoder passwordEncoder;

	public UserRepositoryDecorator(Repository<User> delegateRepository, DataService dataService,
			PasswordEncoder passwordEncoder)
	{
		super(delegateRepository);
		this.dataService = requireNonNull(dataService);
		this.passwordEncoder = requireNonNull(passwordEncoder);
	}

	@Override
	public void add(User entity)
	{
		encodePassword(entity);
		delegate().add(entity);
	}

	@Override
	public void update(User entity)
	{
		updatePassword(entity);
		delegate().update(entity);
	}

	@Override
	public Integer add(Stream<User> entities)
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
	public void update(Stream<User> entities)
	{
		entities = entities.filter(entity ->
		{
			updatePassword(entity);
			return true;
		});
		delegate().update(entities);
	}

	private void updatePassword(User user)
	{
		User currentUser = findOneById(user.getId());

		String currentPassword = currentUser.getPassword();
		String password = user.getPassword();
		//password is updated
		if (!currentPassword.equals(password))
		{
			password = passwordEncoder.encode(user.getPassword());
		}
		user.setPassword(password);
	}

	private void encodePassword(User user)
	{
		String password = user.getPassword();
		String encodedPassword = passwordEncoder.encode(password);
		user.setPassword(encodedPassword);
	}

	@Override
	public void delete(User entity)
	{
		deleteGroupMemberships(entity);
		delegate().delete(entity);
	}

	@Override
	public void delete(Stream<User> entities)
	{
		entities = entities.map(entity ->
		{
			deleteGroupMemberships(entity);
			return entity;
		});
		delegate().delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		deleteGroupMemberships(findOneById(id));
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		ids = ids.map(id ->
		{
			deleteGroupMemberships(findOneById(id));
			return id;
		});
		delegate().deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException("Deleting all users is not supported.");
	}

	private void deleteGroupMemberships(User user)
	{
		Stream<GroupMember> groupMembers = dataService.findAll(GROUP_MEMBER,
				new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user), GroupMember.class);
		dataService.delete(GROUP_MEMBER, groupMembers);
	}
}
