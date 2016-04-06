package org.molgenis.data.postgresql;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.elasticsearch.IndexedManageableRepositoryCollectionDecorator;
import org.molgenis.data.elasticsearch.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PostgreSqlConfiguration
{
	@Autowired
	private DataService dataService;

	@Autowired
	private PostgreSqlEntityFactory postgreSqlEntityFactory;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private SearchService searchService;

	@Bean
	@Scope("prototype")
	public PostgreSqlRepository postgreSqlRepository()
	{
		return new PostgreSqlRepository(dataService, postgreSqlEntityFactory, dataSource);
	}

	@Bean(name =
	{ "PostgreSqlRepositoryCollection" })
	public ManageableRepositoryCollection postgreSqlRepositoryCollection()
	{
		PostgreSqlRepositoryCollection postgreSqlRepositoryCollection = new PostgreSqlRepositoryCollection()
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

		return new IndexedManageableRepositoryCollectionDecorator(searchService, postgreSqlRepositoryCollection);
	}
}
