package org.molgenis.app;

import java.io.IOException;
import java.util.Map;

import javax.sql.DataSource;

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
import org.molgenis.dataexplorer.freemarker.DataExplorerHyperlinkDirective;
import org.molgenis.system.core.FreemarkerTemplateRepository;
import org.molgenis.ui.MolgenisWebAppConfig;
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
		};
		localDataService.getMeta().setDefaultBackend(backend);

		for (EntityMetaData emd : localDataService.getMeta().getEntityMetaDatas())
		{
			if (emd.getBackend().equals(MysqlRepositoryCollection.NAME))
			{
				localDataService.addRepository(backend.addEntityMeta(emd));
			}
			else if (emd.getBackend().equals(JpaRepositoryCollection.NAME))
			{
				localDataService.addRepository(jpaRepositoryCollection.getUnderlying(emd.getName()));
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
