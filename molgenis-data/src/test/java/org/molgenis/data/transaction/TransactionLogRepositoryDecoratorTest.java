package org.molgenis.data.transaction;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TransactionLogRepositoryDecoratorTest
{
	private Repository decoratedRepo;
	private EntityMetaData entityMeta;
	private TransactionLogService transactionLogService;
	private TransactionLogRepositoryDecorator transactionLogRepositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		when(decoratedRepo.getName()).thenReturn("entity");
		entityMeta = mock(EntityMetaData.class);
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);
		transactionLogService = mock(TransactionLogService.class);
		transactionLogRepositoryDecorator = new TransactionLogRepositoryDecorator(decoratedRepo, transactionLogService);
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(transactionLogRepositoryDecorator.add(entities), Integer.valueOf(123));
		verify(transactionLogService).log(entityMeta, MolgenisTransactionLogEntryMetaData.Type.ADD);
	}

	@Test
	public void addStreamExcludedEntity()
	{
		when(decoratedRepo.getName()).thenReturn(MolgenisTransactionLogEntryMetaData.ENTITY_NAME);

		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(transactionLogRepositoryDecorator.add(entities), Integer.valueOf(123));
		verifyZeroInteractions(transactionLogService);
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.empty();
		transactionLogRepositoryDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
		verify(transactionLogService, times(1)).log(entityMeta, MolgenisTransactionLogEntryMetaData.Type.DELETE);
	}

	@Test
	public void deleteStreamExcludedEntity()
	{
		when(decoratedRepo.getName()).thenReturn(MolgenisTransactionLogEntryMetaData.ENTITY_NAME);

		Stream<Entity> entities = Stream.empty();
		transactionLogRepositoryDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
		verifyZeroInteractions(transactionLogService);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		Entity entity0 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepo).update(captor.capture());
		transactionLogRepositoryDecorator.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0));
		verify(transactionLogService).log(entityMeta, MolgenisTransactionLogEntryMetaData.Type.UPDATE);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void updateStreamExcludedEntity()
	{
		when(decoratedRepo.getName()).thenReturn(MolgenisTransactionLogEntryMetaData.ENTITY_NAME);

		Entity entity0 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepo).update(captor.capture());
		transactionLogRepositoryDecorator.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0));
		verifyZeroInteractions(transactionLogService);
	}

	@Test
	public void findOne()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(decoratedRepo.findOne(id, fetch)).thenReturn(entity);
		assertEquals(entity, transactionLogRepositoryDecorator.findOne(id, fetch));
		verify(decoratedRepo, times(1)).findOne(id, fetch);
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = transactionLogRepositoryDecorator.findAll(entityIds);
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
		when(decoratedRepo.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = transactionLogRepositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllAsStream()
	{
		Entity entity0 = mock(Entity.class);
		Query query = mock(Query.class);
		when(decoratedRepo.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = transactionLogRepositoryDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void streamFetch()
	{
		Fetch fetch = new Fetch();
		transactionLogRepositoryDecorator.stream(fetch);
		verify(decoratedRepo, times(1)).stream(fetch);
	}
}
