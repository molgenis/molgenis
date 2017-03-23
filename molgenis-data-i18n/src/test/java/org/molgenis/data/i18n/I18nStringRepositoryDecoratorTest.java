package org.molgenis.data.i18n;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.model.I18nString;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class I18nStringRepositoryDecoratorTest
{
	private Repository<I18nString> decoratedRepo;
	private I18nStringRepositoryDecorator i18NStringRepositoryDecorator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		i18NStringRepositoryDecorator = new I18nStringRepositoryDecorator(decoratedRepo);
	}

	@Test
	public void testDelegate() throws Exception
	{
		assertEquals(i18NStringRepositoryDecorator.delegate(), decoratedRepo);
	}

	@Test
	public void testQuery() throws Exception
	{
		assertEquals(i18NStringRepositoryDecorator.query().getRepository(), i18NStringRepositoryDecorator);
	}

	@Test
	public void addStream()
	{
		Stream<I18nString> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(i18NStringRepositoryDecorator.add(entities), Integer.valueOf(123));
	}

	@Test
	public void deleteStream()
	{
		Stream<I18nString> entities = Stream.empty();
		i18NStringRepositoryDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		I18nString entity0 = mock(I18nString.class);
		Stream<I18nString> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<I18nString>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepo).update(captor.capture());
		i18NStringRepositoryDecorator.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		I18nString entity0 = mock(I18nString.class);
		I18nString entity1 = mock(I18nString.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<I18nString> expectedEntities = i18NStringRepositoryDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		I18nString entity0 = mock(I18nString.class);
		I18nString entity1 = mock(I18nString.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<I18nString> expectedEntities = i18NStringRepositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllAsStream()
	{
		I18nString entity0 = mock(I18nString.class);
		@SuppressWarnings("unchecked")
		Query<I18nString> query = mock(Query.class);
		when(decoratedRepo.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<I18nString> entities = i18NStringRepositoryDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void forEachBatchedFetch()
	{
		Fetch fetch = new Fetch();
		@SuppressWarnings("unchecked")
		Consumer<List<I18nString>> consumer = mock(Consumer.class);
		i18NStringRepositoryDecorator.forEachBatched(fetch, consumer, 234);
		verify(decoratedRepo, times(1)).forEachBatched(fetch, consumer, 234);
	}
}
