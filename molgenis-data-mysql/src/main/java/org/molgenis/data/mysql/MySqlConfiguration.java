package org.molgenis.data.mysql;

import javax.sql.DataSource;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.molgenis.data.DataService;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.elasticsearch.IndexedManageableRepositoryCollectionDecorator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class MySqlConfiguration
{
	@Autowired
	private DataService dataService;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ImportServiceFactory importServiceFactory;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@Autowired
	private SearchService searchService;

	@Bean
	public AsyncJdbcTemplate asyncJdbcTemplate()
	{
		return new AsyncJdbcTemplate(new JdbcTemplate(dataSource));
	}

	@Bean
	@Scope("prototype")
	public MysqlRepository mysqlRepository()
	{
		return new MysqlRepository(dataService, dataSource, asyncJdbcTemplate());
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
				throw new NotImplementedException("Not implemented yet");
			}
		};

		return new IndexedManageableRepositoryCollectionDecorator(searchService, mysqlRepositoryCollection);
	}
}
