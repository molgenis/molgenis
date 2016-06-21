package org.molgenis.app;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import freemarker.template.TemplateException;
import org.molgenis.CommandLineOnlyConfiguration;
import org.molgenis.DatabaseConfig;
import org.molgenis.data.*;
import org.molgenis.data.config.HttpClientConfig;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.mysql.*;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.system.RepositoryTemplateLoader;
import org.molgenis.dataexplorer.freemarker.DataExplorerHyperlinkDirective;
import org.molgenis.framework.MolgenisUpgradeService;
import org.molgenis.migrate.version.v1_11.Step20RebuildElasticsearchIndex;
import org.molgenis.migrate.version.v1_11.Step21SetLoggingEventBackend;
import org.molgenis.migrate.version.v1_13.Step22RemoveDiseaseMatcher;
import org.molgenis.migrate.version.v1_14.Step23RebuildElasticsearchIndex;
import org.molgenis.migrate.version.v1_15.Step24UpdateApplicationSettings;
import org.molgenis.migrate.version.v1_15.Step25LanguagesPermissions;
import org.molgenis.migrate.version.v1_16.Step26migrateJpaBackend;
import org.molgenis.migrate.version.v1_17.Step27MetaDataAttributeRoles;
import org.molgenis.migrate.version.v1_19.Step28MigrateSorta;
import org.molgenis.migrate.version.v1_21.Step29MigrateJobExecutionProgressMessage;
import org.molgenis.migrate.version.v1_21.Step30MigrateJobExecutionUser;
import org.molgenis.migrate.version.v1_22.Step31UpdateApplicationSettings;
import org.molgenis.migrate.version.v1_22.Step32AddRowLevelSecurityMetadata;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.GsonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;

@Configuration
@EnableTransactionManagement
@EnableWebMvc
@EnableAsync
@ComponentScan(basePackages = "org.molgenis", excludeFilters = @Filter(type = FilterType.ANNOTATION, value = CommandLineOnlyConfiguration.class))
@Import({ WebAppSecurityConfig.class, DatabaseConfig.class, HttpClientConfig.class, EmbeddedElasticSearchConfig.class,
		GsonConfig.class })
public class WebAppConfig extends MolgenisWebAppConfig
{
	private static final Logger LOG = LoggerFactory.getLogger(WebAppConfig.class);

	@Autowired
	private DataService dataService;

	@Autowired
	@Qualifier("MysqlRepositoryCollection")
	private ManageableRepositoryCollection mysqlRepositoryCollection;

	@Autowired
	private ElasticsearchRepositoryCollection elasticsearchRepositoryCollection;

	@Autowired
	private EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory;

	@Autowired
	private Gson gson;

	@Autowired
	private Step20RebuildElasticsearchIndex step20RebuildElasticsearchIndex;

	@Autowired
	private Step23RebuildElasticsearchIndex step23RebuildElasticsearchIndex;

	@Override
	public ManageableRepositoryCollection getBackend()
	{
		return mysqlRepositoryCollection;
	}

	@Override
	public void addUpgrades()
	{
		upgradeService.addUpgrade(step20RebuildElasticsearchIndex);
		upgradeService.addUpgrade(new Step21SetLoggingEventBackend(dataSource));
		upgradeService.addUpgrade(new Step22RemoveDiseaseMatcher(dataSource));
		upgradeService.addUpgrade(step23RebuildElasticsearchIndex);
		upgradeService.addUpgrade(new Step24UpdateApplicationSettings(dataSource, idGenerator));
		upgradeService.addUpgrade(new Step25LanguagesPermissions(dataService));
		upgradeService.addUpgrade(new Step26migrateJpaBackend(dataSource, MysqlRepositoryCollection.NAME, idGenerator));
		upgradeService.addUpgrade(new Step27MetaDataAttributeRoles(dataSource));
		upgradeService.addUpgrade(new Step28MigrateSorta(dataSource));
		upgradeService.addUpgrade(new Step29MigrateJobExecutionProgressMessage(dataSource));
		upgradeService.addUpgrade(new Step30MigrateJobExecutionUser(dataSource));
		upgradeService.addUpgrade(new Step31UpdateApplicationSettings(dataSource, idGenerator));

		// Set the entities which should be row level secured
		Step32AddRowLevelSecurityMetadata step32AddRowLevelSecurityMetadata = new Step32AddRowLevelSecurityMetadata(
				dataSource, idGenerator);

		step32AddRowLevelSecurityMetadata.setEntitiesToSecure(
				asList("bbmri_eric_biobanksize", "bbmri_eric_directory", "bbmri_eric_EricSource",
						"bbmri_eric_staffsize", "bbmri_nl_age_types", "bbmri_nl_biobanks", "bbmri_nl_collection_types",
						"bbmri_nl_countries", "bbmri_nl_data_category_types", "bbmri_nl_disease_types",
						"bbmri_nl_gender_types", "bbmri_nl_juristic_persons", "bbmri_nl_material_types",
						"bbmri_nl_omics_data_types", "bbmri_nl_ontology_term", "bbmri_nl_persons",
						"bbmri_nl_publications", "bbmri_nl_sample_collections", "bbmri_nl_sample_size_types",
						"bbmri_nl_staff_size_types"));
		upgradeService.addUpgrade(step32AddRowLevelSecurityMetadata);
	}

	@Override
	protected void addReposToReindex(DataServiceImpl localDataService, MySqlEntityFactory localMySqlEntityFactory)
	{
		// Get the undecorated repos to index
		MysqlRepositoryCollection backend = new MysqlRepositoryCollection()
		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				return new MysqlRepository(localDataService, localMySqlEntityFactory, dataSource,
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
			if (!emd.isAbstract() && !localDataService.hasRepository(emd.getName()))
			{
				if (MysqlRepositoryCollection.NAME.equals(emd.getBackend()))
				{
					localDataService.addRepository(backend.addEntityMeta(emd));
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
		freemarkerVariables
				.put("dataExplorerLink", new DataExplorerHyperlinkDirective(molgenisPluginRegistry(), dataService));
	}

	@Override
	public FreeMarkerConfigurer freeMarkerConfigurer() throws IOException, TemplateException
	{
		FreeMarkerConfigurer result = super.freeMarkerConfigurer();
		// Look up unknown templates in the FreemarkerTemplate repository
		result.setPostTemplateLoaders(new RepositoryTemplateLoader(dataService));
		return result;
	}

	@Bean
	public SOAPConnectionFactory soapConnectionFactory() throws UnsupportedOperationException, SOAPException
	{
		return SOAPConnectionFactory.newInstance();
	}
}
