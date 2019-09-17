package org.molgenis.navigator.copy.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.navigator.model.ResourceIdentifier.create;
import static org.molgenis.navigator.model.ResourceType.ENTITY_TYPE;
import static org.molgenis.navigator.model.ResourceType.ENTITY_TYPE_ABSTRACT;
import static org.molgenis.navigator.model.ResourceType.PACKAGE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.i18n.ContextMessageSource;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.copy.exception.CopyFailedException;
import org.molgenis.navigator.copy.exception.RecursiveCopyException;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.util.ResourceCollection;
import org.molgenis.navigator.util.ResourceCollector;
import org.molgenis.test.AbstractMockitoTest;

class CopyServiceImplTest extends AbstractMockitoTest {

  @Mock private ResourceCollector resourceCollector;
  @Mock private MetaDataService metadataService;
  @Mock private PackageCopier packageCopier;
  @Mock private EntityTypeCopier entityTypeCopier;
  @Mock private ContextMessageSource contextMessageSource;
  private CopyServiceImpl copyService;

  @BeforeEach
  void beforeMethod() {
    copyService =
        new CopyServiceImpl(
            resourceCollector,
            metadataService,
            packageCopier,
            entityTypeCopier,
            contextMessageSource);
  }

  @Test
  void testCopyFailed() {
    ResourceCollection collection = mock(ResourceCollection.class);
    when(collection.getPackages()).thenReturn(singletonList(mock(Package.class)));
    when(resourceCollector.get(any())).thenReturn(collection);
    doThrow(new RecursiveCopyException()).when(packageCopier).copy(any(), any());

    Exception exception =
        assertThrows(
            CopyFailedException.class,
            () -> copyService.copy(emptyList(), null, mock(Progress.class)));
    assertThat(exception.getMessage())
        .containsPattern("The target package is a subpackage of the package being copied");
  }

  @Test
  void testMaxProgress() {
    ResourceIdentifier id1 = create(ENTITY_TYPE, "e1");
    ResourceIdentifier id2 = create(ENTITY_TYPE_ABSTRACT, "e2");
    ResourceIdentifier id3 = create(PACKAGE, "p1");
    EntityType entityType1 = mock(EntityType.class);
    EntityType entityType2 = mock(EntityType.class);
    Package pack = mock(Package.class);
    Package childPack1 = mock(Package.class);
    Package childPack2 = mock(Package.class);
    when(childPack2.getEntityTypes()).thenReturn(singletonList(mock(EntityType.class)));
    when(pack.getChildren()).thenReturn(asList(childPack1, childPack2));
    ResourceCollection collection =
        ResourceCollection.of(singletonList(pack), asList(entityType1, entityType2));
    when(resourceCollector.get(asList(id1, id2, id3))).thenReturn(collection);
    Progress progress = mock(Progress.class);

    copyService.copy(asList(id1, id2, id3), null, progress);

    verify(progress).setProgressMax(6);
  }

  @Test
  void testCopy() {
    ResourceIdentifier id1 = create(ENTITY_TYPE, "e1");
    ResourceIdentifier id2 = create(ENTITY_TYPE_ABSTRACT, "e2");
    ResourceIdentifier id3 = create(PACKAGE, "p1");
    EntityType entityType1 = mock(EntityType.class);
    EntityType entityType2 = mock(EntityType.class);
    Package pack = mock(Package.class);
    ResourceCollection collection =
        ResourceCollection.of(singletonList(pack), asList(entityType1, entityType2));
    when(resourceCollector.get(asList(id1, id2, id3))).thenReturn(collection);
    Progress progress = mock(Progress.class);

    copyService.copy(asList(id1, id2, id3), null, progress);

    verify(packageCopier).copy(eq(singletonList(pack)), any(CopyState.class));
    verify(entityTypeCopier).copy(eq(asList(entityType1, entityType2)), any(CopyState.class));
  }

  @Test
  void testProgressMessages() {
    ResourceIdentifier id1 = create(ENTITY_TYPE, "e1");
    ResourceIdentifier id2 = create(PACKAGE, "p1");
    EntityType entityType1 = mock(EntityType.class);
    Package pack = mock(Package.class);
    ResourceCollection collection =
        ResourceCollection.of(singletonList(pack), singletonList(entityType1));
    when(resourceCollector.get(asList(id1, id2))).thenReturn(collection);
    Progress progress = mock(Progress.class);
    setupContextMessageSourceAnswers();

    copyService.copy(asList(id1, id2), null, progress);

    verify(progress).progress(0, "started");
    verify(progress).status("copying packages");
    verify(progress).status("copying entity types");
    verify(progress).status("success");
  }

  @Test
  void testUnknownTargetPackage() {
    Exception exception =
        assertThrows(
            UnknownPackageException.class,
            () -> copyService.copy(emptyList(), "it_emx_datawipes", mock(Progress.class)));
    assertThat(exception.getMessage()).containsPattern("package:it_emx_datawipes");
  }

  private void setupContextMessageSourceAnswers() {
    when(contextMessageSource.getMessage(any(String.class)))
        .thenAnswer(
            (Answer<String>)
                invocation -> {
                  String key = invocation.getArgument(0).toString();
                  switch (key) {
                    case "progress-copy-started":
                      return "started";
                    case "progress-copy-success":
                      return "success";
                    case "progress-copy-packages":
                      return "copying packages";
                    case "progress-copy-entity-types":
                      return "copying entity types";
                    default:
                      return null;
                  }
                });
  }
}
