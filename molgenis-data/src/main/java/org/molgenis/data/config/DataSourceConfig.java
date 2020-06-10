package org.molgenis.data.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datasource configuration */
@Configuration
public class DataSourceConfig {

  /**
   * Max pool size must be <= the maximum number of connections of configured in the DBMS (e.g.
   * PostgreSQL). The magic number is based on PostgreSQL default max connections = 100 minus 5
   * connections for admin tools communicating with the DBMS.
   */
  private static final int MAX_POOL_SIZE = 95;
  /** When the connections are not in use, keep 10 idle connections around. */
  public static final int MIN_IDLE = 10;

  @Value("${db_driver:org.postgresql.Driver}")
  private String dbDriverClass;

  @Value("${db_uri:@null}")
  private String dbJdbcUri;

  @Value("${db_user:@null}")
  private String dbUser;

  @Value("${db_password:@null}")
  private String dbPassword;

  @Bean
  public HikariDataSource dataSource() {
    if (dbDriverClass == null) throw new IllegalArgumentException("db_driver is null");
    if (dbJdbcUri == null) throw new IllegalArgumentException("db_uri is null");
    if (dbUser == null)
      throw new IllegalArgumentException(
          "please configure the db_user property in your molgenis-server.properties");
    if (dbPassword == null)
      throw new IllegalArgumentException(
          "please configure the db_password property in your molgenis-server.properties");

    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setDriverClassName(dbDriverClass);
    dataSource.setJdbcUrl(dbJdbcUri);
    dataSource.setUsername(dbUser);
    dataSource.setPassword(dbPassword);
    dataSource.setMaximumPoolSize(MAX_POOL_SIZE);
    dataSource.setMinimumIdle(MIN_IDLE);

    dataSource.addDataSourceProperty("reWriteBatchedInserts", "true");
    dataSource.addDataSourceProperty("autosave", "CONSERVATIVE");

    return dataSource;
  }
}
