package org.molgenis.app;

import java.io.IOException;
import java.util.Map;

import org.molgenis.DatabaseConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.data.system.RepositoryTemplateLoader;
import org.molgenis.dataexplorer.freemarker.DataExplorerHyperlinkDirective;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.system.core.FreemarkerTemplateRepository;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
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
{ WebAppSecurityConfig.class, DatabaseConfig.class, EmbeddedElasticSearchConfig.class })
public class WebAppConfig extends MolgenisWebAppConfig
{
	@Autowired
	private DataService dataService;
	@Autowired
	private MolgenisUserService molgenisUserService;
	@Autowired
	private FreemarkerTemplateRepository freemarkerTemplateRepository;

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
