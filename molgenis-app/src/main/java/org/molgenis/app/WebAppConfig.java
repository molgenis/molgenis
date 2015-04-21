package org.molgenis.app;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.molgenis.DatabaseConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.data.jpa.JpaRepositoryCollection;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.system.RepositoryTemplateLoader;
import org.molgenis.data.version.v1_5.Step1UpgradeMetaData;
import org.molgenis.data.version.v1_5.Step2;
import org.molgenis.data.version.v1_5.Step3AddOrderColumnToMrefTables;
import org.molgenis.data.version.v1_5.Step4;
import org.molgenis.data.version.v1_6.Step7UpgradeMetaDataTo1_6;
import org.molgenis.dataexplorer.freemarker.DataExplorerHyperlinkDirective;
import org.molgenis.system.core.FreemarkerTemplateRepository;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.molgenis.ui.migrate.v1_5.Step5AlterDataexplorerMenuURLs;
import org.molgenis.ui.migrate.v1_5.Step6ChangeRScriptType;
import org.molgenis.util.DependencyResolver;
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

import com.google.common.collect.Sets;

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
		upgradeService.addUpgrade(new Step4(dataSource, mysqlRepositoryCollection));
		upgradeService.addUpgrade(new Step5AlterDataexplorerMenuURLs(jpaRepositoryCollection
				.getRepository("RuntimeProperty")));
		upgradeService.addUpgrade(new Step6ChangeRScriptType(dataSource, searchService));
		upgradeService.addUpgrade(new Step7UpgradeMetaDataTo1_6(dataSource, searchService));
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
		List<EntityMetaData> metas = DependencyResolver.resolve(Sets.newHashSet(localDataService.getMeta()
				.getEntityMetaDatas()));

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
				else
				{
					LOG.warn("backend unkown for metadata " + emd.getName());
				}
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
