package org.molgenis.data;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.*;
import org.molgenis.data.support.EntityWithComputedAttributes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ComputedEntityValuesDecoratorTest
{
	@Mock
	private Repository<Entity> decoratedRepo;
	@Mock
	private Consumer<List<Entity>> consumer;
	private ComputedEntityValuesDecorator computedEntityValuesDecorator;
	@Captor
	private ArgumentCaptor<Consumer<List<Entity>>> consumerArgumentCaptor;
	@Captor
	private ArgumentCaptor<List<Entity>> listArgumentCaptor;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		initMocks(this);
		computedEntityValuesDecorator = new ComputedEntityValuesDecorator(decoratedRepo);
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(computedEntityValuesDecorator.add(entities), Integer.valueOf(123));
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.of(mock(Entity.class));
		computedEntityValuesDecorator.delete(entities);
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
		computedEntityValuesDecorator.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), asList(entity0));
	}

	@Test
	public void findAllStreamNoComputedAttrs()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.hasAttributeWithExpression()).thenReturn(false);
		when(entityMeta.getAtomicAttributes()).thenReturn(emptyList());
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);

		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityMetaData()).thenReturn(entityMeta);
		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityMetaData()).thenReturn(entityMeta);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = computedEntityValuesDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), asList(entity0, entity1));
	}

	@Test
	public void findAllStreamComputedAttrs()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.hasAttributeWithExpression()).thenReturn(true);
		when(entityMeta.getAtomicAttributes()).thenReturn(emptyList());
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);

		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityMetaData()).thenReturn(entityMeta);
		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityMetaData()).thenReturn(entityMeta);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		List<Entity> expectedEntities = computedEntityValuesDecorator.findAll(entityIds).collect(Collectors.toList());
		assertEquals(expectedEntities.size(), 2);
		assertEquals(expectedEntities.get(0).getClass(), EntityWithComputedAttributes.class);
		assertEquals(expectedEntities.get(1).getClass(), EntityWithComputedAttributes.class);
	}

	@Test
	public void findAllStreamFetchNoComputedAttrs()
	{
		Fetch fetch = new Fetch();
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.hasAttributeWithExpression()).thenReturn(false);
		when(entityMeta.getAtomicAttributes()).thenReturn(emptyList());
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);

		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityMetaData()).thenReturn(entityMeta);
		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityMetaData()).thenReturn(entityMeta);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = computedEntityValuesDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetchComputedAttrs()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.hasAttributeWithExpression()).thenReturn(true);
		when(entityMeta.getAtomicAttributes()).thenReturn(emptyList());
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);

		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityMetaData()).thenReturn(entityMeta);
		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityMetaData()).thenReturn(entityMeta);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		List<Entity> expectedEntities = computedEntityValuesDecorator.findAll(entityIds, fetch)
				.collect(Collectors.toList());
		assertEquals(expectedEntities.size(), 2);
		assertEquals(expectedEntities.get(0).getClass(), EntityWithComputedAttributes.class);
		assertEquals(expectedEntities.get(1).getClass(), EntityWithComputedAttributes.class);
	}

	@Test
	public void findAllAsStreamNoComputedAttrs()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.hasAttributeWithExpression()).thenReturn(false);
		when(entityMeta.getAtomicAttributes()).thenReturn(emptyList());
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);

		Entity entity0 = mock(Entity.class);
		Query<Entity> query = mock(Query.class);
		when(decoratedRepo.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = computedEntityValuesDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), asList(entity0));
	}

	@Test
	public void findAllAsStreamComputedAttrs()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.hasAttributeWithExpression()).thenReturn(true);
		when(entityMeta.getAtomicAttributes()).thenReturn(emptyList());
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);

		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityMetaData()).thenReturn(entityMeta);
		Query<Entity> query = mock(Query.class);
		when(decoratedRepo.findAll(query)).thenReturn(Stream.of(entity0));
		List<Entity> expectedEntities = computedEntityValuesDecorator.findAll(query).collect(Collectors.toList());
		assertEquals(expectedEntities.size(), 1);
		assertEquals(expectedEntities.get(0).getClass(), EntityWithComputedAttributes.class);
	}

	@Test
	public void streamFetchNoComputedAttrs()
	{
		Fetch fetch = new Fetch();
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.hasAttributeWithExpression()).thenReturn(false);
		when(entityMeta.getAtomicAttributes()).thenReturn(emptyList());
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);

		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityMetaData()).thenReturn(entityMeta);
		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityMetaData()).thenReturn(entityMeta);

		Consumer<List<Entity>> consumer = mock(Consumer.class);
		computedEntityValuesDecorator.forEachBatched(fetch, consumer, 1234);
		verify(decoratedRepo).forEachBatched(eq(fetch), consumerArgumentCaptor.capture(), eq(1234));

		consumerArgumentCaptor.getValue().accept(asList(entity0, entity1));

		verify(consumer).accept(listArgumentCaptor.capture());
		assertEquals(listArgumentCaptor.getValue(), asList(entity0, entity1));
	}

	@Test
	public void streamFetchComputedAttrs()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.hasAttributeWithExpression()).thenReturn(true);
		when(entityMeta.getAtomicAttributes()).thenReturn(emptyList());
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);

		Fetch fetch = new Fetch();
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityMetaData()).thenReturn(entityMeta);
		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityMetaData()).thenReturn(entityMeta);

		computedEntityValuesDecorator.forEachBatched(fetch, consumer, 1234);
		verify(decoratedRepo).forEachBatched(eq(fetch), consumerArgumentCaptor.capture(), eq(1234));

		consumerArgumentCaptor.getValue().accept(asList(entity0, entity1));

		verify(consumer).accept(listArgumentCaptor.capture());
		assertTrue(listArgumentCaptor.getValue().stream().allMatch(e -> e instanceof EntityWithComputedAttributes));
	}
}
