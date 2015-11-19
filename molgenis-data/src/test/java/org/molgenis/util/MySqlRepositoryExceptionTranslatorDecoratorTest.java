package org.molgenis.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

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

	@Test(expectedExceptions = MolgenisDataException.class)
	public void addIterableextendsEntityWithUncategorizedSQLException()
	{
		Exception e = new UncategorizedSQLException("", "",
				new SQLException("", "", SQLExceptionTranslatorTemplate.MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE));
		List<Entity> entities = Arrays.asList(new MapEntity());

		Mockito.doThrow(e).when(decoratedRepo).add(entities);
		decorator.add(entities);
	}

	@Test
	public void addIterableextendsEntityNoException()
	{
		List<Entity> entities = Arrays.asList(new MapEntity());
		decorator.add(entities);
		Mockito.verify(decoratedRepo).add(entities);
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

	@Test(expectedExceptions = MolgenisDataException.class)
	public void updateIterableextendsEntityWithUncategorizedSQLException()
	{
		Exception e = new UncategorizedSQLException("", "",
				new SQLException("", "", SQLExceptionTranslatorTemplate.MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE));
		List<Entity> entities = Arrays.asList(new MapEntity());

		Mockito.doThrow(e).when(decoratedRepo).update(entities);
		decorator.update(entities);
	}

	@Test
	public void updateIterableextendsEntityNoException()
	{
		List<Entity> entities = Arrays.asList(new MapEntity());
		decorator.update(entities);
		Mockito.verify(decoratedRepo).update(entities);
	}

	@Test
	public void findAllIterableFetch()
	{
		Iterable<Object> ids = Arrays.<Object> asList(Integer.valueOf(0), Integer.valueOf(1));
		Fetch fetch = new Fetch();
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Iterable<Entity> entities = Arrays.asList(entity0, entity1);
		when(decoratedRepo.findAll(ids, fetch)).thenReturn(entities);
		assertEquals(Arrays.asList(entity0, entity1), Lists.newArrayList(decorator.findAll(ids, fetch)));
		verify(decoratedRepo, times(1)).findAll(ids, fetch);
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
}
