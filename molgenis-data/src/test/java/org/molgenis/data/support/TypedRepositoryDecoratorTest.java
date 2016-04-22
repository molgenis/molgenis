package org.molgenis.data.support;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TypedRepositoryDecoratorTest
{
	private Repository<Entity> decoratedRepository;
	private TypedRepositoryDecorator<MolgenisUser> repositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepository = mock(Repository.class);
		repositoryDecorator = new TypedRepositoryDecorator<>(decoratedRepository, MolgenisUser.class);
	}

	@Test
	public void addEntity()
	{
		MolgenisUser entity = mock(MolgenisUser.class);
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
		Stream<MolgenisUser> entities = Stream.of(mock(MolgenisUser.class));
		repositoryDecorator.delete(entities);
		verify(decoratedRepository, times(1)).delete(asUntypedStream(entities));
	}

	@SuppressWarnings(
			{ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		MolgenisUser entity0 = mock(MolgenisUser.class);
		Stream<MolgenisUser> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		repositoryDecorator.update(entities);
		assertEquals(Arrays.asList(entity0), captor.getValue().collect(Collectors.toList()));
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		MolgenisUser entity0 = mock(MolgenisUser.class);
		MolgenisUser entity1 = mock(MolgenisUser.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<MolgenisUser> expectedEntities = repositoryDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		MolgenisUser entity0 = mock(MolgenisUser.class);
		MolgenisUser entity1 = mock(MolgenisUser.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<MolgenisUser> expectedEntities = repositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findOneObjectFetch()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		MolgenisUser entity = mock(MolgenisUser.class);
		when(decoratedRepository.findOneById(id, fetch)).thenReturn(entity);
		assertEquals(entity, repositoryDecorator.findOneById(id, fetch));
		verify(decoratedRepository, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findAllAsStream()
	{
		MolgenisUser entity0 = mock(MolgenisUser.class);
		Query<MolgenisUser> query = mock(Query.class);
		when(decoratedRepository.findAll(asUntypedQuery(query))).thenReturn(Stream.of(entity0));
		Stream<MolgenisUser> entities = repositoryDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void streamFetch()
	{
		Fetch fetch = new Fetch();
		when(decoratedRepository.stream(fetch)).thenReturn(mock(Stream.class));
		repositoryDecorator.stream(fetch);
		verify(decoratedRepository, times(1)).stream(fetch);
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