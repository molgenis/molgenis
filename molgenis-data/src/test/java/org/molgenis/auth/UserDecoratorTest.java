package org.molgenis.auth;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;

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
		decoratedRepository = Mockito.mock(Repository.class);
		userAuthorityRepository = Mockito.mock(Repository.class);
		UserAuthorityFactory userAuthorityFactory = Mockito.mock(UserAuthorityFactory.class);
		Mockito.when(userAuthorityFactory.create()).thenAnswer(new Answer<UserAuthority>()
		{
			@Override
			public UserAuthority answer(InvocationOnMock invocation) throws Throwable
			{
				return Mockito.mock(UserAuthority.class);
			}
		});
		dataService = Mockito.mock(DataService.class);
		Mockito.when(dataService.getRepository(USER_AUTHORITY, UserAuthority.class))
				.thenReturn(userAuthorityRepository);
		passwordEncoder = Mockito.mock(PasswordEncoder.class);
		userDecorator = new UserDecorator(decoratedRepository, userAuthorityFactory, dataService, passwordEncoder);
	}

	@Test
	public void testDelegate() throws Exception
	{
		Assert.assertEquals(userDecorator.delegate(), decoratedRepository);
	}

	@Test
	public void testQuery() throws Exception
	{
		Assert.assertEquals(userDecorator.query().getRepository(), userDecorator);
	}

	@Test
	public void addEntity()
	{
		String password = "password";
		User user = Mockito.mock(User.class);
		Mockito.when(user.getPassword()).thenReturn(password);
		Mockito.when(user.isSuperuser()).thenReturn(false);
		userDecorator.add(user);
		Mockito.verify(passwordEncoder).encode(password);
		Mockito.verify(decoratedRepository).add(user);
		Mockito.verify(userAuthorityRepository, Mockito.times(0)).add(Matchers.any(UserAuthority.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addStream()
	{
		String password = "password";
		User user0 = Mockito.mock(User.class);
		Mockito.when(user0.getPassword()).thenReturn(password);
		Mockito.when(user0.isSuperuser()).thenReturn(false);
		User user1 = Mockito.mock(User.class);
		Mockito.when(user1.getPassword()).thenReturn(password);
		Mockito.when(user1.isSuperuser()).thenReturn(false);

		Mockito.when(decoratedRepository.add(Matchers.any(Stream.class))).thenAnswer(new Answer<Integer>()
		{
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable
			{
				Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
				List<Entity> entitiesList = entities.collect(toList());
				return entitiesList.size();
			}
		});
		Assert.assertEquals(userDecorator.add(Stream.of(user0, user1)), Integer.valueOf(2));
		Mockito.verify(passwordEncoder, Mockito.times(2)).encode(password);
		Mockito.verify(userAuthorityRepository, Mockito.times(0)).add(Matchers.any(UserAuthority.class));
	}

	@Test
	public void deleteStream()
	{
		Stream<User> entities = Stream.empty();
		userDecorator.delete(entities);
		Mockito.verify(decoratedRepository, Mockito.times(1)).delete(entities);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		Mockito.when(passwordEncoder.encode("password")).thenReturn("passwordHash");

		User currentUser = Mockito.mock(User.class);
		Mockito.when(currentUser.getId()).thenReturn("1");
		Mockito.when(currentUser.getPassword()).thenReturn("currentPasswordHash");
		Mockito.when(userDecorator.findOneById("1")).thenReturn(currentUser);

		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn("1");
		Mockito.when(user.getPassword()).thenReturn("password");

		Stream<User> entities = Stream.of(user);
		ArgumentCaptor<Stream<User>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		Mockito.doNothing().when(decoratedRepository).update(captor.capture());
		userDecorator.update(entities);
		Assert.assertEquals(captor.getValue().collect(toList()), singletonList(user));
		Mockito.verify(user).setPassword("passwordHash");
		// TODO add authority tests
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStreamUnchangedPassword()
	{
		Mockito.when(passwordEncoder.encode("currentPasswordHash")).thenReturn("blaat");

		User currentUser = Mockito.mock(User.class);
		Mockito.when(currentUser.getId()).thenReturn("1");
		Mockito.when(currentUser.getPassword()).thenReturn("currentPasswordHash");
		Mockito.when(userDecorator.findOneById("1")).thenReturn(currentUser);

		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn("1");
		Mockito.when(user.getPassword()).thenReturn("currentPasswordHash");

		Stream<User> entities = Stream.of(user);
		ArgumentCaptor<Stream<User>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		Mockito.doNothing().when(decoratedRepository).update(captor.capture());
		userDecorator.update(entities);
		Assert.assertEquals(captor.getValue().collect(toList()), singletonList(user));
		Mockito.verify(user).setPassword("currentPasswordHash");
		// TODO add authority tests
	}

	@Test
	public void addEntitySu()
	{
		String password = "password";
		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn("1");
		Mockito.when(user.getPassword()).thenReturn(password);
		Mockito.when(user.isSuperuser()).thenReturn(true);
		Mockito.when(decoratedRepository.findOneById("1")).thenReturn(user);

		userDecorator.add(user);
		Mockito.verify(passwordEncoder).encode(password);
		Mockito.verify(decoratedRepository).add(user);
	}

	@Test
	public void findOneObjectFetch()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		userDecorator.findOneById(id, fetch);
		Mockito.verify(decoratedRepository, Mockito.times(1)).findOneById(id, fetch);
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		User user0 = Mockito.mock(User.class);
		User user1 = Mockito.mock(User.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		Mockito.when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(user0, user1));
		Stream<User> expectedEntities = userDecorator.findAll(entityIds);
		Assert.assertEquals(expectedEntities.collect(toList()), asList(user0, user1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		User user0 = Mockito.mock(User.class);
		User user1 = Mockito.mock(User.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		Mockito.when(decoratedRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(user0, user1));
		Stream<User> expectedEntities = userDecorator.findAll(entityIds, fetch);
		Assert.assertEquals(expectedEntities.collect(toList()), asList(user0, user1));
	}

	@Test
	public void findAllAsStream()
	{
		User user = Mockito.mock(User.class);
		Query<User> query = Mockito.mock(Query.class);
		Mockito.when(decoratedRepository.findAll(query)).thenReturn(Stream.of(user));
		Stream<User> entities = userDecorator.findAll(query);
		Assert.assertEquals(entities.collect(toList()), singletonList(user));
	}

	@Test
	public void forEachBatchedFetch()
	{
		Fetch fetch = new Fetch();
		Consumer<List<User>> consumer = Mockito.mock(Consumer.class);
		userDecorator.forEachBatched(fetch, consumer, 234);
		Mockito.verify(decoratedRepository, Mockito.times(1)).forEachBatched(fetch, consumer, 234);
	}
}
