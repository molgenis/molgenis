package org.molgenis.data.mysql;

import java.util.UUID;

import javax.sql.DataSource;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.importer.EmxImportService;
import org.molgenis.data.importer.EmxMetaDataParser;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.ImportWriter;
import org.molgenis.data.importer.MetaDataParser;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.meta.WritableMetaDataServiceDecorator;
import org.molgenis.data.semantic.TagRepository;
import org.molgenis.data.semantic.UntypedTagService;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.IdGenerator;

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

	private MetaDataServiceImpl writableMetaDataService;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@Bean
	TagRepository tagRepository()
	{
		CrudRepository repo = (CrudRepository) mysqlRepositoryCollection().getRepositoryByEntityName(
				TagMetaData.ENTITY_NAME);
		return new TagRepository(repo, new IdGenerator()
		{

			@Override
			public UUID generateId()
			{
				return UUID.randomUUID();
			}
		});
	}

	@Bean
	public UntypedTagService tagService()
	{
		return new UntypedTagService(dataService, tagRepository());
	}

	@Bean
	public AsyncJdbcTemplate asyncJdbcTemplate()
	{
		return new AsyncJdbcTemplate(new JdbcTemplate(dataSource));
	}

	@Bean
	@Scope("prototype")
	public MysqlRepository mysqlRepository()
	{
		return new MysqlRepository(dataSource, asyncJdbcTemplate());
	}

	@Bean
	public WritableMetaDataService writableMetaDataService()
	{
		writableMetaDataService = new MetaDataServiceImpl();
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

		writableMetaDataService.setManageableCrudRepositoryCollection(mysqlRepositoryCollection);

		return mysqlRepositoryCollection;
	}

	@Bean
	public ImportService emxImportService()
	{
		return new EmxImportService(emxMetaDataParser(), importWriter());
	}

	@Bean
	public ImportWriter importWriter()
	{
		return new ImportWriter(dataService, writableMetaDataService, permissionSystemService, tagService());
	}

	@Bean
	public MetaDataParser emxMetaDataParser()
	{
		return new EmxMetaDataParser(dataService, writableMetaDataService);
	}

	@Bean
	public MysqlRepositoryRegistrator mysqlRepositoryRegistrator()
	{
		return new MysqlRepositoryRegistrator(mysqlRepositoryCollection(), importServiceFactory, emxImportService());
	}
}
