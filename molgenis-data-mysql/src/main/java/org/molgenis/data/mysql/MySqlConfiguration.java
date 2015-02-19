package org.molgenis.data.mysql;

import java.util.UUID;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.IndexedManageableRepositoryCollectionDecorator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.importer.EmxImportService;
import org.molgenis.data.importer.EmxMetaDataParser;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.ImportWriter;
import org.molgenis.data.importer.MetaDataParser;
import org.molgenis.data.meta.TagMetaData;
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
		};

		return new IndexedManageableRepositoryCollectionDecorator(searchService, mysqlRepositoryCollection);
	}

	// TODO emx importer to own module
	@Bean
	public ImportService emxImportService()
	{
		return new EmxImportService(emxMetaDataParser(), importWriter(), dataService);
	}

	@Bean
	public ImportWriter importWriter()
	{
		return new ImportWriter(dataService, permissionSystemService, tagService());
	}

	@Bean
	TagRepository tagRepository()
	{
		Repository repo = mysqlRepositoryCollection().getRepository(TagMetaData.ENTITY_NAME);
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
	public MetaDataParser emxMetaDataParser()
	{
		return new EmxMetaDataParser(dataService);
	}

	@Bean
	public EmxImportServiceRegistrator mysqlRepositoryRegistrator()
	{
		return new EmxImportServiceRegistrator(importServiceFactory, emxImportService());
	}
}
