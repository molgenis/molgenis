package org.molgenis.navigator.copy.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.copy.exception.RecursiveCopyException;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PackageCopierTest extends AbstractMockitoTest {

  @Mock(answer = RETURNS_DEEP_STUBS)
  DataService dataService;

  @Mock MetaDataService metaDataService;
  @Mock IdGenerator idGenerator;

  private PackageCopier copier;

  @BeforeMethod
  public void beforeMethod() {
    copier = new PackageCopier(dataService, idGenerator);
  }

  @Test(expectedExceptions = RecursiveCopyException.class)
  public void notContainsItself() {
    Package packageA = mock(Package.class);
    Package packageAa = mock(Package.class);
    Package packageAb = mock(Package.class);
    Package packageAba = mock(Package.class);
    when(packageA.getChildren()).thenReturn(asList(packageAa, packageAb));
    when(packageAb.getChildren()).thenReturn(singletonList(packageAba));

    CopyState state = CopyState.create(packageAba, mock(Progress.class));
    copier.copy(singletonList(packageA), state);
  }

  @Test
  public void testUniqueLabel() {
    Package packageA = mock(Package.class);
    when(packageA.getLabel()).thenReturn("A");
    Package targetPackage = mock(Package.class);
    Package packageInTarget = mock(Package.class);
    when(targetPackage.getChildren()).thenReturn(singletonList(packageInTarget));
    when(packageInTarget.getLabel()).thenReturn("A");

    CopyState state = CopyState.create(targetPackage, mock(Progress.class));
    copier.copy(singletonList(packageA), state);

    verify(packageA).setLabel("A (Copy)");
  }

  @Test
  public void testUniqueLabelNoChange() {
    Package packageA = mock(Package.class);
    when(packageA.getLabel()).thenReturn("A");
    Package targetPackage = mock(Package.class);
    Package packageInTarget = mock(Package.class);
    when(targetPackage.getChildren()).thenReturn(singletonList(packageInTarget));
    when(packageInTarget.getLabel()).thenReturn("B");

    CopyState state = CopyState.create(targetPackage, mock(Progress.class));
    copier.copy(singletonList(packageA), state);

    verify(packageA).setLabel("A");
  }

  @Test
  public void testUniqueLabelRootPackage() {
    Package packageA = mock(Package.class);
    when(packageA.getLabel()).thenReturn("A");
    Package packageInRoot = mock(Package.class);
    when(packageInRoot.getLabel()).thenReturn("A");
    when(dataService.query(PACKAGE, Package.class).eq(PackageMetadata.PARENT, null).findAll())
        .thenReturn(Stream.of(packageInRoot));

    CopyState state = CopyState.create(null, mock(Progress.class));
    copier.copy(singletonList(packageA), state);

    verify(packageA).setLabel("A (Copy)");
  }

  @Test
  public void testCopyPackage() {
    setupPredictableIdGeneratorMock();
    EntityType entityType1 = mock(EntityType.class);
    EntityType entityType2 = mock(EntityType.class);
    EntityType entityType3 = mock(EntityType.class);
    Package packageA = mock(Package.class);
    Package packageAa = mock(Package.class);
    Package packageAb = mock(Package.class);
    Package packageAba = mock(Package.class);
    Package packageC = mock(Package.class);
    when(packageA.getChildren()).thenReturn(asList(packageAa, packageAb));
    when(packageAb.getChildren()).thenReturn(singletonList(packageAba));
    when(packageAba.getEntityTypes()).thenReturn(asList(entityType1, entityType2));
    when(packageC.getEntityTypes()).thenReturn(singletonList(entityType3));
    when(packageA.getId()).thenReturn("A");
    when(packageAa.getId()).thenReturn("Aa");
    when(packageAb.getId()).thenReturn("Ab");
    when(packageAba.getId()).thenReturn("Aba");
    when(packageC.getId()).thenReturn("C");
    when(packageA.getLabel()).thenReturn("labelA");
    when(packageC.getLabel()).thenReturn("labelC");
    Package targetPackage = mock(Package.class);
    when(dataService.getMeta()).thenReturn(metaDataService);
    setupMetadataServiceAnswers(asList(packageA, packageAa, packageAb, packageAba, packageC));

    Progress progress = mock(Progress.class);
    CopyState state = CopyState.create(targetPackage, progress);
    copier.copy(asList(packageA, packageC), state);

    verify(packageA).setParent(targetPackage);
    verify(packageA).setId("id1");
    verify(packageAa).setId("id2");
    verify(packageAb).setId("id3");
    verify(packageAba).setId("id4");
    verify(packageC).setId("id5");
    verify(dataService).add(PACKAGE, packageA);
    verify(dataService).add(PACKAGE, packageAa);
    verify(dataService).add(PACKAGE, packageAb);
    verify(dataService).add(PACKAGE, packageAba);
    verify(dataService).add(PACKAGE, packageC);
    verify(progress, times(5)).increment(1);
    assertEquals(
        state.copiedPackages(),
        ImmutableMap.of(
            "A", packageA, "Aa", packageAa, "Ab", packageAb, "Aba", packageAba, "C", packageC));
    assertEquals(state.entityTypesInPackages(), asList(entityType1, entityType2, entityType3));
  }

  private void setupMetadataServiceAnswers(List<Package> packageMocks) {
    Map<String, Package> packages =
        packageMocks.stream().collect(toMap(Package::getId, identity()));

    when(metaDataService.getPackage(any(String.class)))
        .thenAnswer(
            (Answer<Optional<Package>>)
                invocation -> {
                  String id = invocation.getArgument(0);
                  return Optional.of(packages.getOrDefault(id, null));
                });
  }

  private void setupPredictableIdGeneratorMock() {
    when(idGenerator.generateId())
        .thenAnswer(
            new Answer() {
              private int count = 0;

              @Override
              public Object answer(InvocationOnMock invocation) {
                count++;
                return "id" + count;
              }
            });
  }
}
