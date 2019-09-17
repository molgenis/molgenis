package org.molgenis.navigator.delete;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

import java.util.HashSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.i18n.ContextMessageSource;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;
import org.molgenis.test.AbstractMockitoTest;

class ResourceDeleteServiceImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private ContextMessageSource contextMessageSource;
  private ResourceDeleteServiceImpl resourceDeleteServiceImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    resourceDeleteServiceImpl = new ResourceDeleteServiceImpl(dataService, contextMessageSource);
  }

  @Test
  void testResourceDeleteServiceImpl() {
    assertThrows(NullPointerException.class, () -> new ResourceDeleteServiceImpl(null, null));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteResourcesPackagesAndEntityTypes() {
    EntityType entityTypeA0 = mock(EntityType.class);
    EntityType entityTypeA1 = mock(EntityType.class);

    Package packageA = mock(Package.class);
    Package packageB = mock(Package.class);

    doReturn(Stream.of(packageA, packageB))
        .when(dataService)
        .findAll(eq(PACKAGE), any(Stream.class), eq(Package.class));

    doReturn(Stream.of(entityTypeA0, entityTypeA1))
        .when(dataService)
        .findAll(eq(ENTITY_TYPE_META_DATA), any(Stream.class), eq(EntityType.class));

    Progress progress = mock(Progress.class);

    when(contextMessageSource.getMessage(any(String.class)))
        .thenAnswer(
            (Answer<String>)
                invocation -> {
                  String key = invocation.getArgument(0).toString();
                  switch (key) {
                    case "progress-delete-started":
                      return "started";
                    case "progress-delete-success":
                      return "success";
                    default:
                      return null;
                  }
                });

    resourceDeleteServiceImpl.delete(
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("pA").build(),
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("pB").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("eA0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("eA1").build()),
        progress);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<EntityType>> entityTypeCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).delete(eq(ENTITY_TYPE_META_DATA), entityTypeCaptor.capture());
    assertEquals(
        new HashSet<>(asList(entityTypeA0, entityTypeA1)),
        entityTypeCaptor.getValue().collect(toSet()));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).delete(eq(PACKAGE), packageCaptor.capture());
    assertEquals(
        new HashSet<>(asList(packageA, packageB)), packageCaptor.getValue().collect(toSet()));

    verify(progress).status("started");
    verify(progress).status("success");
    verifyNoMoreInteractions(dataService);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteResourcesPackages() {
    Package packageA = mock(Package.class);
    Package packageB = mock(Package.class);

    doReturn(Stream.of(packageA, packageB))
        .when(dataService)
        .findAll(eq(PACKAGE), any(Stream.class), eq(Package.class));

    Progress progress = mock(Progress.class);
    resourceDeleteServiceImpl.delete(
        asList(
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("pA").build(),
            ResourceIdentifier.builder().setType(ResourceType.PACKAGE).setId("pB").build()),
        progress);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).delete(eq(PACKAGE), packageCaptor.capture());
    assertEquals(
        new HashSet<>(asList(packageA, packageB)), packageCaptor.getValue().collect(toSet()));

    verifyNoMoreInteractions(dataService);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteResourcesEntityTypes() {

    EntityType entityTypeA0 = mock(EntityType.class);
    EntityType entityTypeA1 = mock(EntityType.class);

    doReturn(Stream.of(entityTypeA0, entityTypeA1))
        .when(dataService)
        .findAll(eq(ENTITY_TYPE_META_DATA), any(Stream.class), eq(EntityType.class));

    Progress progress = mock(Progress.class);
    resourceDeleteServiceImpl.delete(
        asList(
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e0").build(),
            ResourceIdentifier.builder().setType(ResourceType.ENTITY_TYPE).setId("e1").build()),
        progress);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<EntityType>> entityTypeCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).delete(eq(ENTITY_TYPE_META_DATA), entityTypeCaptor.capture());
    assertEquals(
        new HashSet<>(asList(entityTypeA0, entityTypeA1)),
        entityTypeCaptor.getValue().collect(toSet()));

    verifyNoMoreInteractions(dataService);
  }

  @Test
  void testDeleteResourcesNothing() {
    resourceDeleteServiceImpl.delete(emptyList(), mock(Progress.class));
    verifyZeroInteractions(dataService);
  }
}
