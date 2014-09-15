package org.molgenis.data.mysql;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.importer.EmxImportService;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class MySqlConfiguration
{
	@Autowired
	private DataService dataService;

	@Autowired
	private SearchService elasticSearchService;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ImportServiceFactory importServiceFactory;

	@Bean
	@Scope("prototype")
	public MysqlRepository mysqlRepository()
	{
		return new MysqlRepository(dataSource);
	}

	@Bean
	public MysqlEntityMetaDataRepository entityMetaDataRepository()
	{
		return new MysqlEntityMetaDataRepository(dataSource);
	}

	@Bean
	public MysqlAttributeMetaDataRepository attributeMetaDataRepository()
	{
		return new MysqlAttributeMetaDataRepository(dataSource);
	}

	@Bean
	public MysqlRepositoryCollection mysqlRepositoryCollection()
	{
		return new MysqlRepositoryCollection(dataSource, dataService, entityMetaDataRepository(),
				attributeMetaDataRepository(), elasticSearchService)
		{
			@Override
			protected MysqlRepository createMysqlRepsitory()
			{
				MysqlRepository repo = mysqlRepository();
				repo.setRepositoryCollection(this);
				return repo;
			}
		};
	}

	@Bean
	public ImportService emxImportService()
	{
		return new EmxImportService(dataService);
	}

	@Bean
	public MysqlRepositoryRegistrator mysqlRepositoryRegistrator()
	{
		return new MysqlRepositoryRegistrator(mysqlRepositoryCollection(), importServiceFactory, emxImportService());
	}
}
