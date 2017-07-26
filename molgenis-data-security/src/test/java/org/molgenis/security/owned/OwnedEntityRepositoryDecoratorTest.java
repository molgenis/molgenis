package org.molgenis.security.owned;

import org.mockito.ArgumentCaptor;
import org.molgenis.auth.SecurityPackage;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;
import static org.molgenis.security.owned.OwnedEntityType.OWNER_USERNAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class OwnedEntityRepositoryDecoratorTest
{
	private EntityType entityType;
	private Repository<Entity> decoratedRepository;
	private OwnedEntityRepositoryDecorator ownedEntityRepositoryDecorator;
	private ArgumentCaptor<Consumer<List<Entity>>> consumerCaptor;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp()
	{
		entityType = mock(EntityType.class);
		decoratedRepository = mock(Repository.class);
		consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
		when(decoratedRepository.getEntityType()).thenReturn(entityType);
		ownedEntityRepositoryDecorator = new OwnedEntityRepositoryDecorator(decoratedRepository);
	}

	@Test
	public void delegate() throws Exception
	{
		assertEquals(ownedEntityRepositoryDecorator.delegate(), decoratedRepository);
	}

	@Test
	public void query() throws Exception
	{
		assertEquals(ownedEntityRepositoryDecorator.query().getRepository(), ownedEntityRepositoryDecorator);
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
		when(entityType.getExtends()).thenReturn(new OwnedEntityType(mock(SecurityPackage.class)));

		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("0");
		Entity entity1 = mock(Entity.class);
		when(entity1.getIdValue()).thenReturn("0");
		Stream<Entity> entities = Stream.of(entity0, entity1);
		ownedEntityRepositoryDecorator.add(entities);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepository, times(1)).add(captor.capture());
		List<Entity> myEntities = captor.getValue().collect(Collectors.toList());
		assertEquals(myEntities, asList(entity0, entity1));
		verify(entity0, times(1)).set(OwnedEntityType.OWNER_USERNAME, "username");
		verify(entity1, times(1)).set(OwnedEntityType.OWNER_USERNAME, "username");
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
		when(entityType.getExtends()).thenReturn(new OwnedEntityType(mock(SecurityPackage.class)));

		Entity myEntity = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("username").getMock();
		Entity notMyEntity = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("notme").getMock();
		ownedEntityRepositoryDecorator.delete(Stream.of(myEntity, notMyEntity));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepository, times(1)).delete(captor.capture());
		List<Entity> myEntities = captor.getValue().collect(Collectors.toList());
		assertEquals(myEntities, asList(myEntity));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		Entity entity0 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		ownedEntityRepositoryDecorator.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), asList(entity0));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStreamExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityType.getExtends()).thenReturn(new OwnedEntityType(mock(SecurityPackage.class)));

		Entity entity0 = mock(Entity.class);
		when(entity0.get(OwnedEntityType.OWNER_USERNAME)).thenReturn("usernameUpdate");
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		ownedEntityRepositoryDecorator.update(entities);
		List<Entity> entityList = captor.getValue().collect(Collectors.toList());
		assertEquals(entityList, asList(entity0));
		verify(entityList.get(0)).set(OwnedEntityType.OWNER_USERNAME, "username");
	}

	@Test
	public void findOneByIdObjectFetchExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityType.getExtends()).thenReturn(new OwnedEntityType(mock(SecurityPackage.class)));

		Object id = 0;
		Fetch fetch = new Fetch();
		Entity myEntity = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("username").getMock();
		Fetch decoratedFetch = new Fetch().field(OWNER_USERNAME);
		when(decoratedRepository.findOneById(id, decoratedFetch)).thenReturn(myEntity);
		assertEquals(myEntity, ownedEntityRepositoryDecorator.findOneById(id, fetch));
		verify(decoratedRepository, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findOneByIdObjectFetchExtendsOwnedBySomeoneElse()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityType.getExtends()).thenReturn(new OwnedEntityType(mock(SecurityPackage.class)));

		Object id = 0;
		Fetch fetch = new Fetch();
		Entity myEntity = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("notme").getMock();
		Fetch decoratedFetch = new Fetch().field(OWNER_USERNAME);
		when(decoratedRepository.findOneById(id, decoratedFetch)).thenReturn(myEntity);
		assertNull(ownedEntityRepositoryDecorator.findOneById(id, fetch));
		verify(decoratedRepository, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findOneByIdObjectFetchNotExtendsOwned()
	{
		Object id = 0;
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		Fetch decoratedFetch = new Fetch().field(OWNER_USERNAME);
		when(decoratedRepository.findOneById(id, decoratedFetch)).thenReturn(entity);
		ownedEntityRepositoryDecorator.findOneById(id, fetch);
		verify(decoratedRepository, times(1)).findOneById(id, fetch);
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
		assertEquals(expectedEntities.collect(Collectors.toList()), asList(entity0, entity1));
	}

	@Test
	public void findAllStreamExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityType.getExtends()).thenReturn(new OwnedEntityType(mock(SecurityPackage.class)));

		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("username").getMock();
		Entity entity1 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("username").getMock();
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = ownedEntityRepositoryDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), asList(entity0, entity1));
	}

	@Test
	public void findAllStreamExtendsOwnedBySomeoneElse()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityType.getExtends()).thenReturn(new OwnedEntityType(mock(SecurityPackage.class)));

		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("notme").getMock();
		Entity entity1 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("notme").getMock();
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
		Fetch decoratedFetch = new Fetch().field(OWNER_USERNAME);
		when(decoratedRepository.findAll(entityIds, decoratedFetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = ownedEntityRepositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetchExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityType.getExtends()).thenReturn(new OwnedEntityType(mock(SecurityPackage.class)));

		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("username").getMock();
		Entity entity1 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("username").getMock();
		Stream<Object> entityIds = Stream.of(id0, id1);
		Fetch decoratedFetch = new Fetch().field(OWNER_USERNAME);
		when(decoratedRepository.findAll(entityIds, decoratedFetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = ownedEntityRepositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetchExtendsOwnedBySomeoneElse()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityType.getExtends()).thenReturn(new OwnedEntityType(mock(SecurityPackage.class)));

		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("notme").getMock();
		Entity entity1 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("notme").getMock();
		Stream<Object> entityIds = Stream.of(id0, id1);
		Fetch decoratedFetch = new Fetch().field(OWNER_USERNAME);
		when(decoratedRepository.findAll(entityIds, decoratedFetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = ownedEntityRepositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), emptyList());
	}

	@Test
	public void findAllAsStreamNotExtendsOwned()
	{
		Entity entity0 = mock(Entity.class);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = ownedEntityRepositoryDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), asList(entity0));
	}

	@Test
	public void findAllAsStreamExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityType.getExtends()).thenReturn(new OwnedEntityType(mock(SecurityPackage.class)));

		Entity entity0 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("username").getMock();
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = ownedEntityRepositoryDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), asList(entity0));
		verify(query, times(1)).eq(OwnedEntityType.OWNER_USERNAME, "username");
	}

	@Test
	public void consumeBatchedExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityType.getExtends()).thenReturn(new OwnedEntityType(mock(SecurityPackage.class)));

		Fetch fetch = new Fetch();
		Entity entity0 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("username").getMock();
		Entity entity1 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("username").getMock();
		Fetch decoratedFetch = new Fetch().field(OWNER_USERNAME);

		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		ownedEntityRepositoryDecorator.forEachBatched(fetch, consumer, 123);

		verify(decoratedRepository).forEachBatched(eq(decoratedFetch), consumerCaptor.capture(), eq(123));

		consumerCaptor.getValue().accept(asList(entity0, entity1));

		verify(consumer).accept(asList(entity0, entity1));
	}

	@Test
	public void consumeBatchedOwnedBySomeoneElse()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityType.getExtends()).thenReturn(new OwnedEntityType(mock(SecurityPackage.class)));

		Fetch fetch = new Fetch();
		Entity entity0 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("notme").getMock();
		Entity entity1 = when(mock(Entity.class).getString(OWNER_USERNAME)).thenReturn("notme").getMock();
		Fetch decoratedFetch = new Fetch().field(OWNER_USERNAME);

		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		ownedEntityRepositoryDecorator.forEachBatched(fetch, consumer, 123);

		verify(decoratedRepository).forEachBatched(eq(decoratedFetch), consumerCaptor.capture(), eq(123));

		consumerCaptor.getValue().accept(asList(entity0, entity1));

		verify(consumer).accept(emptyList());
	}
}
