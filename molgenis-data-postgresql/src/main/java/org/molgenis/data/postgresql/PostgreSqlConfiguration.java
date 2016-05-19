package org.molgenis.data.postgresql;

import javax.sql.DataSource;

import org.molgenis.data.RepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PostgreSqlConfiguration
{
	@Autowired
	private PostgreSqlEntityFactory postgreSqlEntityFactory;

	@Autowired
	private DataSource dataSource;

	@Bean
	@Scope("prototype")
	public PostgreSqlRepository postgreSqlRepository()
	{
		return new PostgreSqlRepository(postgreSqlEntityFactory, jdbcTemplate());
	}

	@Bean
	public JdbcTemplate jdbcTemplate()
	{
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.setExceptionTranslator(new PostgreSqlExceptionTranslator(dataSource));
		return jdbcTemplate;
	}

	@Bean(name =
	{ "PostgreSqlRepositoryCollection" })
	public RepositoryCollection postgreSqlRepositoryCollection()
	{
		PostgreSqlRepositoryCollection postgreSqlRepositoryCollection = new PostgreSqlRepositoryCollection(dataSource)
		{
			@Override
			public boolean hasRepository(String name)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected PostgreSqlRepository createPostgreSqlRepository()
			{
				return postgreSqlRepository();
			}
		};
		return postgreSqlRepositoryCollection;
	}
}
