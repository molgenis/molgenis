package org.molgenis.data.security.service.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.security.model.UserEntity;
import org.molgenis.data.security.model.UserFactory;
import org.molgenis.data.security.model.UserMetadata;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.service.UserService;
import org.molgenis.security.core.service.exception.EmailAlreadyExistsException;
import org.molgenis.security.core.service.exception.UsernameAlreadyExistsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.model.UserMetadata.*;

@Service
public class UserServiceImpl implements UserService
{
	private final DataService dataService;
	private final UserFactory userFactory;

	public UserServiceImpl(DataService dataService, UserFactory userFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.userFactory = requireNonNull(userFactory);
	}

	@Override
	@RunAsSystem
	public List<String> getSuEmailAddresses()
	{
		return dataService.findAll(USER, new QueryImpl<UserEntity>().eq(SUPERUSER, true), UserEntity.class)
						  .map(UserEntity::getEmail)
						  .collect(toList());
	}

	@Override
	@RunAsSystem
	public Optional<User> findByEmailIfPresent(String email)
	{
		return Optional.ofNullable(
				dataService.findOne(USER, new QueryImpl<UserEntity>().eq(EMAIL, email), UserEntity.class))
					   .map(UserEntity::toUser);
	}

	private Optional<User> findByIdIfPresent(String id)
	{
		return Optional.ofNullable(dataService.findOneById(USER, id, UserEntity.class)).map(UserEntity::toUser);
	}

	@Override
	public Optional<User> findByGoogleAccountIdIfPresent(String googleAccountId)
	{
		return Optional.ofNullable(
				dataService.findOne(USER, new QueryImpl<UserEntity>().eq(GOOGLEACCOUNTID, googleAccountId),
						UserEntity.class)).map(UserEntity::toUser);
	}

	@Override
	@RunAsSystem
	public Optional<User> findByUsernameIfPresent(String username)
	{
		return getUserEntity(username).map(UserEntity::toUser);
	}

	private Optional<UserEntity> getUserEntity(String username)
	{
		return Optional.ofNullable(
				dataService.findOne(USER, new QueryImpl<UserEntity>().eq(USERNAME, username), UserEntity.class));
	}

	@Override
	@RunAsSystem
	public User update(User user)
	{
		String username = user.getUsername();
		UserEntity userEntity = getUserEntity(username).orElseThrow(() -> new UsernameNotFoundException(username));
		userEntity.updateFrom(user);
		dataService.update(USER, userEntity);
		return user;
	}

	@Override
	public Optional<User> activateUserUsingCode(String activationCode)
	{
		return Optional.ofNullable(
				dataService.findOne(USER, new QueryImpl<UserEntity>().eq(UserMetadata.ACTIVATIONCODE, activationCode),
						UserEntity.class))
					   .map(UserEntity::toUser)
					   .map(User::toBuilder)
					   .map(u -> u.active(true))
					   .map(User.Builder::build)
					   .map(this::update);
	}

	@Override
	public void activateUser(String id)
	{
		setActive(id, true);
	}

	@Override
	public void deactivateUser(String id)
	{
		setActive(id, false);
	}

	private void setActive(String id, boolean active)
	{
		User user = findByIdIfPresent(id).orElseThrow(
				() -> new IllegalArgumentException(String.format("No user found with id [%s].", id)));
		User activated = user.toBuilder().active(active).build();
		update(activated);
	}

	@Override
	public User add(User user) throws UsernameAlreadyExistsException, EmailAlreadyExistsException
	{
		checkUsername(user.getUsername());
		checkEmail(user.getEmail());
		dataService.add(USER, userFactory.create().updateFrom(user));
		return user;
	}

	private void checkEmail(String email) throws EmailAlreadyExistsException
	{
		if (findByEmailIfPresent(email).isPresent())
		{
			throw new EmailAlreadyExistsException(MessageFormat.format("Email ''{0}'' is already registered.", email));
		}
	}

	private void checkUsername(String username) throws UsernameAlreadyExistsException
	{
		if (findByUsernameIfPresent(username).isPresent())
		{
			throw new UsernameAlreadyExistsException(
					MessageFormat.format("Username ''{0}'' already exists.", username));
		}
	}

	@Override
	public Optional<User> connectExistingUser(String username, String googleAccountId)
	{
		return findByUsernameIfPresent(username).map(User::toBuilder)
												.map(u -> u.googleAccountId(googleAccountId))
												.map(User.Builder::build)
												.map(this::update);
	}

	@Override
	public List<User> getAllUsers()
	{
		return dataService.findAll(USER, UserEntity.class).map(UserEntity::toUser).collect(toList());
	}

	@Override
	public Optional<User> findUserById(String userId)
	{
		return Optional.ofNullable(dataService.findOneById(USER, userId, UserEntity.class)).map(UserEntity::toUser);
	}

}
