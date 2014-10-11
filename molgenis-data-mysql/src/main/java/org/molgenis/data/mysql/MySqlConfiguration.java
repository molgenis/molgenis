package org.molgenis.data.mysql;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.importer.EmxImportService;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.meta.AttributeMetaDataRepositoryDecoratorFactory;
import org.molgenis.data.meta.EntityMetaDataRepositoryDecoratorFactory;
import org.molgenis.data.mysql.meta.MysqlAttributeMetaDataRepository;
import org.molgenis.data.mysql.meta.MysqlEntityMetaDataRepository;
import org.molgenis.data.mysql.meta.MysqlPackageRepository;
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
	@Autowired
	private EntityMetaDataRepositoryDecoratorFactory entityMetaDataRepositoryDecoratorFactory;
	@Autowired
	private AttributeMetaDataRepositoryDecoratorFactory attributeMetaDataRepositoryDecoratorFactory;

	@Bean
	@Scope("prototype")
	public MysqlRepository mysqlRepository()
	{
		return new MysqlRepository(dataSource);
	}

	@Bean
	public MysqlPackageRepository packageRepository()
	{
		return new MysqlPackageRepository(dataSource);
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
		return new MysqlRepositoryCollection(dataSource, dataService, packageRepository(), entityMetaDataRepository(),
				attributeMetaDataRepository(), repositoryDecoratorFactory, entityMetaDataRepositoryDecoratorFactory,
				attributeMetaDataRepositoryDecoratorFactory)

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
