package org.molgenis.integrationtest.utils;

import org.molgenis.integrationtest.data.platform.BootStrapperConfig;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlConfig;
import org.molgenis.integrationtest.utils.config.SecurityITConfig;
import org.molgenis.integrationtest.utils.config.WebAppITConfig;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.token.DataServiceTokenService;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.integrationtest.utils.config.SecurityITConfig.SUPERUSER_NAME;
import static org.molgenis.integrationtest.utils.config.SecurityITConfig.TOKEN_DESCRIPTION;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebAppConfiguration
@TestPropertySource("/conf/molgenis.properties")
@ContextConfiguration(classes = AbstractMolgenisIntegrationTests.Config.class)
public abstract class AbstractMolgenisIntegrationTests extends AbstractTestNGSpringContextTests
{
	@Autowired
	protected WebApplicationContext context;

	protected MockMvc mockMvc;

	@Autowired
	private BootstrapTestUtils bootstrapTestUtils;
	@Autowired
	private TokenService tokenService;

	private String adminToken;

	@BeforeMethod
	public void beforeMethodSetup()
	{
		initMocks(this);
		if (mockMvc == null)
		{
			mockMvc = webAppContextSetup(context).apply(springSecurity()).alwaysDo(print()).build();

			bootstrapTestUtils.bootstrap(context);
		}

		beforeMethod();
	}

	/**
	 * <p>Use this method as a beforeMethod in the integration test</p>
	 * <p>Do not annotate this method to ensure this method is called in the <code>beforeMethodSetup</code></p>
	 */
	public abstract void beforeMethod();

	protected String getAdminToken()
	{
		if (adminToken == null)
		{
			adminToken = tokenService.generateAndStoreToken(SUPERUSER_NAME, TOKEN_DESCRIPTION);
		}
		return adminToken;
	}

	/**
	 * <p>The {@link ApplicationContextProvider} must be in this configuration because of the autowiring from context</p>
	 */
	@Configuration
	@Import({ BootstrapTestUtils.class, BootStrapperConfig.class, DataServiceTokenService.class, WebAppITConfig.class,
			SecurityITConfig.class, PostgreSqlConfig.class })
	static class Config
	{
		@Bean
		public ApplicationContextProvider applicationContextProvider()
		{
			return new ApplicationContextProvider();
		}
	}
}
