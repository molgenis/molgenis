package org.molgenis.data.postgresql;

import javax.sql.DataSource;

import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.transaction.log.index.IndexTransactionLogRepositoryCollectionDecorator;
import org.molgenis.data.transaction.log.index.IndexTransactionLogService;
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
	private IndexTransactionLogService indexTransactionLogService;

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
	public ManageableRepositoryCollection postgreSqlRepositoryCollection()
	{
		PostgreSqlRepositoryCollection postgreSqlRepositoryCollection = new PostgreSqlRepositoryCollection(dataSource)
		{
			@Override
			protected PostgreSqlRepository createPostgreSqlRepository()
			{
				return postgreSqlRepository();
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new UnsupportedOperationException();
			}
		};
		return new IndexTransactionLogRepositoryCollectionDecorator(postgreSqlRepositoryCollection, indexTransactionLogService);
	}
}
