package org.molgenis.omx;

import java.util.Map;

import org.molgenis.DatabaseConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.EntitySourceFactory;
import org.molgenis.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.omx.config.DataExplorerConfig;
import org.molgenis.search.SearchSecurityConfig;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableTransactionManagement
@EnableWebMvc
@EnableAsync
@ComponentScan("org.molgenis")
@Import(
{ WebAppSecurityConfig.class, DatabaseConfig.class, OmxConfig.class, EmbeddedElasticSearchConfig.class,
		DataExplorerConfig.class, SearchSecurityConfig.class })
public class WebAppConfig extends MolgenisWebAppConfig implements ApplicationContextAware
{
	@Autowired
	private DataService dataService;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		// TODO see if @PostConstruct works
		Map<String, EntitySourceFactory> factories = applicationContext.getBeansOfType(EntitySourceFactory.class);
		for (EntitySourceFactory factory : factories.values())
		{
			dataService.registerFactory(factory);
		}

		dataService.registerEntitySource("jpa://");
	}

	/*
	 * @Autowired private Database database;
	 * 
	 * @Bean public CatalogManagerService catalogManagerService() { return new OmxCatalogManagerService(database); }
	 * 
	 * @Bean public StudyManagerService studyDefinitionManagerService() { return new OmxStudyManagerService(database); }
	 */
}