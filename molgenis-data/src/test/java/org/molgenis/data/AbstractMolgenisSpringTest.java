package org.molgenis.data;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.reset;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.config.MetadataTestConfig;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.util.GenericDependencyResolver;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {AbstractMolgenisSpringTest.Config.class})
public abstract class AbstractMolgenisSpringTest extends AbstractMockitoSpringContextTests {
  @Autowired private ApplicationContext applicationContext;
  @Autowired private Config config;

  public AbstractMolgenisSpringTest() {
    super();
  }

  public AbstractMolgenisSpringTest(Strictness strictness) {
    super(strictness);
  }

  private boolean isBootstrapped = false;

  // long method name, because if a method annotated with @BeforeEach and the
  // same method name
  // exists in a subclass then this method is ignored.
  @BeforeEach
  public void abstractMolgenisSpringTestBeforeMethod() {
    synchronized (this) {
      if (!isBootstrapped) {
        // bootstrap meta data
        EntityTypeMetadata entityTypeMeta = applicationContext.getBean(EntityTypeMetadata.class);
        entityTypeMeta.setBackendEnumOptions(newArrayList("test"));
        applicationContext.getBean(AttributeMetadata.class).bootstrap(entityTypeMeta);
        Map<String, SystemEntityType> systemEntityTypeMap =
            applicationContext.getBeansOfType(SystemEntityType.class);
        new GenericDependencyResolver()
            .resolve(systemEntityTypeMap.values(), SystemEntityType::getDependencies)
            .forEach(systemEntityType -> systemEntityType.bootstrap(entityTypeMeta));

        new ApplicationContextProvider().setApplicationContext(applicationContext);

        isBootstrapped = true;
      }
    }
    config.resetMocks();
  }

  @Configuration
  @Import(MetadataTestConfig.class)
  public static class Config {
    @Mock private DataService dataService;

    public Config() {
      org.mockito.MockitoAnnotations.initMocks(this);
    }

    public void resetMocks() {
      reset(dataService);
    }

    @Bean
    public DataService dataService() {
      return dataService;
    }

    @Bean
    public ConversionService conversionService() {
      return new DefaultFormattingConversionService();
    }
  }
}
