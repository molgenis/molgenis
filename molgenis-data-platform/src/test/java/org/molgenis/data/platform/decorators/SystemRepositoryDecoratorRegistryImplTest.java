package org.molgenis.data.platform.decorators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.SystemRepositoryDecoratorFactory;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SystemRepositoryDecoratorRegistryImplTest extends AbstractMockitoTest {

  private SystemRepositoryDecoratorRegistryImpl systemRepositoryDecoratorRegistryImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    systemRepositoryDecoratorRegistryImpl = new SystemRepositoryDecoratorRegistryImpl();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDecorate() {
    String grandparentEntityTypeId = "grandparentEntityTypeId";
    SystemEntityType grandparentEntityType = mock(SystemEntityType.class);
    when(grandparentEntityType.getId()).thenReturn(grandparentEntityTypeId);

    String parentEntityTypeId = "parentEntityTypeId";
    SystemEntityType parentEntityType = mock(SystemEntityType.class);
    when(parentEntityType.getId()).thenReturn(parentEntityTypeId);
    when(parentEntityType.getExtends()).thenReturn(grandparentEntityType);

    String entityTypeId = "entityTypeId";
    SystemEntityType entityType = mock(SystemEntityType.class);
    when(entityType.getId()).thenReturn(entityTypeId);
    when(entityType.getExtends()).thenReturn(parentEntityType);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);

    SystemRepositoryDecoratorFactory entityTypeFactory =
        mock(SystemRepositoryDecoratorFactory.class);
    when(entityTypeFactory.getEntityType()).thenReturn(entityType);
    systemRepositoryDecoratorRegistryImpl.addFactory(entityTypeFactory);

    SystemRepositoryDecoratorFactory grandparentEntityTypeFactory =
        mock(SystemRepositoryDecoratorFactory.class);
    when(grandparentEntityTypeFactory.getEntityType()).thenReturn(grandparentEntityType);
    systemRepositoryDecoratorRegistryImpl.addFactory(grandparentEntityTypeFactory);

    Repository<Entity> decoratedRepository = mock(Repository.class);
    when(grandparentEntityTypeFactory.createDecoratedRepository(repository))
        .thenReturn(decoratedRepository);

    Repository decoratedDecoratedRepository = mock(Repository.class);
    when(entityTypeFactory.createDecoratedRepository(decoratedRepository))
        .thenReturn(decoratedDecoratedRepository);
    assertEquals(
        systemRepositoryDecoratorRegistryImpl.decorate(repository), decoratedDecoratedRepository);
  }

  @Test
  public void testDecorateNoFactory() {
    String entityTypeId = "entityTypeId";
    SystemEntityType entityType = mock(SystemEntityType.class);
    when(entityType.getId()).thenReturn(entityTypeId);

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    assertEquals(systemRepositoryDecoratorRegistryImpl.decorate(repository), repository);
  }
}
