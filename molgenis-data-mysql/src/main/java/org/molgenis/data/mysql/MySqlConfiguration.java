package org.molgenis.data.mysql;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.importer.EmxImportService;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.meta.MetaDataRepositories;
import org.molgenis.data.meta.MetaDataRepositoriesDecorator;
import org.molgenis.data.mysql.meta.MysqlMetaDataRepositories;
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

	private MysqlMetaDataRepositories mysqlMetaDataRepositories;

	@Bean
	@Scope("prototype")
	public MysqlRepository mysqlRepository()
	{
		return new MysqlRepository(dataSource);
	}

	@Bean
	public MetaDataRepositories metaDataRepositories()
	{
		mysqlMetaDataRepositories = new MysqlMetaDataRepositories(dataSource);
		return metaDataRepositoriesDecorator().decorate(mysqlMetaDataRepositories);
	}

	@Bean
	/**
	 * non-decorating decorator, to be overrided if you wish to decorate the MetaDataRepositories
	 */
	MetaDataRepositoriesDecorator metaDataRepositoriesDecorator()
	{
		return new MetaDataRepositoriesDecorator()
		{
			@Override
			public MetaDataRepositories decorate(MetaDataRepositories metaDataRepositories)
			{
				return metaDataRepositories;
			}
		};
	}

	@Bean
	public MysqlRepositoryCollection mysqlRepositoryCollection()
	{
		MysqlRepositoryCollection mysqlRepositoryCollection = new MysqlRepositoryCollection(dataSource, dataService,
				metaDataRepositories(), repositoryDecoratorFactory)

		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				MysqlRepository repo = mysqlRepository();
				repo.setRepositoryCollection(this);
				return repo;
			}
		};

		mysqlMetaDataRepositories.setRepositoryCollection(mysqlRepositoryCollection);

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
