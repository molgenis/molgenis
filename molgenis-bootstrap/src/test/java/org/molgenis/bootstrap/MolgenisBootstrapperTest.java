package org.molgenis.bootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.bootstrap.populate.PermissionPopulator;
import org.molgenis.bootstrap.populate.RepositoryPopulator;
import org.molgenis.core.ui.style.BootstrapThemePopulator;
import org.molgenis.data.event.BootstrappingEventPublisher;
import org.molgenis.data.index.bootstrap.IndexBootstrapper;
import org.molgenis.data.migrate.bootstrap.MolgenisUpgradeBootstrapper;
import org.molgenis.data.platform.bootstrap.SystemEntityTypeBootstrapper;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistryPopulator;
import org.molgenis.data.transaction.TransactionExceptionTranslatorRegistrar;
import org.molgenis.jobs.JobBootstrapper;
import org.molgenis.security.acl.DataSourceAclTablesPopulator;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisBootstrapperTest extends AbstractMockitoTest {
  @Mock private MolgenisUpgradeBootstrapper upgradeBootstrapper;
  @Mock private DataSourceAclTablesPopulator dataSourceAclTablesPopulator;
  @Mock private TransactionExceptionTranslatorRegistrar transactionExceptionTranslatorRegistrar;
  @Mock private RegistryBootstrapper registryBootstrapper;
  @Mock private SystemEntityTypeBootstrapper systemEntityTypeBootstrapper;
  @Mock private RepositoryPopulator repositoryPopulator;
  @Mock private PermissionPopulator systemPermissionPopulator;
  @Mock private JobBootstrapper jobBootstrapper;
  @Mock private IndexBootstrapper indexBootstrapper;
  @Mock private EntityTypeRegistryPopulator entityTypeRegistryPopulator;
  @Mock private BootstrapThemePopulator bootstrapThemePopulator;
  @Mock private BootstrappingEventPublisher bootstrappingEventPublisher;

  private MolgenisBootstrapper molgenisBootstrapper;

  @BeforeMethod
  public void setUpBeforeMethod() {
    molgenisBootstrapper =
        new MolgenisBootstrapper(
            upgradeBootstrapper,
            dataSourceAclTablesPopulator,
            transactionExceptionTranslatorRegistrar,
            registryBootstrapper,
            systemEntityTypeBootstrapper,
            repositoryPopulator,
            systemPermissionPopulator,
            jobBootstrapper,
            indexBootstrapper,
            entityTypeRegistryPopulator,
            bootstrapThemePopulator,
            bootstrappingEventPublisher);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testMolgenisBootstrapper() {
    new MolgenisBootstrapper(
        null, null, null, null, null, null, null, null, null, null, null, null);
  }

  @Test
  public void testOnApplicationEvent() {
    ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);
    ApplicationContext applicationContext = mock(ApplicationContext.class);
    when(event.getApplicationContext()).thenReturn(applicationContext);

    molgenisBootstrapper.onApplicationEvent(event);

    verify(bootstrappingEventPublisher).publishBootstrappingStartedEvent();
    verify(upgradeBootstrapper).bootstrap();
    verify(dataSourceAclTablesPopulator).populate();
    verify(transactionExceptionTranslatorRegistrar).register(applicationContext);
    verify(registryBootstrapper).bootstrap(event);
    verify(systemEntityTypeBootstrapper).bootstrap(event);
    verify(repositoryPopulator).populate(event);
    verify(systemPermissionPopulator).populate(applicationContext);
    verify(jobBootstrapper).bootstrap();
    verify(indexBootstrapper).bootstrap();
    verify(entityTypeRegistryPopulator).populate();
    verify(bootstrapThemePopulator).populate();
    verify(bootstrappingEventPublisher).publishBootstrappingFinishedEvent();
  }

  @Test
  public void testGetOrder() {
    assertEquals(molgenisBootstrapper.getOrder(), Integer.MIN_VALUE);
  }
}
