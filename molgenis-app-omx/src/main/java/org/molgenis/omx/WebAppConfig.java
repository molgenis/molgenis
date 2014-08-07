package org.molgenis.omx;

import java.io.IOException;
import java.util.Map;

import org.molgenis.DatabaseConfig;
import org.molgenis.catalogmanager.CatalogManagerService;
import org.molgenis.data.DataService;
import org.molgenis.dataexplorer.freemarker.DataExplorerHyperlinkDirective;
import org.molgenis.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.omx.catalogmanager.OmxCatalogManagerService;
import org.molgenis.omx.config.DataExplorerConfig;
import org.molgenis.omx.core.FreemarkerTemplateRepository;
import org.molgenis.omx.studymanager.OmxStudyManagerService;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.studymanager.StudyManagerService;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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
{ WebAppSecurityConfig.class, DatabaseConfig.class, OmxConfig.class, EmbeddedElasticSearchConfig.class,
		DataExplorerConfig.class })
public class WebAppConfig extends MolgenisWebAppConfig
{
	@Autowired
	private DataService dataService;
	@Autowired
	private MolgenisUserService molgenisUserService;
	@Autowired
	private FreemarkerTemplateRepository freemarkerTemplateRepository;

	@Bean
	@Qualifier("catalogService")
	public CatalogManagerService catalogManagerService()
	{
		return new OmxCatalogManagerService(dataService);
	}

	@Bean
	public StudyManagerService studyDefinitionManagerService()
	{
		return new OmxStudyManagerService(dataService, molgenisUserService);
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
