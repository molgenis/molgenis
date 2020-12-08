package org.molgenis.data.security.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.acl.MutableSidService;
import org.molgenis.test.AbstractMockitoTest;

class RoleRepositoryDecoratorFactoryTest extends AbstractMockitoTest {

  @Mock private RoleMetadata roleMetadata;
  @Mock private CachedRoleHierarchy cachedRoleHierarchy;
  @Mock private MutableSidService mutableSidService;
  private RoleRepositoryDecoratorFactory roleRepositoryDecoratorFactory;

  @BeforeEach
  void setUpBeforeEach() {
    roleRepositoryDecoratorFactory =
        new RoleRepositoryDecoratorFactory(roleMetadata, cachedRoleHierarchy, mutableSidService);
  }

  @Test
  void createDecoratedRepository() {
    @SuppressWarnings("unchecked")
    Repository<Role> repository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Repository<Role> decoratedRepository =
        roleRepositoryDecoratorFactory.createDecoratedRepository(repository);
    assertEquals(entityType, decoratedRepository.getEntityType());
  }
}
