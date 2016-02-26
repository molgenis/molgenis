package org.molgenis.security.owned;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.support.OwnedEntityMetaData.ATTR_OWNER_USERNAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OwnedEntityRepositoryDecoratorTest
{
	private EntityMetaData entityMeta;
	private Repository decoratedRepository;
	private OwnedEntityRepositoryDecorator ownedEntityRepositoryDecorator;

	@BeforeMethod
	public void setUp()
	{
		entityMeta = mock(EntityMetaData.class);
		decoratedRepository = mock(Repository.class);
		when(decoratedRepository.getEntityMetaData()).thenReturn(entityMeta);
		ownedEntityRepositoryDecorator = new OwnedEntityRepositoryDecorator(decoratedRepository);
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.of(mock(Entity.class));
		ownedEntityRepositoryDecorator.add(entities);
		verify(decoratedRepository, times(1)).add(entities);
	}

	@Test
	public void addStreamExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("0");
		Entity entity1 = mock(Entity.class);
		when(entity1.getIdValue()).thenReturn("0");
		Stream<Entity> entities = Stream.of(entity0, entity1);
		ownedEntityRepositoryDecorator.add(entities);

		@SuppressWarnings(
		{ "unchecked", "rawtypes" })
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepository, times(1)).add(captor.capture());
		List<Entity> myEntities = captor.getValue().collect(Collectors.toList());
		assertEquals(myEntities, Arrays.asList(entity0, entity1));
		verify(entity0, times(1)).set(OwnedEntityMetaData.ATTR_OWNER_USERNAME, "username");
		verify(entity1, times(1)).set(OwnedEntityMetaData.ATTR_OWNER_USERNAME, "username");
	}

	@Test
	public void deleteStream()
	{
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0, entity1);
		ownedEntityRepositoryDecorator.delete(entities);
		verify(decoratedRepository, times(1)).delete(entities);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void deleteStreamEntityExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Entity myEntity = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("username").getMock();
		Entity notMyEntity = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("notme").getMock();
		ownedEntityRepositoryDecorator.delete(Stream.of(myEntity, notMyEntity));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepository, times(1)).delete(captor.capture());
		List<Entity> myEntities = captor.getValue().collect(Collectors.toList());
		assertEquals(myEntities, Arrays.asList(myEntity));
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		Entity entity0 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		ownedEntityRepositoryDecorator.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void updateStreamExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Entity entity0 = mock(Entity.class);
		when(entity0.get(OwnedEntityMetaData.ATTR_OWNER_USERNAME)).thenReturn("usernameUpdate");
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		ownedEntityRepositoryDecorator.update(entities);
		List<Entity> entityList = captor.getValue().collect(Collectors.toList());
		assertEquals(entityList, Arrays.asList(entity0));
		verify(entityList.get(0)).set(OwnedEntityMetaData.ATTR_OWNER_USERNAME, "username");
	}

	@Test
	public void findOneObjectFetchExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity myEntity = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("username").getMock();
		Fetch decoratedFetch = new Fetch().field(ATTR_OWNER_USERNAME);
		when(decoratedRepository.findOne(id, decoratedFetch)).thenReturn(myEntity);
		assertEquals(myEntity, ownedEntityRepositoryDecorator.findOne(id, fetch));
		verify(decoratedRepository, times(1)).findOne(id, fetch);
	}

	@Test
	public void findOneObjectFetchExtendsOwnedBySomeoneElse()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity myEntity = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("notme").getMock();
		Fetch decoratedFetch = new Fetch().field(ATTR_OWNER_USERNAME);
		when(decoratedRepository.findOne(id, decoratedFetch)).thenReturn(myEntity);
		assertNull(ownedEntityRepositoryDecorator.findOne(id, fetch));
		verify(decoratedRepository, times(1)).findOne(id, fetch);
	}

	@Test
	public void findOneObjectFetchNotExtendsOwned()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		Fetch decoratedFetch = new Fetch().field(ATTR_OWNER_USERNAME);
		when(decoratedRepository.findOne(id, decoratedFetch)).thenReturn(entity);
		ownedEntityRepositoryDecorator.findOne(id, fetch);
		verify(decoratedRepository, times(1)).findOne(id, fetch);
	}

	@Test
	public void findAllStreamNotExtendsOwned()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = ownedEntityRepositoryDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("username").getMock();
		Entity entity1 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("username").getMock();
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = ownedEntityRepositoryDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamExtendsOwnedBySomeoneElse()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("notme").getMock();
		Entity entity1 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("notme").getMock();
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = ownedEntityRepositoryDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), emptyList());
	}

	@Test
	public void findAllStreamFetchNotExtendsOwned()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		Fetch decoratedFetch = new Fetch().field(ATTR_OWNER_USERNAME);
		when(decoratedRepository.findAll(entityIds, decoratedFetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = ownedEntityRepositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetchExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("username").getMock();
		Entity entity1 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("username").getMock();
		Stream<Object> entityIds = Stream.of(id0, id1);
		Fetch decoratedFetch = new Fetch().field(ATTR_OWNER_USERNAME);
		when(decoratedRepository.findAll(entityIds, decoratedFetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = ownedEntityRepositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetchExtendsOwnedBySomeoneElse()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("notme").getMock();
		Entity entity1 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("notme").getMock();
		Stream<Object> entityIds = Stream.of(id0, id1);
		Fetch decoratedFetch = new Fetch().field(ATTR_OWNER_USERNAME);
		when(decoratedRepository.findAll(entityIds, decoratedFetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = ownedEntityRepositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), emptyList());
	}

	@Test
	public void findAllAsStreamNotExtendsOwned()
	{
		Entity entity0 = mock(Entity.class);
		Query query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = ownedEntityRepositoryDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void findAllAsStreamExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Entity entity0 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("username").getMock();
		Query query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = ownedEntityRepositoryDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
		verify(query, times(1)).eq(OwnedEntityMetaData.ATTR_OWNER_USERNAME, "username");
	}

	@Test
	public void streamFetchExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Fetch fetch = new Fetch();
		Entity entity0 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("username").getMock();
		Entity entity1 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("username").getMock();
		Fetch decoratedFetch = new Fetch().field(ATTR_OWNER_USERNAME);
		when(decoratedRepository.stream(decoratedFetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = ownedEntityRepositoryDecorator.stream(fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void streamFetchExtendsOwnedBySomeoneElse()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Fetch fetch = new Fetch();
		Entity entity0 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("notme").getMock();
		Entity entity1 = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("notme").getMock();
		Fetch decoratedFetch = new Fetch().field(ATTR_OWNER_USERNAME);
		when(decoratedRepository.stream(decoratedFetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = ownedEntityRepositoryDecorator.stream(fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), emptyList());
	}
}
