package org.molgenis.app;

import org.molgenis.DatabaseConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.config.HttpClientConfig;
import org.molgenis.data.elasticsearch.client.ElasticsearchConfig;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.molgenis.ui.freemarker.RepositoryTemplateLoader;
import org.molgenis.util.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableWebMvc
@EnableAsync
@ComponentScan(basePackages = "org.molgenis")
@Import({ WebAppSecurityConfig.class, DatabaseConfig.class, HttpClientConfig.class, ElasticsearchConfig.class,
		GsonConfig.class })
public class WebAppConfig extends MolgenisWebAppConfig
{
	@Autowired
	private DataService dataService;

	/**
	 * Configures Freemarker
	 */
	@Override
	public FreeMarkerConfigurer freeMarkerConfigurer()
	{
		FreeMarkerConfigurer result = super.freeMarkerConfigurer();
		// Look up unknown templates in the FreemarkerTemplate repository
		result.setPostTemplateLoaders(new RepositoryTemplateLoader(dataService));
		return result;
	}
}
