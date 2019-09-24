package org.molgenis.data.migrate.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.migrate.version.MolgenisVersionService.VERSION;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;

class MolgenisVersionServiceTest extends AbstractMockitoTest {
  @Mock private DataSource dataSource;

  private MolgenisVersionService molgenisVersionService;

  @BeforeEach
  void setUpBeforeMethod() {
    molgenisVersionService = new MolgenisVersionService(dataSource);
  }

  @Test
  void testGetAppVersion() {
    assertEquals(VERSION, molgenisVersionService.getAppVersion());
  }

  @Test
  void testGetSchemaVersion() throws SQLException {
    Connection connection = mock(Connection.class);
    when(dataSource.getConnection()).thenReturn(connection);
    Statement statement = mock(Statement.class);
    when(connection.createStatement()).thenReturn(statement);
    ResultSet resultSet = mock(ResultSet.class);
    when(statement.executeQuery("SELECT \"id\" FROM \"Version\"")).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getInt("id")).thenReturn(30);
    assertEquals(30, molgenisVersionService.getSchemaVersion());
  }

  @Test
  void testSetSchemaVersion() throws SQLException {
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
