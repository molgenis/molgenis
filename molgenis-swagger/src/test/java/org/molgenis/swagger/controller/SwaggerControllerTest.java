package org.molgenis.swagger.controller;

import com.google.common.collect.Maps;
import freemarker.template.TemplateException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.ui.freemarker.LimitMethod;
import org.molgenis.ui.freemarker.MolgenisFreemarkerObjectWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static freemarker.template.Configuration.VERSION_2_3_23;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { SwaggerControllerTest.FreemarkerConfig.class, SwaggerController.class })
public class SwaggerControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MetaDataService metaDataService;

	@Mock
	EntityType type1;
	@Mock
	EntityType type2;

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		Mockito.reset(metaDataService, type1, type2);
	}

	@Test
	public void testInitRedirectsToYml() throws Exception
	{
		this.mockMvc.perform(get("http://localhost:8080/plugin/swagger")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(
						"http://localhost:8080/swagger-ui/index.html?url=http://localhost:8080/plugin/swagger/swagger.yml"));
	}

	@Test
	public void testYmlUrlServesSwagger() throws Exception
	{
		when(metaDataService.getEntityTypes()).thenReturn(Stream.of(type1, type2));
		when(type1.getName()).thenReturn("EntityType1");
		when(type2.getName()).thenReturn("abc_EntityType2ëæ");

		this.mockMvc.perform(get("http://localhost:8080/plugin/swagger/swagger.yml")).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/yaml")).andExpect(content().encoding("UTF-8"))
				.andExpect(content().string(containsString("- EntityType1")))
				.andExpect(content().string(containsString("- abc_EntityType2ëæ")));
	}

	@Configuration
	public static class FreemarkerConfig
	{
		/**
		 * Enable spring freemarker viewresolver. All freemarker template names should end with '.ftl'
		 */
		@Bean
		public ViewResolver viewResolver()
		{
			FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
			resolver.setCache(true);
			resolver.setSuffix(".ftl");
			resolver.setContentType("text/html;charset=UTF-8");
			return resolver;
		}

		/**
		 * Configure freemarker. All freemarker templates should be on the classpath in a package called 'freemarker'
		 *
		 * @throws TemplateException
		 * @throws IOException
		 */
		@Bean
		public FreeMarkerConfigurer freeMarkerConfigurer() throws IOException, TemplateException
		{
			FreeMarkerConfigurer result = new FreeMarkerConfigurer()
			{
				@Override
				protected void postProcessConfiguration(freemarker.template.Configuration config)
						throws IOException, TemplateException
				{
					config.setObjectWrapper(new MolgenisFreemarkerObjectWrapper(VERSION_2_3_23));
				}
			};
			result.setPreferFileSystemAccess(false);
			result.setTemplateLoaderPath("classpath:/templates/");
			result.setDefaultEncoding("UTF-8");
			Properties freemarkerSettings = new Properties();
			freemarkerSettings
					.setProperty(freemarker.template.Configuration.LOCALIZED_LOOKUP_KEY, Boolean.FALSE.toString());
			result.setFreemarkerSettings(freemarkerSettings);
			Map<String, Object> freemarkerVariables = Maps.newHashMap();
			freemarkerVariables.put("limit", new LimitMethod());

			result.setFreemarkerVariables(freemarkerVariables);

			return result;
		}

		@Bean
		public MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public MolgenisPluginRegistry molgenisPluginRegistry()
		{
			return mock(MolgenisPluginRegistry.class);
		}

	}
}
