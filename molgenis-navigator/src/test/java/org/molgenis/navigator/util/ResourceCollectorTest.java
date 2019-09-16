package org.molgenis.navigator.util;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;
import org.molgenis.test.AbstractMockitoTest;

class ResourceCollectorTest extends AbstractMockitoTest {

  @Mock private MetaDataService metaDataService;

  private ResourceCollector resourceCollector;

  @BeforeEach
  void beforeMethod() {
    resourceCollector = new ResourceCollector(metaDataService);
  }

  @Test
  void testGet() {
    EntityType entityType1 = mock(EntityType.class);
    EntityType entityType2 = mock(EntityType.class);
    EntityType entityType3 = mock(EntityType.class);
    Package package1 = mock(Package.class);
    Package package2 = mock(Package.class);
    ResourceIdentifier id1 = ResourceIdentifier.create(ResourceType.ENTITY_TYPE, "entity1");
    ResourceIdentifier id2 = ResourceIdentifier.create(ResourceType.ENTITY_TYPE, "entity2");
    ResourceIdentifier id3 =
        ResourceIdentifier.create(ResourceType.ENTITY_TYPE_ABSTRACT, "entity3");
    ResourceIdentifier id4 = ResourceIdentifier.create(ResourceType.PACKAGE, "package1");
    ResourceIdentifier id5 = ResourceIdentifier.create(ResourceType.PACKAGE, "package2");
    when(metaDataService.getEntityType(any(String.class)))
        .thenAnswer(
            invocation -> {
              String id = invocation.getArgument(0);
              switch (id) {
                case "entity1":
                  return Optional.of(entityType1);
                case "entity2":
                  return Optional.of(entityType2);
                case "entity3":
                  return Optional.of(entityType3);
                default:
                  return null;
              }
            });
    when(metaDataService.getPackage(any(String.class)))
        .thenAnswer(
            invocation -> {
              String id = invocation.getArgument(0);
              switch (id) {
                case "package1":
                  return Optional.of(package1);
                case "package2":
                  return Optional.of(package2);
                default:
                  return null;
              }
            });

    ResourceCollection collection = resourceCollector.get(asList(id1, id2, id3, id4, id5));

    assertEquals(asList(entityType1, entityType2, entityType3), collection.getEntityTypes());
    assertEquals(asList(package1, package2), collection.getPackages());
  }

  @Test
  void testGetUnknownEntityType() {
    ResourceIdentifier id1 = ResourceIdentifier.create(ResourceType.ENTITY_TYPE, "entity1");
    when(metaDataService.getEntityType("entity1")).thenReturn(Optional.empty());

    assertThrows(UnknownEntityTypeException.class, () -> resourceCollector.get(singletonList(id1)));
  }

  @Test
  void testGetUnknownPackage() {
    ResourceIdentifier id1 = ResourceIdentifier.create(ResourceType.PACKAGE, "package1");
    when(metaDataService.getPackage("package1")).thenReturn(Optional.empty());

    assertThrows(UnknownPackageException.class, () -> resourceCollector.get(singletonList(id1)));
  }
}
