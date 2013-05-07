package org.molgenis.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;

import org.mockito.Mockito;
import org.molgenis.framework.db.Database;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class DatabaseUtilTest
{
	@AfterMethod
	public void tearDown()
	{
		new ApplicationContextProvider().setApplicationContext(null);
	}

	@Test
	public void closeQuietly() throws IOException
	{
		Database database = mock(Database.class);
		DatabaseUtil.closeQuietly(database);
		verify(database).close();
	}

	@Test
	public void closeQuietly_ignoreException() throws IOException
	{
		Database database = mock(Database.class);
		Mockito.doThrow(new IOException()).when(database).close();
		DatabaseUtil.closeQuietly(database);
		verify(database).close();
	}

	@Test
	public void closeQuietly_null()
	{
		// test passes if no exception occurs
		DatabaseUtil.closeQuietly(null);
	}

	@Test
	public void createDatabase()
	{
		Database database = mock(Database.class);
		ApplicationContext applicationContext = mock(ApplicationContext.class);
		when(applicationContext.getBean("unauthorizedPrototypeDatabase", Database.class)).thenReturn(database);
		new ApplicationContextProvider().setApplicationContext(applicationContext);
		assertNotNull(DatabaseUtil.createDatabase());
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void createDatabase_noApplicationContext()
	{
		DatabaseUtil.createDatabase();
	}
}
