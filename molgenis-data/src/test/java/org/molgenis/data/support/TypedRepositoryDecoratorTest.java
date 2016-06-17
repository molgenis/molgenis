package org.molgenis.data.support;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import autovalue.shaded.com.google.common.common.collect.Lists;
import org.mockito.*;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TypedRepositoryDecoratorTest
{
	@Mock
	private Repository<Entity> decoratedRepository;
	private TypedRepositoryDecorator<MolgenisUser> repositoryDecorator;
	@Captor
	private ArgumentCaptor<Consumer<List<Entity>>> untypedConsumerArgumentCaptor;
	@Mock
	private Consumer<List<MolgenisUser>> typedConsumer;
	@Mock
	private Query<MolgenisUser> query;
	@Mock
	MolgenisUser entity;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		initMocks(this);
		repositoryDecorator = new TypedRepositoryDecorator<>(decoratedRepository, MolgenisUser.class);
	}

	@Test
	public void addEntity()
	{
		repositoryDecorator.add(entity);
		verify(decoratedRepository, times(1)).add(entity);
	}

	@Test
	public void addStream()
	{
		Stream<MolgenisUser> entities = Stream.empty();
		when(decoratedRepository.add(asUntypedStream(entities))).thenReturn(123);
		assertEquals(repositoryDecorator.add(entities), Integer.valueOf(123));
	}

	@Test
	public void deleteStream()
	{
		Stream<MolgenisUser> entities = Stream.of(entity);
		repositoryDecorator.delete(entities);
		verify(decoratedRepository, times(1)).delete(asUntypedStream(entities));
	}

	@SuppressWarnings(
			{ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		Stream<MolgenisUser> entities = Stream.of(entity);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		repositoryDecorator.update(entities);
		assertEquals(Arrays.asList(entity), captor.getValue().collect(Collectors.toList()));
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		MolgenisUser entity = mock(MolgenisUser.class);
		MolgenisUser entity1 = mock(MolgenisUser.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity, entity1));
		Stream<MolgenisUser> expectedEntities = repositoryDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity, entity1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		MolgenisUser entity1 = mock(MolgenisUser.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(entity, entity1));
		Stream<MolgenisUser> expectedEntities = repositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity, entity1));
	}

	@Test
	public void findOneObjectFetch()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		when(decoratedRepository.findOneById(id, fetch)).thenReturn(entity);
		assertEquals(entity, repositoryDecorator.findOneById(id, fetch));
		verify(decoratedRepository, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findAllAsStream()
	{
		when(decoratedRepository.findAll(asUntypedQuery(query))).thenReturn(Stream.of(entity));
		Stream<MolgenisUser> entities = repositoryDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity));
	}

	@Test
	public void forEachBatchedFetch()
	{
		Fetch fetch = new Fetch();


		// the test
		repositoryDecorator.forEachBatched(fetch, typedConsumer, 234);

		verify(decoratedRepository, times(1)).forEachBatched(eq(fetch), untypedConsumerArgumentCaptor.capture(), eq(234));

		Consumer<List<Entity>> untypedConsumer = untypedConsumerArgumentCaptor.getValue();
		DataService dataService = mock(DataService.class);
		DefaultEntity untypedEntity = new DefaultEntity(new MolgenisUserMetaData(), dataService);
		untypedEntity.set(MolgenisUser.USERNAME, "abcd");
		untypedConsumer.accept(Lists.newArrayList(untypedEntity));

		ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
		verify(typedConsumer).accept(listCaptor.capture());
		MolgenisUser user = (MolgenisUser) listCaptor.getValue().get(0);
		assertEquals("abcd", user.getUsername());
	}

	@SuppressWarnings("unchecked")
	private Query<Entity> asUntypedQuery(Query<MolgenisUser> typedQuery)
	{
		return (Query<Entity>) (Query) typedQuery;
	}

	@SuppressWarnings("unchecked")
	private Stream<Entity> asUntypedStream(Stream<MolgenisUser> typedEntities)
	{
		return (Stream<Entity>) (Stream) typedEntities;
	}
}