package org.molgenis.bootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RegistryBootstrapperTest extends AbstractMockitoTest {
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
  private RegistryBootstrapper registryBootstrapper;

  @BeforeMethod
  public void setUpBeforeMethod() {
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
            jobFactoryRegistrar);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testRegistryBootstrapper() {
    new RegistryBootstrapper(null, null, null, null, null, null, null, null, null);
  }

  @Test
  public void testBootstrap() {
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
  }
}
