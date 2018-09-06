package org.molgenis.data.security.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.PackagePermission.UPDATE;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.EntityAlreadyExistsException;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.PackagePermission;
import org.molgenis.data.security.exception.NullParentPackageNotSuException;
import org.molgenis.data.security.exception.PackagePermissionDeniedException;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {PackageRepositorySecurityDecoratorTest.Config.class})
public class PackageRepositorySecurityDecoratorTest extends AbstractMolgenisSpringTest {
  private static final String USERNAME = "user";
  private static final String ROLE_SU = "SU";

  @Mock private Repository<Package> delegateRepository;
  @Mock private MutableAclService mutableAclService;
  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  private PackageRepositorySecurityDecorator repo;

  @BeforeMethod
  public void setUp() {
    repo =
        new PackageRepositorySecurityDecorator(
            delegateRepository, mutableAclService, userPermissionEvaluator);
  }

  @Test
  public void testUpdate() {
    Package pack = mock(Package.class);
    Package parent = mock(Package.class);
    Package oldPack = mock(Package.class);

    when(pack.getId()).thenReturn("1");
    when(parent.getId()).thenReturn("2");
    when(pack.getParent()).thenReturn(parent);
    when(oldPack.getParent()).thenReturn(parent);

    MutableAcl acl = mock(MutableAcl.class);
    MutableAcl parentAcl = mock(MutableAcl.class);

    doReturn(true).when(userPermissionEvaluator).hasPermission(new PackageIdentity("1"), UPDATE);

    when(mutableAclService.readAclById(any()))
        .thenAnswer(
            invocation -> {
              Object argument = invocation.getArguments()[0];
              if (argument.equals(new PackageIdentity("1"))) {
                return acl;
              } else if (argument.equals(new PackageIdentity("2"))) {
                return parentAcl;
              }
              return null;
            });

    when(delegateRepository.findOneById(pack.getId())).thenReturn(oldPack);

    repo.update(pack);

    verify(mutableAclService).updateAcl(acl);
    verify(delegateRepository).update(pack);
  }

  @Test(expectedExceptions = PackagePermissionDeniedException.class)
  public void testUpdateNoParentPermission() {
    Package pack = mock(Package.class);
    Package parent = mock(Package.class);
    Package oldPack = mock(Package.class);
    Package oldParent = mock(Package.class);

    when(pack.getId()).thenReturn("1");
    when(parent.getId()).thenReturn("2");
    when(pack.getParent()).thenReturn(parent);
    when(oldPack.getParent()).thenReturn(oldParent);

    MutableAcl acl = mock(MutableAcl.class);

    when(delegateRepository.findOneById(pack.getId())).thenReturn(oldPack);

    repo.update(pack);

    verify(mutableAclService).updateAcl(acl);
    verify(delegateRepository).update(pack);
  }

  @Test(expectedExceptions = NullParentPackageNotSuException.class)
  public void testUpdateToNullPackage() {
    Package pack = mock(Package.class);
    when(pack.getParent()).thenReturn(null);
    MutableAcl acl1 = mock(MutableAcl.class);
    Package oldPack = mock(Package.class);
    Package oldParent = mock(Package.class);
    when(oldPack.getParent()).thenReturn(oldParent);
    when(delegateRepository.findOneById(pack.getId())).thenReturn(oldPack);

    repo.update(pack);

    verify(mutableAclService).updateAcl(acl1);
    verify(delegateRepository).update(pack);
  }

  @Test
  public void testUpdate1() {
    Package package1 = mock(Package.class);
    Package package2 = mock(Package.class);
    Package parent = mock(Package.class);
    when(package1.getId()).thenReturn("1");
    when(package2.getId()).thenReturn("2");
    when(parent.getId()).thenReturn("parent");
    when(package1.getParent()).thenReturn(parent);
    when(package2.getParent()).thenReturn(parent);
    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);
    MutableAcl parentAcl = mock(MutableAcl.class);
    Package oldPack1 = mock(Package.class);
    Package oldPack2 = mock(Package.class);
    when(oldPack1.getParent()).thenReturn(parent);
    when(oldPack2.getParent()).thenReturn(parent);

    when(acl1.getParentAcl()).thenReturn(parentAcl);
    when(acl2.getParentAcl()).thenReturn(parentAcl);

    doReturn(oldPack1).when(delegateRepository).findOneById("1");
    doReturn(oldPack2).when(delegateRepository).findOneById("2");

    Stream<Package> packages = Stream.of(package1, package2);
    repo.update(packages);

    // TODO: how to verify the deleteAcl method in the "filter" of the stream

    doReturn(acl1).when(mutableAclService).readAclById(new PackageIdentity("1"));
    doReturn(acl2).when(mutableAclService).readAclById(new PackageIdentity("2"));
    doReturn(parentAcl).when(mutableAclService).readAclById(new PackageIdentity("parent"));

    doReturn(true).when(userPermissionEvaluator).hasPermission(new PackageIdentity("1"), UPDATE);
    doReturn(true).when(userPermissionEvaluator).hasPermission(new PackageIdentity("2"), UPDATE);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(captor.capture());
    assertEquals(captor.getValue().collect(toList()), asList(package1, package2));
  }

  @Test
  public void testDelete() {
    Package package1 = mock(Package.class);
    Package package2 = mock(Package.class);
    when(package1.getId()).thenReturn("1");
    when(package2.getId()).thenReturn("2");
    when(package1.getParent()).thenReturn(package2);

    doReturn(true).when(userPermissionEvaluator).hasPermission(new PackageIdentity("2"), UPDATE);

    repo.delete(package1);

    verify(mutableAclService).deleteAcl(new PackageIdentity("1"), true);
    verify(delegateRepository).delete(package1);
  }

  @Test
  public void testDelete1() {
    Package package1 = mock(Package.class);
    Package package2 = mock(Package.class);
    Package package3 = mock(Package.class);
    Package package4 = mock(Package.class);
    when(package1.getId()).thenReturn("1");
    when(package3.getId()).thenReturn("3");
    when(package4.getId()).thenReturn("4");

    when(package1.getParent()).thenReturn(package3);
    when(package2.getParent()).thenReturn(package4);

    doReturn(true).when(userPermissionEvaluator).hasPermission(new PackageIdentity("3"), UPDATE);
    doReturn(false).when(userPermissionEvaluator).hasPermission(new PackageIdentity("4"), UPDATE);

    Stream<Package> packages = Stream.of(package1, package2);
    repo.delete(packages);

    // TODO: how to verify the deleteAcl method in the "filter" of the stream

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(captor.capture());
    assertEquals(captor.getValue().collect(toList()), singletonList(package1));
  }

  @Test
  public void testDeleteById() {
    Package package1 = mock(Package.class);
    Package package2 = mock(Package.class);
    when(package2.getId()).thenReturn("2");
    when(package1.getParent()).thenReturn(package2);

    doReturn(true).when(userPermissionEvaluator).hasPermission(new PackageIdentity("2"), UPDATE);

    doReturn(package1).when(delegateRepository).findOneById("1");

    repo.deleteById("1");
    verify(mutableAclService).deleteAcl(new PackageIdentity("1"), true);
    verify(delegateRepository).deleteById("1");
  }

  @Test
  public void testDeleteAll() {
    Package permittedParentPackage =
        when(mock(Package.class).getId()).thenReturn("permittedParentPackageId").getMock();

    Package permittedPackage =
        when(mock(Package.class).getId()).thenReturn("permittedPackageId").getMock();
    when(permittedPackage.getParent()).thenReturn(permittedParentPackage);
    Package notPermittedPackage = mock(Package.class);
    doAnswer(
            invocation -> {
              Consumer<List<Package>> consumer = invocation.getArgument(0);
              consumer.accept(asList(permittedPackage, notPermittedPackage));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(any(), eq(1000));

    when(userPermissionEvaluator.hasPermission(
            new PackageIdentity(permittedParentPackage), PackagePermission.UPDATE))
        .thenReturn(true);
    repo.deleteAll();
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(entityStreamCaptor.capture());
    assertEquals(entityStreamCaptor.getValue().collect(toList()), singletonList(permittedPackage));
  }

  @Test
  public void testDeleteAll1() {
    Package package1 = mock(Package.class);
    Package package2 = mock(Package.class);
    Package package3 = mock(Package.class);
    Package package4 = mock(Package.class);
    when(package3.getId()).thenReturn("3");
    when(package4.getId()).thenReturn("4");

    when(package1.getParent()).thenReturn(package3);
    when(package2.getParent()).thenReturn(package4);

    doReturn(true).when(userPermissionEvaluator).hasPermission(new PackageIdentity("3"), UPDATE);
    doReturn(false).when(userPermissionEvaluator).hasPermission(new PackageIdentity("4"), UPDATE);

    doReturn(package1).when(delegateRepository).findOneById("1");
    doReturn(package2).when(delegateRepository).findOneById("2");

    Stream<Object> ids = Stream.of("1", "2");
    repo.deleteAll(ids);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(captor.capture());
    assertEquals(captor.getValue().collect(toList()), singletonList("1"));
  }

  @Test
  public void testAdd() {
    Package pack = mock(Package.class);
    Package parent = mock(Package.class);

    when(pack.getId()).thenReturn("1");
    when(parent.getId()).thenReturn("2");
    when(pack.getParent()).thenReturn(parent);

    MutableAcl acl = mock(MutableAcl.class);
    MutableAcl parentAcl = mock(MutableAcl.class);
    when(mutableAclService.createAcl(new PackageIdentity("1"))).thenReturn(acl);
    when(mutableAclService.readAclById(new PackageIdentity("2"))).thenReturn(parentAcl);

    when(userPermissionEvaluator.hasPermission(
            new PackageIdentity(parent.getId()), PackagePermission.ADD_PACKAGE))
        .thenReturn(true);

    repo.add(pack);

    verify(mutableAclService).createAcl(new PackageIdentity("1"));
    verify(mutableAclService).updateAcl(acl);
    verify(delegateRepository).add(pack);
  }

  @Test(expectedExceptions = PackagePermissionDeniedException.class)
  public void testAddNoPermissionOnParent() {
    Package pack = mock(Package.class);
    Package parent = mock(Package.class);

    when(parent.getId()).thenReturn("2");
    when(pack.getParent()).thenReturn(parent);

    when(userPermissionEvaluator.hasPermission(
            new PackageIdentity(parent.getId()), PackagePermission.ADD_PACKAGE))
        .thenReturn(false);

    repo.add(pack);
  }

  @Test(expectedExceptions = NullParentPackageNotSuException.class)
  public void testAddNullParent() {
    Package pack = mock(Package.class);
    when(pack.getParent()).thenReturn(null);
    repo.add(pack);
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test(
      expectedExceptions = EntityAlreadyExistsException.class,
      expectedExceptionsMessageRegExp = "type:Package id:packageId")
  public void testAddAlreadyExists() {
    Package aPackage = mock(Package.class);
    when(aPackage.getId()).thenReturn("packageId");
    when(aPackage.getIdValue()).thenReturn("packageId");
    PackageMetadata packageMetadata =
        when(mock(PackageMetadata.class).getId()).thenReturn("Package").getMock();
    when(aPackage.getEntityType()).thenReturn(packageMetadata);
    when(mutableAclService.createAcl(new PackageIdentity(aPackage)))
        .thenThrow(new AlreadyExistsException(""));
    repo.add(aPackage);
  }

  @Test
  public void testAdd1() {
    Package package1 = mock(Package.class);
    Package package2 = mock(Package.class);
    Package parent = mock(Package.class);

    when(package1.getId()).thenReturn("1");
    when(package2.getId()).thenReturn("2");
    when(parent.getId()).thenReturn("parent");
    when(package1.getParent()).thenReturn(parent);
    when(package2.getParent()).thenReturn(parent);

    when(userPermissionEvaluator.hasPermission(
            new PackageIdentity(parent.getId()), PackagePermission.ADD_PACKAGE))
        .thenReturn(true);

    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);
    MutableAcl parentAcl = mock(MutableAcl.class);
    doReturn(acl1).when(mutableAclService).createAcl(new PackageIdentity("1"));
    doReturn(acl2).when(mutableAclService).createAcl(new PackageIdentity("2"));
    when(mutableAclService.readAclById(new PackageIdentity("parent"))).thenReturn(parentAcl);

    Stream<Package> packages = Stream.of(package1, package2);
    repo.add(packages);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(captor.capture());
    assertEquals(captor.getValue().collect(toList()), asList(package1, package2));
  }

  @Test
  public void findOneByIdUserPermissionAllowed() {
    Package pack = mock(Package.class);
    when(pack.getId()).thenReturn("1");
    when(delegateRepository.findOneById("1")).thenReturn(pack);
    when(userPermissionEvaluator.hasPermission(new PackageIdentity("1"), PackagePermission.VIEW))
        .thenReturn(true);
    assertEquals(repo.findOneById("1"), pack);
  }

  @Test(expectedExceptions = PackagePermissionDeniedException.class)
  public void findOneByIdUserPermissionDenied() {
    Package pack = mock(Package.class);
    when(pack.getId()).thenReturn("1");
    when(delegateRepository.findOneById("1")).thenReturn(pack);
    when(userPermissionEvaluator.hasPermission(new PackageIdentity("1"), PackagePermission.VIEW))
        .thenReturn(false);
    repo.findOneById("1");
  }

  static class Config {}
}
