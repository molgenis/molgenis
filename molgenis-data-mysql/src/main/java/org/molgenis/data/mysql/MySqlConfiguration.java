package org.molgenis.data.mysql;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.importer.EmxImportService;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.meta.WritableMetaDataServiceDecorator;
import org.molgenis.data.mysql.meta.MysqlWritableMetaDataService;
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
	private DataSource dataSource;

	@Autowired
	private ImportServiceFactory importServiceFactory;

	// temporary workaround for module dependencies
	@Autowired
	private RepositoryDecoratorFactory repositoryDecoratorFactory;

	private MysqlWritableMetaDataService writableMetaDataService;

	@Bean
	@Scope("prototype")
	public MysqlRepository mysqlRepository()
	{
		return new MysqlRepository(dataSource);
	}

	@Bean
	public WritableMetaDataService writableMetaDataService()
	{
		writableMetaDataService = new MysqlWritableMetaDataService(dataSource);
		return writableMetaDataServiceDecorator().decorate(writableMetaDataService);
	}

	@Bean
	/**
	 * non-decorating decorator, to be overrided if you wish to decorate the MetaDataRepositories
	 */
	WritableMetaDataServiceDecorator writableMetaDataServiceDecorator()
	{
		return new WritableMetaDataServiceDecorator()
		{
			@Override
			public WritableMetaDataService decorate(WritableMetaDataService metaDataRepositories)
			{
				return metaDataRepositories;
			}
		};
	}

	@Bean
	public MysqlRepositoryCollection mysqlRepositoryCollection()
	{
		MysqlRepositoryCollection mysqlRepositoryCollection = new MysqlRepositoryCollection(dataSource, dataService,
				writableMetaDataService(), repositoryDecoratorFactory)

		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				MysqlRepository repo = mysqlRepository();
				repo.setRepositoryCollection(this);
				return repo;
			}
		};

		writableMetaDataService.setRepositoryCollection(mysqlRepositoryCollection);

		return mysqlRepositoryCollection;
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
