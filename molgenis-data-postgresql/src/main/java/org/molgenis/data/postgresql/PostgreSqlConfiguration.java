package org.molgenis.data.postgresql;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PostgreSqlConfiguration {
  private final PostgreSqlEntityFactory postgreSqlEntityFactory;
  private final DataSource dataSource;
  private final DataService dataService;
  private final PostgreSqlExceptionTranslator postgreSqlExceptionTranslator;
  private final EntityTypeRegistry entityTypeRegistry;

  public PostgreSqlConfiguration(
      PostgreSqlEntityFactory postgreSqlEntityFactory,
      DataSource dataSource,
      DataService dataService,
      PostgreSqlExceptionTranslator postgreSqlExceptionTranslator,
      EntityTypeRegistry entityTypeRegistry) {
    this.postgreSqlEntityFactory = requireNonNull(postgreSqlEntityFactory);
    this.dataSource = requireNonNull(dataSource);
    this.dataService = requireNonNull(dataService);
    this.postgreSqlExceptionTranslator = requireNonNull(postgreSqlExceptionTranslator);
    this.entityTypeRegistry = requireNonNull(entityTypeRegistry);
  }

  @Bean
  public JdbcTemplate jdbcTemplate() {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.setExceptionTranslator(postgreSqlExceptionTranslator);
    return jdbcTemplate;
  }

  @Bean
  public RepositoryCollection postgreSqlRepositoryCollection() {
    return new PostgreSqlRepositoryCollectionDecorator(
        new PostgreSqlRepositoryCollection(
            postgreSqlEntityFactory, dataSource, jdbcTemplate(), dataService),
        entityTypeRegistry);
  }
}
