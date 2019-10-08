package org.molgenis.integrationtest.utils;

import static org.molgenis.integrationtest.config.SecurityITConfig.SUPERUSER_NAME;
import static org.molgenis.integrationtest.config.SecurityITConfig.TOKEN_DESCRIPTION;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.postgresql.DatabaseConfig;
import org.molgenis.integrationtest.config.BootStrapperTestConfig;
import org.molgenis.integrationtest.config.FileTestConfig;
import org.molgenis.integrationtest.config.PostgreSqlTestConfig;
import org.molgenis.integrationtest.config.SecurityITConfig;
import org.molgenis.integrationtest.config.WebAppITConfig;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@WebAppConfiguration
@ContextConfiguration(classes = AbstractMolgenisIntegrationTests.Config.class)
public abstract class AbstractMolgenisIntegrationTests extends AbstractMockitoSpringContextTests {
  @Autowired protected WebApplicationContext context;

  protected MockMvc mockMvc;

  @Autowired private BootstrapTestUtils bootstrapTestUtils;
  @Autowired private TokenService tokenService;
  @Autowired private FileStore fileStore;

  private String adminToken;

  @BeforeEach
  public void superBeforeMethod() {
    if (mockMvc == null) {
      mockMvc = webAppContextSetup(context).apply(springSecurity()).alwaysDo(print()).build();

      bootstrapTestUtils.bootstrap(context);
    }

    beforeMethod();
  }

  /**
   * Use this method as a beforeMethod in the integration test
   *
   * <p>Do not annotate this method to ensure this method is called in the <code>beforeMethodSetup
   * </code>
   */
  public abstract void beforeMethod();

  @AfterEach
  public void superAfterEach() throws IOException {
    fileStore.deleteDirectory(fileStore.getStorageDir());
    afterEach();
  }

  /** Use this class to implement in your own integration test. */
  public abstract void afterEach();

  protected String getAdminToken() {
    if (adminToken == null) {
      adminToken = tokenService.generateAndStoreToken(SUPERUSER_NAME, TOKEN_DESCRIPTION);
    }
    return adminToken;
  }

  /**
   * The {@link ApplicationContextProvider} must be in this configuration because of the autowiring
   * from context
   */
  @Configuration
  // Use this annotation in your controller configuration to test the GetMapping annotations
  @EnableWebMvc
  @EnableAspectJAutoProxy
  @Import({
    BootstrapTestUtils.class,
    BootStrapperTestConfig.class,
    WebAppITConfig.class,
    SecurityITConfig.class,
    PostgreSqlTestConfig.class,
    FileTestConfig.class,
    DatabaseConfig.class
  })
  static class Config {
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
      PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
      Resource[] resources = new Resource[] {new ClassPathResource("/conf/molgenis.properties")};
      pspc.setLocations(resources);
      pspc.setFileEncoding("UTF-8");
      pspc.setIgnoreUnresolvablePlaceholders(true);
      pspc.setIgnoreResourceNotFound(true);
      pspc.setNullValue("@null");
      return pspc;
    }
  }
}
