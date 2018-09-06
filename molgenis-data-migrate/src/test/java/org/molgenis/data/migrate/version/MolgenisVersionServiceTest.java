package org.molgenis.data.migrate.version;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

import java.sql.*;
import javax.sql.DataSource;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisVersionServiceTest extends AbstractMockitoTest {
  @Mock private DataSource dataSource;

  private MolgenisVersionService molgenisVersionService;

  @BeforeMethod
  public void setUpBeforeMethod() {
    molgenisVersionService = new MolgenisVersionService(dataSource);
  }

  @Test
  public void testGetAppVersion() {
    assertEquals(molgenisVersionService.getAppVersion(), MolgenisVersionService.VERSION);
  }

  @Test
  public void testGetSchemaVersion() throws SQLException {
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
  public void testSetSchemaVersion() throws SQLException {
    Connection connection = mock(Connection.class);
    when(dataSource.getConnection()).thenReturn(connection);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    when(connection.prepareStatement("UPDATE \"Version\" SET \"id\"=?"))
        .thenReturn(preparedStatement);

    molgenisVersionService.setSchemaVersion(30);
    verify(preparedStatement).setInt(1, 30);
    verify(preparedStatement).execute();
  }
}
