package org.molgenis.app;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.molgenis.CommandLineOnlyConfiguration;
import org.molgenis.DatabaseConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.jpa.JpaRepositoryCollection;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.system.RepositoryTemplateLoader;
import org.molgenis.dataexplorer.freemarker.DataExplorerHyperlinkDirective;
import org.molgenis.migrate.version.v1_10.Step17RuntimePropertiesToGafListSettings;
import org.molgenis.migrate.version.v1_10.Step18RuntimePropertiesToAnnotatorSettings;
import org.molgenis.migrate.version.v1_10.Step19RemoveMolgenisLock;
import org.molgenis.migrate.version.v1_11.Step20RebuildElasticsearchIndex;
import org.molgenis.migrate.version.v1_11.Step21SetLoggingEventBackend;
import org.molgenis.migrate.version.v1_5.Step1UpgradeMetaData;
import org.molgenis.migrate.version.v1_5.Step2;
import org.molgenis.migrate.version.v1_5.Step3AddOrderColumnToMrefTables;
import org.molgenis.migrate.version.v1_5.Step4VarcharToText;
import org.molgenis.migrate.version.v1_5.Step5AlterDataexplorerMenuURLs;
import org.molgenis.migrate.version.v1_5.Step6ChangeRScriptType;
import org.molgenis.migrate.version.v1_6.Step7UpgradeMetaDataTo1_6;
import org.molgenis.migrate.version.v1_6.Step8VarcharToTextRepeated;
import org.molgenis.migrate.version.v1_6.Step9MysqlTablesToInnoDB;
import org.molgenis.migrate.version.v1_8.Step10DeleteFormReferences;
import org.molgenis.migrate.version.v1_8.Step11ConvertNames;
import org.molgenis.migrate.version.v1_8.Step12ChangeElasticsearchTokenizer;
import org.molgenis.migrate.version.v1_8.Step13RemoveCatalogueMenuEntries;
import org.molgenis.migrate.version.v1_9.RuntimePropertyToAppSettingsMigrator;
import org.molgenis.migrate.version.v1_9.RuntimePropertyToDataExplorerSettingsMigrator;
import org.molgenis.migrate.version.v1_9.RuntimePropertyToGenomicDataSettingsMigrator;
import org.molgenis.migrate.version.v1_9.RuntimePropertyToStaticContentMigrator;
import org.molgenis.migrate.version.v1_9.Step14UpdateAttributeMapping;
import org.molgenis.migrate.version.v1_9.Step15AddDefaultValue;
import org.molgenis.migrate.version.v1_9.Step16RuntimePropertyToSettings;
import org.molgenis.system.core.FreemarkerTemplateRepository;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.GsonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.google.common.collect.Sets;
import com.google.gson.Gson;

import freemarker.template.TemplateException;

@Configuration
@EnableTransactionManagement
@EnableWebMvc
@EnableAsync
@ComponentScan(basePackages = "org.molgenis", excludeFilters = @Filter(type = FilterType.ANNOTATION, value = CommandLineOnlyConfiguration.class) )
@Import(
{ WebAppSecurityConfig.class, DatabaseConfig.class, EmbeddedElasticSearchConfig.class, GsonConfig.class })
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
	private ElasticsearchRepositoryCollection elasticsearchRepositoryCollection;

	@Autowired
	private EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory;

	@Autowired
	private Gson gson;

	@Autowired
	private RuntimePropertyToAppSettingsMigrator runtimePropertyToAppSettingsMigrator;

	@Autowired
	private RuntimePropertyToGenomicDataSettingsMigrator runtimePropertyToGenomicDataSettingsMigrator;

	@Autowired
	private RuntimePropertyToDataExplorerSettingsMigrator runtimePropertyToDataExplorerSettingsMigrator;

	@Autowired
	private RuntimePropertyToStaticContentMigrator runtimePropertyToStaticContentMigrator;

	@Autowired
	private Step17RuntimePropertiesToGafListSettings step17RuntimePropertiesToGafListSettings;

	@Autowired
	private Step18RuntimePropertiesToAnnotatorSettings step18RuntimePropertiesToAnnotatorSettings;

	@Autowired
	private Step19RemoveMolgenisLock step19RemoveMolgenisLock;

	@Autowired
	private Step20RebuildElasticsearchIndex step20RebuildElasticsearchIndex;

	@Override
	public ManageableRepositoryCollection getBackend()
	{
		return mysqlRepositoryCollection;
	}

	@Override
	public void addUpgrades()
	{
		upgradeService.addUpgrade(new Step1UpgradeMetaData(dataSource, searchService));
		upgradeService.addUpgrade(new Step2(dataService, jpaRepositoryCollection, dataSource, searchService));
		upgradeService.addUpgrade(new Step3AddOrderColumnToMrefTables(dataSource));
		upgradeService.addUpgrade(new Step4VarcharToText(dataSource, mysqlRepositoryCollection));
		upgradeService.addUpgrade(
				new Step5AlterDataexplorerMenuURLs(jpaRepositoryCollection.getRepository("RuntimeProperty"), gson));
		upgradeService.addUpgrade(new Step6ChangeRScriptType(dataSource, searchService));
		upgradeService.addUpgrade(new Step7UpgradeMetaDataTo1_6(dataSource, searchService));
		upgradeService.addUpgrade(new Step8VarcharToTextRepeated(dataSource));
		upgradeService.addUpgrade(new Step9MysqlTablesToInnoDB(dataSource));
		upgradeService.addUpgrade(new Step10DeleteFormReferences(dataSource, gson));

		SingleConnectionDataSource singleConnectionDS = null;
		try
		{
			singleConnectionDS = new SingleConnectionDataSource(dataSource.getConnection(), true);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		upgradeService.addUpgrade(new Step11ConvertNames(singleConnectionDS));
		upgradeService.addUpgrade(new Step12ChangeElasticsearchTokenizer(embeddedElasticSearchServiceFactory));
		upgradeService.addUpgrade(new Step13RemoveCatalogueMenuEntries(dataSource, gson));
		upgradeService.addUpgrade(new Step14UpdateAttributeMapping(dataSource));
		upgradeService.addUpgrade(new Step15AddDefaultValue(dataSource, searchService, jpaRepositoryCollection));
		upgradeService.addUpgrade(new Step16RuntimePropertyToSettings(runtimePropertyToAppSettingsMigrator,
				runtimePropertyToGenomicDataSettingsMigrator, runtimePropertyToDataExplorerSettingsMigrator,
				runtimePropertyToStaticContentMigrator));
		upgradeService.addUpgrade(step17RuntimePropertiesToGafListSettings);
		upgradeService.addUpgrade(step18RuntimePropertiesToAnnotatorSettings);
		upgradeService.addUpgrade(step19RemoveMolgenisLock);
		upgradeService.addUpgrade(step20RebuildElasticsearchIndex);
		upgradeService.addUpgrade(new Step21SetLoggingEventBackend(dataSource));
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
				return new MysqlRepository(localDataService, dataSource,
						new AsyncJdbcTemplate(new JdbcTemplate(dataSource)));
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new UnsupportedOperationException();
			}
		};

		// metadata repositories get created here.
		localDataService.getMeta().setDefaultBackend(backend);
		List<EntityMetaData> metas = DependencyResolver
				.resolve(Sets.newHashSet(localDataService.getMeta().getEntityMetaDatas()));

		for (EntityMetaData emd : metas)
		{
			if (!emd.isAbstract())
			{
				if (MysqlRepositoryCollection.NAME.equals(emd.getBackend()))
				{
					localDataService.addRepository(backend.addEntityMeta(emd));
				}
				else if (JpaRepositoryCollection.NAME.equals(emd.getBackend()))
				{
					localDataService.addRepository(jpaRepositoryCollection.getUnderlying(emd.getName()));
				}
				else if (ElasticsearchRepositoryCollection.NAME.equals(emd.getBackend()))
				{
					localDataService.addRepository(elasticsearchRepositoryCollection.addEntityMeta(emd));
				}
				else
				{
					LOG.warn("backend [{}] unknown for meta data [{}]", emd.getBackend(), emd.getName());
				}
			}
		}
	}

	@Override
	protected void addFreemarkerVariables(Map<String, Object> freemarkerVariables)
	{
		freemarkerVariables.put("dataExplorerLink",
				new DataExplorerHyperlinkDirective(molgenisPluginRegistry(), dataService));
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
