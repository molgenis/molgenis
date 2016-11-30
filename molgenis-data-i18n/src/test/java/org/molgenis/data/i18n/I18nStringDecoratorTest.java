package org.molgenis.data.i18n;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class I18nStringDecoratorTest
{
	private Repository<Entity> decoratedRepo;
	private I18nStringDecorator i18nStringDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		i18nStringDecorator = new I18nStringDecorator(decoratedRepo);
	}

	@Test
	public void testDelegate() throws Exception
	{
		assertEquals(i18nStringDecorator.delegate(), decoratedRepo);
	}

	@Test
	public void testQuery() throws Exception
	{
		assertEquals(i18nStringDecorator.query().getRepository(), i18nStringDecorator);
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(i18nStringDecorator.add(entities), Integer.valueOf(123));
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.empty();
		i18nStringDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		Entity entity0 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepo).update(captor.capture());
		i18nStringDecorator.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0));
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
		Stream<Entity> expectedEntities = i18nStringDecorator.findAll(entityIds);
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
		Stream<Entity> expectedEntities = i18nStringDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllAsStream()
	{
		Entity entity0 = mock(Entity.class);
		Query<Entity> query = mock(Query.class);
		when(decoratedRepo.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = i18nStringDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void forEachBatchedFetch()
	{
		Fetch fetch = new Fetch();
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		i18nStringDecorator.forEachBatched(fetch, consumer, 234);
		verify(decoratedRepo, times(1)).forEachBatched(fetch, consumer, 234);
	}
}
