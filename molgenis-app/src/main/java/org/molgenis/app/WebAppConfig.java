package org.molgenis.app;

import static org.molgenis.data.support.QueryImpl.EQ;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.molgenis.DatabaseConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.jpa.JpaRepositoryCollection;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.meta.migrate.v1_4.AttributeMetaDataMetaData1_4;
import org.molgenis.data.meta.migrate.v1_4.EntityMetaDataMetaData1_4;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.system.RepositoryTemplateLoader;
import org.molgenis.dataexplorer.freemarker.DataExplorerHyperlinkDirective;
import org.molgenis.system.core.FreemarkerTemplateRepository;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import autovalue.shaded.com.google.common.common.collect.Lists;
import freemarker.template.TemplateException;

@Configuration
@EnableTransactionManagement
@EnableWebMvc
@EnableAsync
@ComponentScan("org.molgenis")
@Import(
{ WebAppSecurityConfig.class, DatabaseConfig.class, EmbeddedElasticSearchConfig.class })
public class WebAppConfig extends MolgenisWebAppConfig
{
	private static final Logger LOG = LoggerFactory.getLogger(WebAppConfig.class);

	@Autowired
	private DataService dataService;

	@Autowired
	private FreemarkerTemplateRepository freemarkerTemplateRepository;

	@Autowired
	@Qualifier("MysqlRepositoryCollection")
	private ManageableRepositoryCollection mysqlRepositoryCollection;

	@Autowired
	private JpaRepositoryCollection jpaRepositoryCollection;

	@Autowired
	private DataSource dataSource;

	@Override
	public ManageableRepositoryCollection getBackend()
	{
		return mysqlRepositoryCollection;
	}

	@Override
	protected void upgradeMetaData()
	{
		// Update database tables here! (or run SQL script manually)

		// Create local dataservice and metadataservice
		DataServiceImpl localDataService = new DataServiceImpl();
		new MetaDataServiceImpl(localDataService);

		MysqlRepositoryCollection backend = new MysqlRepositoryCollection()
		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				return new MysqlRepository(localDataService, dataSource, new AsyncJdbcTemplate(new JdbcTemplate(
						dataSource)));
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new NotImplementedException("Not implemented yet");
			}
		};

		LOG.info("updateAttributeOrder...");
		localDataService.addRepository(backend.addEntityMeta(new PackageMetaData()));
		localDataService.addRepository(backend.addEntityMeta(new TagMetaData()));
		localDataService.addRepository(backend.addEntityMeta(new AttributeMetaDataMetaData()));
		Repository entityRepo = backend.addEntityMeta(new EntityMetaDataMetaData());
		localDataService.addRepository(entityRepo);

		SearchService localSearchService = embeddedElasticSearchServiceFactory.create(localDataService,
				new EntityToSourceConverter());

		// save all entity metadata with attributes in proper order
		for (Entity entityMetaDataEntity : entityRepo)
		{
			LOG.info("Entity: " + entityMetaDataEntity.get(EntityMetaDataMetaData1_4.SIMPLE_NAME));
			List<Entity> attributes = Lists.newArrayList(localSearchService.search(
					EQ(AttributeMetaDataMetaData1_4.ENTITY,
							entityMetaDataEntity.getString(EntityMetaDataMetaData1_4.SIMPLE_NAME)),
					new AttributeMetaDataMetaData1_4()));
			entityMetaDataEntity.set(EntityMetaDataMetaData.ATTRIBUTES, attributes);
			entityRepo.update(entityMetaDataEntity);
		}
		LOG.info("updateAttributeOrder done.");

		// TODO: How can we refresh the metadata here? I still get merge warnings.
		localSearchService.delete("entities");
		localSearchService.delete("attributes");
		localSearchService.delete("tags");
		localSearchService.delete("packages");

		try
		{
			searchService.createMappings(new TagMetaData());
			searchService.createMappings(new PackageMetaData());
			searchService.createMappings(new AttributeMetaDataMetaData());
			searchService.createMappings(new EntityMetaDataMetaData());
		}
		catch (IOException e)
		{
			LOG.error("error creating metadata mappings", e);
		}
	}

	@Override
	protected void addReposToReindex(DataServiceImpl localDataService)
	{
		// Get the undecorated repos to index
		MysqlRepositoryCollection backend = new MysqlRepositoryCollection()
		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				return new MysqlRepository(localDataService, dataSource, new AsyncJdbcTemplate(new JdbcTemplate(
						dataSource)));
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new NotImplementedException("Not implemented yet");
			}
		};

		// metadata repositories get created here.
		localDataService.getMeta().setDefaultBackend(backend);

		for (EntityMetaData emd : localDataService.getMeta().getEntityMetaDatas())
		{
			if (MysqlRepositoryCollection.NAME.equals(emd.getBackend()))
			{
				localDataService.addRepository(backend.addEntityMeta(emd));
			}
			else if (JpaRepositoryCollection.NAME.equals(emd.getBackend()))
			{
				localDataService.addRepository(jpaRepositoryCollection.getUnderlying(emd.getName()));
			}
			else
			{
				LOG.warn("backend unkown for metadata " + emd.getName());
			}
		}
	}

	@Override
	protected void addFreemarkerVariables(Map<String, Object> freemarkerVariables)
	{
		freemarkerVariables.put("dataExplorerLink", new DataExplorerHyperlinkDirective(molgenisPluginRegistry(),
				dataService));
	}

	@Override
	public FreeMarkerConfigurer freeMarkerConfigurer() throws IOException, TemplateException
	{
		FreeMarkerConfigurer result = super.freeMarkerConfigurer();
		// Look up unknown templates in the FreemarkerTemplate repository
		result.setPostTemplateLoaders(new RepositoryTemplateLoader(freemarkerTemplateRepository));
		return result;
	}
}
