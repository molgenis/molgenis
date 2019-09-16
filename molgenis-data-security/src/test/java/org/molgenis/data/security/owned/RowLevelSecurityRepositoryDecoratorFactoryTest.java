package org.molgenis.data.security.owned;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.MutableAclService;

class RowLevelSecurityRepositoryDecoratorFactoryTest extends AbstractMockitoTest {
  @Mock private UserPermissionEvaluator userPermissionEvaluator;
  @Mock private MutableAclService mutableAclService;
  @Mock private MutableAclClassService mutableAclClassService;
  private RowLevelSecurityRepositoryDecoratorFactory rowLevelSecurityRepositoryDecoratorFactory;

  @BeforeEach
  void setUpBeforeMethod() {
    rowLevelSecurityRepositoryDecoratorFactory =
        new RowLevelSecurityRepositoryDecoratorFactory(
            userPermissionEvaluator, mutableAclService, mutableAclClassService);
  }

  @Test
  void testRowLevelSecurityRepositoryDecoratorFactory() {
    assertThrows(
        NullPointerException.class,
        () -> new RowLevelSecurityRepositoryDecoratorFactory(null, null, null));
  }

  @Test
  void testCreateDecoratedRepositoryRowLevelSecurityEnabled() {
    Repository<Entity> repository = getRepositoryMock();
    when(mutableAclClassService.hasAclClass("entity-entityTypeId")).thenReturn(true);
    assertTrue(
        rowLevelSecurityRepositoryDecoratorFactory.createDecoratedRepository(repository)
            instanceof RowLevelSecurityRepositoryDecorator);
  }

  @Test
  void testCreateDecoratedRepositoryRowLevelSecurityDisabled() {
    Repository<Entity> repository = getRepositoryMock();
    assertEquals(
        repository,
        rowLevelSecurityRepositoryDecoratorFactory.createDecoratedRepository(repository));
  }

  private Repository<Entity> getRepositoryMock() {
    EntityType entityType =
        when(mock(EntityType.class).getId()).thenReturn("entityTypeId").getMock();
    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    return repository;
  }
}
