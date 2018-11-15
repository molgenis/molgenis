package org.molgenis.navigator.copy.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.copy.exception.RecursiveCopyException;
import org.molgenis.navigator.util.ResourceCollection;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.MessageSource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResourceCopierTest extends AbstractMockitoTest {

  @Mock IdGenerator idGenerator;
  @Mock DataService dataService;
  @Mock EntityTypeDependencyResolver entityTypeDependencyResolver;
  @Mock AttributeFactory attributeFactory;
  @Mock MessageSource messageSource;

  @BeforeMethod
  public void beforeMethod() {
    MessageSourceHolder.setMessageSource(messageSource);
  }

  @Test(expectedExceptions = RecursiveCopyException.class)
  public void testValidateNotContainsItself() {
    Package packageA = mock(Package.class);
    Package packageAa = mock(Package.class);
    Package packageAb = mock(Package.class);
    when(packageA.getChildren()).thenReturn(asList(packageAa, packageAb));

    createCopier(singletonList(packageA), emptyList(), packageAb, mock(Progress.class)).copy();
  }

  //  @Test
  //  public void testCopyPackage() {
  //    setupPredictableIdGeneratorMock();
  //    Package packageA = mock(Package.class);
  //    Package packageAa = mock(Package.class);
  //    Package packageAb = mock(Package.class);
  //    Package packageAba = mock(Package.class);
  //    Package packageC = mock(Package.class);
  //    when(packageA.getChildren()).thenReturn(asList(packageAa, packageAb, packageC));
  //    when(packageAb.getChildren()).thenReturn(singletonList(packageAba));
  //    when(packageA.getLabel()).thenReturn("test");
  //    Package targetPackage = mock(Package.class);
  //
  //    createCopier(singletonList(packageA), emptyList(), targetPackage,
  // mock(Progress.class)).copy();
  //
  //    verify(packageA).setParent(targetPackage);
  //    verify(packageA).setId("id1");
  //    verify(packageAa).setId("id2");
  //    verify(packageAb).setId("id3");
  //    verify(packageAba).setId("id4");
  //    verify(packageC).setId("id5");
  //  }
  //
  //  @Test
  //  public void testUpdatePackageLabel() {
  //    Package pack = mock(Package.class);
  //    Package targetPack = mock(Package.class);
  //    Package childPack1 = mock(Package.class);
  //    Package childPack2 = mock(Package.class);
  //    Package childPack3 = mock(Package.class);
  //    when(childPack1.getLabel()).thenReturn("Label1");
  //    when(childPack2.getLabel()).thenReturn("Label2");
  //    when(childPack3.getLabel()).thenReturn("Label3");
  //    when(targetPack.getChildren()).thenReturn(asList(childPack1, childPack2, childPack3));
  //    when(pack.getLabel()).thenReturn("Label2");
  //
  //    copyService.copyPackage(pack, targetPack);
  //
  //    verify(pack).setLabel("Label2 (Copy)");
  //  }
  //
  //  @Test
  //  public void testUpdatePackageLabelSecondCopy() {
  //    Package pack = mock(Package.class);
  //    Package targetPack = mock(Package.class);
  //    Package childPack = mock(Package.class);
  //    Package childPackCopy = mock(Package.class);
  //    when(childPack.getLabel()).thenReturn("Label2");
  //    when(childPackCopy.getLabel()).thenReturn("Label2 (Copy)");
  //    when(targetPack.getChildren()).thenReturn(asList(childPack, childPackCopy));
  //    when(pack.getLabel()).thenReturn("Label2");
  //
  //    copyService.copyPackage(pack, targetPack);
  //
  //    verify(pack).setLabel("Label2 (Copy) (Copy)");
  //  }
  //
  //  @Test
  //  public void testNotUpdatePackageLabel() {
  //    Package pack = mock(Package.class);
  //    Package targetPack = mock(Package.class);
  //    Package childPack1 = mock(Package.class);
  //    when(childPack1.getLabel()).thenReturn("Label1");
  //    when(targetPack.getChildren()).thenReturn(singletonList(childPack1));
  //    when(pack.getLabel()).thenReturn("Label2");
  //
  //    copyService.copyPackage(pack, targetPack);
  //
  //    verify(pack, never()).setLabel(any(String.class));
  //  }

  //  private void setupPredictableIdGeneratorMock() {
  //    when(idGenerator.generateId())
  //        .thenAnswer(
  //            new Answer() {
  //              private int count = 0;
  //
  //              @Override
  //              public Object answer(InvocationOnMock invocation) {
  //                count++;
  //                return "id" + count;
  //              }
  //            });
  //  }

  private ResourceCopier createCopier(
      List<Package> packages,
      List<EntityType> entityTypes,
      Package targetPackage,
      Progress progress) {
    ResourceCollection resourceCollection = mock(ResourceCollection.class);
    when(resourceCollection.getEntityTypes()).thenReturn(entityTypes);
    when(resourceCollection.getPackages()).thenReturn(packages);
    return new ResourceCopier(
        resourceCollection,
        targetPackage,
        progress,
        dataService,
        idGenerator,
        entityTypeDependencyResolver,
        attributeFactory);
  }
}
