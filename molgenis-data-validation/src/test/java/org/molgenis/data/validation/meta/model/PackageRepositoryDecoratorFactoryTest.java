package org.molgenis.data.validation.meta.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.auth.GroupPackageService;
import org.molgenis.data.validation.meta.PackageValidator;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.MutableAclService;

class PackageRepositoryDecoratorFactoryTest extends AbstractMockitoTest {
  @Mock private PackageMetadata packageMetadata;
  @Mock private DataService dataService;
  @Mock private PackageValidator packageValidator;
  @Mock private MutableAclService mutableAclService;
  @Mock private UserPermissionEvaluator userPermissionEvaluator;
  @Mock private GroupPackageService groupPackageService;
  @Mock private MetaDataService metaDataService;

  private PackageRepositoryDecoratorFactory packageRepositoryDecoratorFactory;

  @BeforeEach
  void setUpBeforeMethod() {
    packageRepositoryDecoratorFactory =
        new PackageRepositoryDecoratorFactory(
            packageMetadata,
            dataService,
            packageValidator,
            mutableAclService,
            userPermissionEvaluator,
            groupPackageService,
            metaDataService);
  }

  @Test
  void testPackageRepositoryDecoratorFactory() {
    assertThrows(
        NullPointerException.class,
        () -> new PackageRepositoryDecoratorFactory(null, null, null, null, null, null, null));
  }

  @Test
  void testCreateDecoratedRepository() {
    EntityType entityType = mock(EntityType.class);
    Repository<Package> repository =
        when(mock(Repository.class).getEntityType()).thenReturn(entityType).getMock();
    Repository<Package> decoratedRepository =
        packageRepositoryDecoratorFactory.createDecoratedRepository(repository);
    assertEquals(entityType, decoratedRepository.getEntityType());
  }
}
