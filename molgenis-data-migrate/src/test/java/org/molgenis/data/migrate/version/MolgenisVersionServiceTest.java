package org.molgenis.data.migrate.version;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class MolgenisVersionServiceTest extends AbstractMockitoTest
{
	@Mock
	private DataSource dataSource;

	private MolgenisVersionService molgenisVersionService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		molgenisVersionService = new MolgenisVersionService(dataSource);
	}

	@Test
	public void testGetAppVersion()
	{
		assertEquals(molgenisVersionService.getAppVersion(), MolgenisVersionService.VERSION);
	}

	@Test
	public void testGetSchemaVersion() throws SQLException
	{
		Connection connection = mock(Connection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		Statement statement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(statement);
		ResultSet resultSet = mock(ResultSet.class);
		when(statement.executeQuery("SELECT \"id\" FROM \"Version\"")).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(true);
		when(resultSet.getInt("id")).thenReturn(30);
		assertEquals(molgenisVersionService.getSchemaVersion(), 30);
	}

	@Test
	public void testSetSchemaVersion() throws SQLException
	{
		Connection connection = mock(Connection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		Statement statement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(statement);

		molgenisVersionService.setSchemaVersion(30);
		verify(statement).execute("UPDATE \"Version\" SET \"id\"=30");
	}
}