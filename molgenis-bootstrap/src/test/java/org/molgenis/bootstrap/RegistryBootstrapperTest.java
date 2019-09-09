package org.molgenis.bootstrap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.EntityFactoryRegistrar;
import org.molgenis.data.RepositoryCollectionBootstrapper;
import org.molgenis.data.SystemRepositoryDecoratorFactoryRegistrar;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactoryRegistrar;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.meta.system.SystemEntityTypeRegistrar;
import org.molgenis.data.meta.system.SystemPackageRegistrar;
import org.molgenis.jobs.JobFactoryRegistrar;
import org.molgenis.script.core.ScriptRunnerRegistrar;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.exception.ExceptionResponseGeneratorRegistrar;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

class RegistryBootstrapperTest extends AbstractMockitoTest {
  @Mock private RepositoryCollectionBootstrapper repoCollectionBootstrapper;
  @Mock private SystemEntityTypeRegistrar systemEntityTypeRegistrar;
  @Mock private SystemPackageRegistrar systemPackageRegistrar;
  @Mock private EntityFactoryRegistrar entityFactoryRegistrar;
  @Mock private SystemRepositoryDecoratorFactoryRegistrar systemRepositoryDecoratorFactoryRegistrar;

  @Mock
  private DynamicRepositoryDecoratorFactoryRegistrar dynamicRepositoryDecoratorFactoryRegistrar;

  @Mock private ImportServiceRegistrar importServiceRegistrar;
  @Mock private ScriptRunnerRegistrar scriptRunnerRegistrar;
  @Mock private JobFactoryRegistrar jobFactoryRegistrar;
  @Mock private ExceptionResponseGeneratorRegistrar exceptionResponseGeneratorRegistrar;
  private RegistryBootstrapper registryBootstrapper;

  @BeforeEach
  void setUpBeforeMethod() {
    registryBootstrapper =
        new RegistryBootstrapper(
            repoCollectionBootstrapper,
            systemEntityTypeRegistrar,
            systemPackageRegistrar,
            entityFactoryRegistrar,
            systemRepositoryDecoratorFactoryRegistrar,
            dynamicRepositoryDecoratorFactoryRegistrar,
            importServiceRegistrar,
            scriptRunnerRegistrar,
            jobFactoryRegistrar,
            exceptionResponseGeneratorRegistrar);
  }

  @Test
  void testRegistryBootstrapper() {
    assertThrows(
        NullPointerException.class,
        () -> new RegistryBootstrapper(null, null, null, null, null, null, null, null, null, null));
  }

  @Test
  void testBootstrap() {
    ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);
    ApplicationContext applicationContext = mock(ApplicationContext.class);
    when(event.getApplicationContext()).thenReturn(applicationContext);

    registryBootstrapper.bootstrap(event);
    verify(repoCollectionBootstrapper).bootstrap(event, "PostgreSQL");
    verify(systemEntityTypeRegistrar).register(event);
    verify(systemPackageRegistrar).register(event);
    verify(entityFactoryRegistrar).register(event);
    verify(systemRepositoryDecoratorFactoryRegistrar).register(event);
    verify(dynamicRepositoryDecoratorFactoryRegistrar).register(applicationContext);
    verify(importServiceRegistrar).register(event);
    verify(scriptRunnerRegistrar).register(event);
    verify(jobFactoryRegistrar).register(event);
    verify(exceptionResponseGeneratorRegistrar).register(applicationContext);
  }
}
