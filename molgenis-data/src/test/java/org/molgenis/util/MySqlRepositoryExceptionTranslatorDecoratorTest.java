package org.molgenis.util;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MySqlRepositoryExceptionTranslatorDecoratorTest
{
	private MySqlRepositoryExceptionTranslatorDecorator decorator;
	private Repository decoratedRepo;

	@BeforeMethod
	public void beforeMethod()
	{
		decoratedRepo = Mockito.mock(Repository.class);
		decorator = new MySqlRepositoryExceptionTranslatorDecorator(decoratedRepo);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void addEntityWithUncategorizedSQLException()
	{
		Exception e = new UncategorizedSQLException("", "",
				new SQLException("", "", SQLExceptionTranslatorTemplate.MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE));
		Entity entity = new MapEntity();

		Mockito.doThrow(e).when(decoratedRepo).add(entity);
		decorator.add(entity);
	}

	@Test
	public void addEntityNoException()
	{
		Entity entity = new MapEntity();
		decorator.add(entity);
		Mockito.verify(decoratedRepo).add(entity);
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(decorator.add(entities), Integer.valueOf(123));
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void addStreamExceptionTranslation()
	{
		Exception e = new UncategorizedSQLException("", "",
				new SQLException("", "", SQLExceptionTranslatorTemplate.MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE));
		Stream<Entity> entities = Stream.empty();
		Mockito.doThrow(e).when(decoratedRepo).add(entities);
		decorator.add(entities);
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.empty();
		decorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@Test
	public void updateStream()
	{
		Entity entity0 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepo).update(captor.capture());
		decorator.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void updateStreamExceptionTranslation()
	{
		Exception e = new UncategorizedSQLException("", "",
				new SQLException("", "", SQLExceptionTranslatorTemplate.MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE));
		Stream<Entity> entities = Stream.empty();
		Mockito.doThrow(e).when(decoratedRepo).update(entities);
		decorator.update(entities);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void updateEntityWithUncategorizedSQLException()
	{
		Exception e = new UncategorizedSQLException("", "",
				new SQLException("", "", SQLExceptionTranslatorTemplate.MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE));
		Entity entity = new MapEntity();

		Mockito.doThrow(e).when(decoratedRepo).update(entity);
		decorator.update(entity);
	}

	@Test
	public void updateEntityNoException()
	{
		Entity entity = new MapEntity();
		decorator.update(entity);
		Mockito.verify(decoratedRepo).update(entity);
	}

	@Test
	public void findOne()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(decoratedRepo.findOne(id, fetch)).thenReturn(entity);
		assertEquals(entity, decorator.findOne(id, fetch));
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
		Stream<Entity> expectedEntities = decorator.findAll(entityIds);
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
		Stream<Entity> expectedEntities = decorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllAsStream()
	{
		Entity entity0 = mock(Entity.class);
		Query query = mock(Query.class);
		when(decoratedRepo.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = decorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}
}
