package org.molgenis.security.user;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.User;
import org.molgenis.auth.UserDecorator;
import org.molgenis.auth.UserAuthority;
import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.data.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.testng.Assert.assertEquals;

public class UserDecoratorTest
{
	private Repository<User> decoratedRepository;
	private Repository<UserAuthority> userAuthorityRepository;
	private UserDecorator userDecorator;
	private PasswordEncoder passwordEncoder;
	private DataService dataService;

	@BeforeMethod
	public void setUp()
	{
		decoratedRepository = mock(Repository.class);
		userAuthorityRepository = mock(Repository.class);
		UserAuthorityFactory userAuthorityFactory = mock(UserAuthorityFactory.class);
		when(userAuthorityFactory.create()).thenAnswer(new Answer<UserAuthority>()
		{
			@Override
			public UserAuthority answer(InvocationOnMock invocation) throws Throwable
			{
				return mock(UserAuthority.class);
			}
		});
		dataService = mock(DataService.class);
		when(dataService.getRepository(USER_AUTHORITY, UserAuthority.class)).thenReturn(userAuthorityRepository);
		passwordEncoder = mock(PasswordEncoder.class);
		userDecorator = new UserDecorator(decoratedRepository, userAuthorityFactory, dataService,
				passwordEncoder);
	}

	@Test
	public void addEntity()
	{
		String password = "password";
		User user = mock(User.class);
		when(user.getPassword()).thenReturn(password);
		when(user.isSuperuser()).thenReturn(false);
		userDecorator.add(user);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(user);
		verify(userAuthorityRepository, times(0)).add(any(UserAuthority.class));
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

		when(decoratedRepository.add(any(Stream.class))).thenAnswer(new Answer<Integer>()
		{
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable
			{
				Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
				List<Entity> entitiesList = entities.collect(toList());
				return entitiesList.size();
			}
		});
		assertEquals(userDecorator.add(Stream.of(user0, user1)), Integer.valueOf(2));
		verify(passwordEncoder, times(2)).encode(password);
		verify(userAuthorityRepository, times(0)).add(any(UserAuthority.class));
	}

	@Test
	public void deleteStream()
	{
		Stream<User> entities = Stream.empty();
		userDecorator.delete(entities);
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
		when(userDecorator.findOneById("1")).thenReturn(currentUser);

		User user = mock(User.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn("password");

		Stream<User> entities = Stream.of(user);
		ArgumentCaptor<Stream<User>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		userDecorator.update(entities);
		assertEquals(captor.getValue().collect(toList()), singletonList(user));
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
		when(userDecorator.findOneById("1")).thenReturn(currentUser);

		User user = mock(User.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn("currentPasswordHash");

		Stream<User> entities = Stream.of(user);
		ArgumentCaptor<Stream<User>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		userDecorator.update(entities);
		assertEquals(captor.getValue().collect(toList()), singletonList(user));
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

		userDecorator.add(user);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(user);
	}

	@Test
	public void findOneObjectFetch()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		userDecorator.findOneById(id, fetch);
		verify(decoratedRepository, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		User user0 = mock(User.class);
		User user1 = mock(User.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(user0, user1));
		Stream<User> expectedEntities = userDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(toList()), asList(user0, user1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		User user0 = mock(User.class);
		User user1 = mock(User.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(user0, user1));
		Stream<User> expectedEntities = userDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(toList()), asList(user0, user1));
	}

	@Test
	public void findAllAsStream()
	{
		User user = mock(User.class);
		Query<User> query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(user));
		Stream<User> entities = userDecorator.findAll(query);
		assertEquals(entities.collect(toList()), singletonList(user));
	}

	@Test
	public void forEachBatchedFetch()
	{
		Fetch fetch = new Fetch();
		Consumer<List<User>> consumer = mock(Consumer.class);
		userDecorator.forEachBatched(fetch, consumer, 234);
		verify(decoratedRepository, times(1)).forEachBatched(fetch, consumer, 234);
	}
}
