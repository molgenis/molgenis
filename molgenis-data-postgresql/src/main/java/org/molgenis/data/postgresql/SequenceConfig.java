package org.molgenis.data.postgresql;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SequenceConfig {

  @Bean
  public JdbcSequences defaultSequenceIdGenerator(DataSource dataSource) {
    JdbcOperations operations = new JdbcTemplate(dataSource);
    return new JdbcSequences(new PostgreSqlIdGenerator(32), operations);
  }
}
