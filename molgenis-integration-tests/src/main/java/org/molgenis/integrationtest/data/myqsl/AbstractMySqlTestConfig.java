package org.molgenis.integrationtest.data.myqsl;

import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.elasticsearch.IndexedManageableRepositoryCollectionDecorator;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MySqlEntityFactory;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.integrationtest.data.AbstractDataApiTestConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractMySqlTestConfig extends AbstractDataApiTestConfig
{
	@Override
	protected ManageableRepositoryCollection getBackend()
	{
		return mysqlRepositoryCollection();
	}

	@Bean
	public MySqlEntityFactory mySqlEntityFactory()
	{
		return new MySqlEntityFactory(entityManager(), dataService());
	}

	@Bean
	public AsyncJdbcTemplate asyncJdbcTemplate()
	{
		return new AsyncJdbcTemplate(new JdbcTemplate(dataSource()));
	}

	@Bean
	@Scope("prototype")
	public MysqlRepository mysqlRepository()
	{
		return new MysqlRepository(dataService(), mySqlEntityFactory(), dataSource(), asyncJdbcTemplate());
	}

	@Bean(name =
	{ "MysqlRepositoryCollection" })
	public ManageableRepositoryCollection mysqlRepositoryCollection()
	{
		MysqlRepositoryCollection mysqlRepositoryCollection = new MysqlRepositoryCollection()
		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				return mysqlRepository();
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new UnsupportedOperationException();
			}
		};

		return new IndexedManageableRepositoryCollectionDecorator(searchService, mysqlRepositoryCollection);
	}

}
