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
import static org.molgenis.data.security.auth.AuthorityMetaData.ROLE;
import static org.molgenis.data.security.auth.GroupMemberMetaData.GROUP_MEMBER;
import static org.molgenis.data.security.auth.UserAuthorityMetaData.USER;
import static org.molgenis.data.security.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;

public class UserRepositoryDecorator extends AbstractRepositoryDecorator<User>
{
	private static final int BATCH_SIZE = 1000;

	private final UserAuthorityFactory userAuthorityFactory;
	private final DataService dataService;
	private final PasswordEncoder passwordEncoder;

	public UserRepositoryDecorator(Repository<User> delegateRepository, UserAuthorityFactory userAuthorityFactory,
			DataService dataService, PasswordEncoder passwordEncoder)
	{
		super(delegateRepository);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.dataService = requireNonNull(dataService);
		this.passwordEncoder = requireNonNull(passwordEncoder);
	}

	@Override
	public void add(User entity)
	{
		encodePassword(entity);
		delegate().add(entity);
		addSuperuserAuthority(entity);
	}

	@Override
	public void update(User entity)
	{
		updatePassword(entity);
		delegate().update(entity);
		updateSuperuserAuthority(entity);
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

			users.forEach(this::addSuperuserAuthority);
		});
		return count.get();
	}

	@Override
	public void update(Stream<User> entities)
	{
		entities = entities.map(entity ->
		{
			updatePassword(entity);
			return entity;
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

	private void addSuperuserAuthority(User user)
	{
		Boolean isSuperuser = user.isSuperuser();
		if (isSuperuser != null && isSuperuser)
		{
			UserAuthority userAuthority = userAuthorityFactory.create();
			userAuthority.setUser(user);
			userAuthority.setRole(AUTHORITY_SU);

			dataService.add(USER_AUTHORITY, userAuthority);
		}
	}

	private void updateSuperuserAuthority(User user)
	{
		UserAuthority suAuthority = dataService.findOne(USER_AUTHORITY,
				new QueryImpl<UserAuthority>().eq(USER, user).and().eq(ROLE, AUTHORITY_SU), UserAuthority.class);

		Boolean isSuperuser = user.isSuperuser();
		if (isSuperuser != null && isSuperuser)
		{
			if (suAuthority == null)
			{
				UserAuthority userAuthority = userAuthorityFactory.create();
				userAuthority.setUser(user);
				userAuthority.setRole(AUTHORITY_SU);
				dataService.add(USER_AUTHORITY, userAuthority);
			}
		}
		else
		{
			if (suAuthority != null)
			{
				dataService.deleteById(USER_AUTHORITY, suAuthority.getId());
			}
		}
	}

	@Override
	public void delete(User entity)
	{
		deleteUserAuthoritiesAndGroupMember(entity);
		delegate().delete(entity);
	}

	@Override
	public void delete(Stream<User> entities)
	{
		entities = entities.map(entity ->
		{
			deleteUserAuthoritiesAndGroupMember(entity);
			return entity;
		});
		delegate().delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		deleteUserAuthoritiesAndGroupMember(findOneById(id));
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		ids = ids.map(id ->
		{
			deleteUserAuthoritiesAndGroupMember(findOneById(id));
			return id;
		});
		delegate().deleteAll(ids);
	}

	private void deleteUserAuthoritiesAndGroupMember(User user)
	{
		Stream<UserAuthority> userAuthorities = dataService.findAll(USER_AUTHORITY,
				new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.USER, user), UserAuthority.class);
		dataService.delete(USER_AUTHORITY, userAuthorities);

		Stream<GroupMember> groupMembers = dataService.findAll(GROUP_MEMBER,
				new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user), GroupMember.class);
		dataService.delete(GROUP_MEMBER, groupMembers);
	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException("Deleting all users is not supported.");
	}
}
