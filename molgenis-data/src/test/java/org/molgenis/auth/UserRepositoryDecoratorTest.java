package org.molgenis.auth;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;

public class UserRepositoryDecoratorTest
{
	private Repository<User> decoratedRepository;
	private Repository<UserAuthority> userAuthorityRepository;
	private UserRepositoryDecorator userRepositoryDecorator;
	private PasswordEncoder passwordEncoder;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp()
	{
		decoratedRepository = mock(Repository.class);
		userAuthorityRepository = mock(Repository.class);
		UserAuthorityFactory userAuthorityFactory = mock(UserAuthorityFactory.class);
		when(userAuthorityFactory.create()).thenAnswer(invocation -> mock(UserAuthority.class));
		DataService dataService = mock(DataService.class);
		when(dataService.getRepository(USER_AUTHORITY, UserAuthority.class)).thenReturn(userAuthorityRepository);
		passwordEncoder = mock(PasswordEncoder.class);
		userRepositoryDecorator = new UserRepositoryDecorator(decoratedRepository, userAuthorityFactory, dataService,
				passwordEncoder);
	}

	@Test
	public void testDelegate() throws Exception
	{
		Assert.assertEquals(userRepositoryDecorator.delegate(), decoratedRepository);
	}

	@Test
	public void addEntity()
	{
		String password = "password";
		User user = mock(User.class);
		when(user.getPassword()).thenReturn(password);
		when(user.isSuperuser()).thenReturn(false);
		userRepositoryDecorator.add(user);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(user);
		verify(userAuthorityRepository, times(0)).add(Matchers.any(UserAuthority.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addStream()
	{
		String password = "password";
		User user0 = mock(User.class);
		when(user0.getPassword()).thenReturn(password);
		when(user0.isSuperuser()).thenReturn(false);
		User user1 = mock(User.class);
		when(user1.getPassword()).thenReturn(password);
		when(user1.isSuperuser()).thenReturn(false);

		when(decoratedRepository.add(Matchers.any(Stream.class))).thenAnswer(invocation ->
		{
			Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
			List<Entity> entitiesList = entities.collect(toList());
			return entitiesList.size();
		});
		Assert.assertEquals(userRepositoryDecorator.add(Stream.of(user0, user1)), Integer.valueOf(2));
		verify(passwordEncoder, times(2)).encode(password);
		verify(userAuthorityRepository, times(0)).add(Matchers.any(UserAuthority.class));
	}

	@Test
	public void deleteStream()
	{
		Stream<User> entities = Stream.empty();
		userRepositoryDecorator.delete(entities);
		verify(decoratedRepository, times(1)).delete(entities);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		when(passwordEncoder.encode("password")).thenReturn("passwordHash");

		User currentUser = mock(User.class);
		when(currentUser.getId()).thenReturn("1");
		when(currentUser.getPassword()).thenReturn("currentPasswordHash");
		when(userRepositoryDecorator.findOneById("1")).thenReturn(currentUser);

		User user = mock(User.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn("password");

		Stream<User> entities = Stream.of(user);
		ArgumentCaptor<Stream<User>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		userRepositoryDecorator.update(entities);
		Assert.assertEquals(captor.getValue().collect(toList()), singletonList(user));
		verify(user).setPassword("passwordHash");
		// TODO add authority tests
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStreamUnchangedPassword()
	{
		when(passwordEncoder.encode("currentPasswordHash")).thenReturn("blaat");

		User currentUser = mock(User.class);
		when(currentUser.getId()).thenReturn("1");
		when(currentUser.getPassword()).thenReturn("currentPasswordHash");
		when(userRepositoryDecorator.findOneById("1")).thenReturn(currentUser);

		User user = mock(User.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn("currentPasswordHash");

		Stream<User> entities = Stream.of(user);
		ArgumentCaptor<Stream<User>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		userRepositoryDecorator.update(entities);
		Assert.assertEquals(captor.getValue().collect(toList()), singletonList(user));
		verify(user).setPassword("currentPasswordHash");
		// TODO add authority tests
	}

	@Test
	public void addEntitySu()
	{
		String password = "password";
		User user = mock(User.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn(password);
		when(user.isSuperuser()).thenReturn(true);
		when(decoratedRepository.findOneById("1")).thenReturn(user);

		userRepositoryDecorator.add(user);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(user);
	}
}
