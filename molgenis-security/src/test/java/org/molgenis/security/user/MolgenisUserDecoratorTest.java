package org.molgenis.security.user;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserDecorator;
//import org.molgenis.auth.UserAuthorityRepository;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisUserDecoratorTest
{
	private Repository decoratedRepository;
	private Repository userAuthorityRepository;
	private MolgenisUserDecorator molgenisUserDecorator;
	private PasswordEncoder passwordEncoder;

	@BeforeMethod
	public void setUp()
	{
		decoratedRepository = mock(Repository.class);
		userAuthorityRepository = mock(Repository.class);
		molgenisUserDecorator = new MolgenisUserDecorator(decoratedRepository);
		ApplicationContext ctx = mock(ApplicationContext.class);
		passwordEncoder = mock(PasswordEncoder.class);
		when(ctx.getBean(PasswordEncoder.class)).thenReturn(passwordEncoder);
		DataService dataService = mock(DataService.class);
		when(dataService.getRepository(UserAuthority.class.getSimpleName())).thenReturn(userAuthorityRepository);
		when(ctx.getBean(DataService.class)).thenReturn(dataService);
		new ApplicationContextProvider().setApplicationContext(ctx);
	}

	@Test
	public void addEntity()
	{
		String password = "password";
		Entity entity = new MapEntity();
		entity.set(MolgenisUser.PASSWORD_, password);
		entity.set(MolgenisUser.SUPERUSER, false);
		molgenisUserDecorator.add(entity);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(entity);
		verify(userAuthorityRepository, times(0)).add(any(UserAuthority.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addStream()
	{
		String password = "password";
		Entity entity0 = new MapEntity();
		entity0.set(MolgenisUser.PASSWORD_, password);
		entity0.set(MolgenisUser.SUPERUSER, false);
		Entity entity1 = new MapEntity();
		entity1.set(MolgenisUser.PASSWORD_, password);
		entity1.set(MolgenisUser.SUPERUSER, false);

		when(decoratedRepository.add(any(Stream.class))).thenAnswer(new Answer<Integer>()
		{
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable
			{
				Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
				List<Entity> entitiesList = entities.collect(Collectors.toList());
				return entitiesList.size();
			}
		});
		assertEquals(molgenisUserDecorator.add(Stream.of(entity0, entity1)), Integer.valueOf(2));
		verify(passwordEncoder, times(2)).encode(password);
		verify(userAuthorityRepository, times(0)).add(any(UserAuthority.class));
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.empty();
		molgenisUserDecorator.delete(entities);
		verify(decoratedRepository, times(1)).delete(entities);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		Entity entity0 = mock(Entity.class);
		entity0.set(MolgenisUser.PASSWORD_, "password");
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		decoratedRepository.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0));
		verify(entity0).set(eq(MolgenisUser.PASSWORD_), anyString());
		// TODO add authority tests
	}

	@Test
	public void addEntitySu()
	{
		String password = "password";
		Entity entity = new MapEntity("id");
		entity.set("id", 1);
		entity.set(MolgenisUser.PASSWORD_, password);
		entity.set(MolgenisUser.SUPERUSER, true);
		when(decoratedRepository.findOne(1)).thenReturn(entity);

		molgenisUserDecorator.add(entity);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(entity);
		//verify(userAuthorityRepository, times(1)).add(any(UserAuthority.class));
	}

	@Test
	public void findOneObjectFetch()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		molgenisUserDecorator.findOne(id, fetch);
		verify(decoratedRepository, times(1)).findOne(id, fetch);
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = molgenisUserDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = molgenisUserDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllAsStream()
	{
		Entity entity0 = mock(Entity.class);
		Query query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = molgenisUserDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}
}
