package org.molgenis.data.migrate.version;

import static org.dbunit.database.DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS;
import static org.dbunit.database.DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES;
import static org.dbunit.database.DatabaseConfig.PROPERTY_DATATYPE_FACTORY;
import static org.dbunit.database.DatabaseConfig.PROPERTY_ESCAPE_PATTERN;

import java.sql.SQLException;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.xml.XmlProducer;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.xml.sax.InputSource;

/**
 * This integration test tests the integration of the sql with an actual postgres database. It only
 * runs in intellij, but we keep it around as an example of how to do this.
 */
class Step49UpdateClientAuthMethodIT {

  private PGSimpleDataSource dataSource;
  private DbUnitAssert assertions;

  @BeforeEach
  void setup() throws Exception {
    dataSource = new PGSimpleDataSource();
    dataSource.setURL(System.getProperty("db_uri", "jdbc:postgresql://localhost/molgenis"));
    dataSource.setUser(System.getProperty("db_user", "molgenis"));
    dataSource.setPassword(System.getProperty("db_password", "molgenis"));

    var in = getClass().getResourceAsStream("step49-before.xml");
    var dataSet = new CachedDataSet(new XmlProducer(new InputSource(in)), true);

    DatabaseDataSourceConnection conn = getConnection();
    DatabaseOperation.CLEAN_INSERT.execute(conn, dataSet);
    assertions = new DbUnitAssert();
  }

  private DatabaseDataSourceConnection getConnection() throws SQLException {
    var conn = new DatabaseDataSourceConnection(dataSource, "public");
    final var config = conn.getConfig();
    config.setProperty(FEATURE_CASE_SENSITIVE_TABLE_NAMES, true);
    config.setProperty(PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
    config.setProperty(PROPERTY_ESCAPE_PATTERN, "\"?\"");
    config.setProperty(FEATURE_ALLOW_EMPTY_FIELDS, true);
    return conn;
  }

  @Test
  void testUpgrade() throws Exception {
    var step49 = new Step49UpdateClientAuthMethod(dataSource);
    Assertions.assertDoesNotThrow(step49::upgrade);

    var actual =
        getConnection()
            .createQueryTable("OidcClient", "select * from \"sys_sec_oidc_OidcClient#3e7b1b4d\"");

    var in = getClass().getResourceAsStream("step49-done.xml");
    var expected =
        new CachedDataSet(new XmlProducer(new InputSource(in)), true).getTable("OidcClient");

    assertions.assertEquals(expected, actual);
  }
}
