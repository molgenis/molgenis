package org.molgenis.security.user;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserDecorator;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.testng.Assert.assertEquals;

public class MolgenisUserDecoratorTest
{
	private Repository<MolgenisUser> decoratedRepository;
	private Repository<UserAuthority> userAuthorityRepository;
	private MolgenisUserDecorator molgenisUserDecorator;
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
		molgenisUserDecorator = new MolgenisUserDecorator(decoratedRepository, userAuthorityFactory, dataService,
				passwordEncoder);
	}

	@Test
	public void addEntity()
	{
		String password = "password";
		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		when(molgenisUser.getPassword()).thenReturn(password);
		when(molgenisUser.isSuperuser()).thenReturn(false);
		molgenisUserDecorator.add(molgenisUser);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(molgenisUser);
		verify(userAuthorityRepository, times(0)).add(any(UserAuthority.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addStream()
	{
		String password = "password";
		MolgenisUser molgenisUser0 = mock(MolgenisUser.class);
		when(molgenisUser0.getPassword()).thenReturn(password);
		when(molgenisUser0.isSuperuser()).thenReturn(false);
		MolgenisUser molgenisUser1 = mock(MolgenisUser.class);
		when(molgenisUser1.getPassword()).thenReturn(password);
		when(molgenisUser1.isSuperuser()).thenReturn(false);

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
		assertEquals(molgenisUserDecorator.add(Stream.of(molgenisUser0, molgenisUser1)), Integer.valueOf(2));
		verify(passwordEncoder, times(2)).encode(password);
		verify(userAuthorityRepository, times(0)).add(any(UserAuthority.class));
	}

	@Test
	public void deleteStream()
	{
		Stream<MolgenisUser> entities = Stream.empty();
		molgenisUserDecorator.delete(entities);
		verify(decoratedRepository, times(1)).delete(entities);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		when(passwordEncoder.encode("password")).thenReturn("passwordHash");

		MolgenisUser currentUser = mock(MolgenisUser.class);
		when(currentUser.getId()).thenReturn("1");
		when(currentUser.getPassword()).thenReturn("currentPasswordHash");
		when(molgenisUserDecorator.findOneById("1")).thenReturn(currentUser);

		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		when(molgenisUser.getId()).thenReturn("1");
		when(molgenisUser.getPassword()).thenReturn("password");

		Stream<MolgenisUser> entities = Stream.of(molgenisUser);
		ArgumentCaptor<Stream<MolgenisUser>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		molgenisUserDecorator.update(entities);
		assertEquals(captor.getValue().collect(toList()), singletonList(molgenisUser));
		verify(molgenisUser).setPassword("passwordHash");
		// TODO add authority tests
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStreamUnchangedPassword()
	{
		when(passwordEncoder.encode("currentPasswordHash")).thenReturn("blaat");

		MolgenisUser currentUser = mock(MolgenisUser.class);
		when(currentUser.getId()).thenReturn("1");
		when(currentUser.getPassword()).thenReturn("currentPasswordHash");
		when(molgenisUserDecorator.findOneById("1")).thenReturn(currentUser);

		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		when(molgenisUser.getId()).thenReturn("1");
		when(molgenisUser.getPassword()).thenReturn("currentPasswordHash");

		Stream<MolgenisUser> entities = Stream.of(molgenisUser);
		ArgumentCaptor<Stream<MolgenisUser>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		molgenisUserDecorator.update(entities);
		assertEquals(captor.getValue().collect(toList()), singletonList(molgenisUser));
		verify(molgenisUser).setPassword("currentPasswordHash");
		// TODO add authority tests
	}

	@Test
	public void addEntitySu()
	{
		String password = "password";
		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		when(molgenisUser.getId()).thenReturn("1");
		when(molgenisUser.getPassword()).thenReturn(password);
		when(molgenisUser.isSuperuser()).thenReturn(true);
		when(decoratedRepository.findOneById("1")).thenReturn(molgenisUser);

		molgenisUserDecorator.add(molgenisUser);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(molgenisUser);
	}

	@Test
	public void findOneObjectFetch()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		molgenisUserDecorator.findOneById(id, fetch);
		verify(decoratedRepository, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		MolgenisUser molgenisUser0 = mock(MolgenisUser.class);
		MolgenisUser molgenisUser1 = mock(MolgenisUser.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(molgenisUser0, molgenisUser1));
		Stream<MolgenisUser> expectedEntities = molgenisUserDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(toList()), asList(molgenisUser0, molgenisUser1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		MolgenisUser molgenisUser0 = mock(MolgenisUser.class);
		MolgenisUser molgenisUser1 = mock(MolgenisUser.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(molgenisUser0, molgenisUser1));
		Stream<MolgenisUser> expectedEntities = molgenisUserDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(toList()), asList(molgenisUser0, molgenisUser1));
	}

	@Test
	public void findAllAsStream()
	{
		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		Query<MolgenisUser> query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(molgenisUser));
		Stream<MolgenisUser> entities = molgenisUserDecorator.findAll(query);
		assertEquals(entities.collect(toList()), singletonList(molgenisUser));
	}

	@Test
	public void forEachBatchedFetch()
	{
		Fetch fetch = new Fetch();
		Consumer<List<MolgenisUser>> consumer = mock(Consumer.class);
		molgenisUserDecorator.forEachBatched(fetch, consumer, 234);
		verify(decoratedRepository, times(1)).forEachBatched(fetch, consumer, 234);
	}
}
