package org.molgenis.util;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MySqlRepositoryExceptionTranslatorDecoratorTest
{
	private MySqlRepositoryExceptionTranslatorDecorator decorator;
	private Repository repository;

	@BeforeMethod
	public void beforeMethod()
	{
		repository = Mockito.mock(Repository.class);
		decorator = new MySqlRepositoryExceptionTranslatorDecorator(repository);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void addEntityWithUncategorizedSQLException()
	{
		Exception e = new UncategorizedSQLException("", "",
				new SQLException("", "", SQLExceptionTranslatorTemplate.MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE));
		Entity entity = new MapEntity();

		Mockito.doThrow(e).when(repository).add(entity);
		decorator.add(entity);
	}

	@Test
	public void addEntityNoException()
	{
		Entity entity = new MapEntity();
		decorator.add(entity);
		Mockito.verify(repository).add(entity);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void addIterableextendsEntityWithUncategorizedSQLException()
	{
		Exception e = new UncategorizedSQLException("", "",
				new SQLException("", "", SQLExceptionTranslatorTemplate.MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE));
		List<Entity> entities = Arrays.asList(new MapEntity());

		Mockito.doThrow(e).when(repository).add(entities);
		decorator.add(entities);
	}

	@Test
	public void addIterableextendsEntityNoException()
	{
		List<Entity> entities = Arrays.asList(new MapEntity());
		decorator.add(entities);
		Mockito.verify(repository).add(entities);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void updateEntityWithUncategorizedSQLException()
	{
		Exception e = new UncategorizedSQLException("", "",
				new SQLException("", "", SQLExceptionTranslatorTemplate.MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE));
		Entity entity = new MapEntity();

		Mockito.doThrow(e).when(repository).update(entity);
		decorator.update(entity);
	}

	@Test
	public void updateEntityNoException()
	{
		Entity entity = new MapEntity();
		decorator.update(entity);
		Mockito.verify(repository).update(entity);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void updateIterableextendsEntityWithUncategorizedSQLException()
	{
		Exception e = new UncategorizedSQLException("", "",
				new SQLException("", "", SQLExceptionTranslatorTemplate.MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE));
		List<Entity> entities = Arrays.asList(new MapEntity());

		Mockito.doThrow(e).when(repository).update(entities);
		decorator.update(entities);
	}

	@Test
	public void updateIterableextendsEntityNoException()
	{
		List<Entity> entities = Arrays.asList(new MapEntity());
		decorator.update(entities);
		Mockito.verify(repository).update(entities);
	}
}
