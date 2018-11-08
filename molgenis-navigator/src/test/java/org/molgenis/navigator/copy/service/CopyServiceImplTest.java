// package org.molgenis.navigator.copy.service;
//
// import static java.util.Arrays.asList;
// import static java.util.Collections.singletonList;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
//
// import org.mockito.Mock;
// import org.mockito.invocation.InvocationOnMock;
// import org.mockito.stubbing.Answer;
// import org.molgenis.data.DataService;
// import org.molgenis.data.meta.MetaDataService;
// import org.molgenis.data.meta.model.Package;
// import org.molgenis.data.meta.model.PackageMetadata;
// import org.molgenis.data.populate.IdGenerator;
// import org.molgenis.test.AbstractMockitoTest;
// import org.testng.annotations.BeforeMethod;
// import org.testng.annotations.Test;
//
// public class CopyServiceImplTest extends AbstractMockitoTest {
//
//  @Mock
//  MetaDataService metaDataService;
//  @Mock
//  PackageMetadata packageMetadata;
//  @Mock
//  IdGenerator idGenerator;
//  @Mock
//  DataService dataService;
//
//  private CopyServiceImpl copyService;
//
//  @BeforeMethod
//  public void beforeMethod() {
//    copyService = new CopyServiceImpl(metaDataService, packageMetadata, idGenerator, dataService);
//  }
//
//  @Test
//  public void testValidateNotContainsItself() {
//    Package packageA = mock(Package.class);
//    Package packageAa = mock(Package.class);
//    Package packageAb = mock(Package.class);
//    when(packageA.getChildren()).thenReturn(asList(packageAa, packageAb));
//    Package targetPackage = mock(Package.class);
//
//    copyService.validateNotContainsItself(packageA, targetPackage);
//  }
//
//  @SuppressWarnings("UnnecessaryLocalVariable")
//  @Test(expectedExceptions = IllegalArgumentException.class)
//  public void testValidateDoesContainItself() {
//    Package packageA = mock(Package.class);
//    Package packageAa = mock(Package.class);
//    Package packageAb = mock(Package.class);
//    Package packageAba = mock(Package.class);
//    when(packageA.getChildren()).thenReturn(asList(packageAa, packageAb));
//    when(packageAb.getChildren()).thenReturn(singletonList(packageAba));
//    Package targetPackage = packageAba;
//
//    copyService.validateNotContainsItself(packageA, targetPackage);
//  }
//
//  @SuppressWarnings("UnnecessaryLocalVariable")
//  @Test(expectedExceptions = IllegalArgumentException.class)
//  public void testValidateIsItself() {
//    Package packageA = mock(Package.class);
//    Package targetPackage = packageA;
//
//    copyService.validateNotContainsItself(packageA, targetPackage);
//  }
//
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
//    Package targetPackage = mock(Package.class);
//
//    copyService.copyPackage(packageA, targetPackage);
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
//
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
// }
