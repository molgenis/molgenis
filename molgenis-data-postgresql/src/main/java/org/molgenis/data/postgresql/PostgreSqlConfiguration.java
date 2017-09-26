package org.molgenis.data.postgresql;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class PostgreSqlConfiguration
{
	@Autowired
	private PostgreSqlEntityFactory postgreSqlEntityFactory;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private DataService dataService;

	@Autowired
	private PostgreSqlExceptionTranslator postgreSqlExceptionTranslator;

	@Autowired
	private EntityTypeRegistry entityTypeRegistry;

	@Bean
	public JdbcTemplate jdbcTemplate()
	{
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.setExceptionTranslator(postgreSqlExceptionTranslator);
		return jdbcTemplate;
	}

	@Bean
	public RepositoryCollection postgreSqlRepositoryCollection()
	{
		return new PostgreSqlRepositoryCollectionDecorator(
				new PostgreSqlRepositoryCollection(postgreSqlEntityFactory, dataSource, jdbcTemplate(), dataService),
				entityTypeRegistry);
	}
}
