package org.molgenis.data.postgresql;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;
import org.molgenis.data.config.DataSourceConfig;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.postgresql.transaction.PostgreSqlTransactionManager;
import org.molgenis.data.transaction.TransactionExceptionTranslatorRegistry;
import org.molgenis.data.transaction.TransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

/** Database configuration */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@Import(DataSourceConfig.class)
public class DatabaseConfig implements TransactionManagementConfigurer {
  @Value("${db_driver:org.postgresql.Driver}")
  private String dbDriverClass;

  @Value("${db_uri:@null}")
  private String dbJdbcUri;

  @Value("${db_user:@null}")
  private String dbUser;

  @Value("${db_password:@null}")
  private String dbPassword;

  private final IdGenerator idGenerator;
  private final DataSource dataSource;
  private final TransactionExceptionTranslatorRegistry transactionExceptionTranslatorRegistry;

  public DatabaseConfig(
      IdGenerator idGenerator,
      DataSource dataSource,
      TransactionExceptionTranslatorRegistry transactionExceptionTranslatorRegistry) {
    this.idGenerator = requireNonNull(idGenerator);
    this.dataSource = requireNonNull(dataSource);
    this.transactionExceptionTranslatorRegistry =
        requireNonNull(transactionExceptionTranslatorRegistry);
  }

  @Bean
  public TransactionManager transactionManager() {
    return new PostgreSqlTransactionManager(
        idGenerator, dataSource, transactionExceptionTranslatorRegistry);
  }

  @Override
  public PlatformTransactionManager annotationDrivenTransactionManager() {
    return transactionManager();
  }
}
