package org.molgenis.data.meta.persist;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.test.AbstractMockitoTest;

class PackagePersisterTest extends AbstractMockitoTest {
  @Mock private DataService dataService;

  private PackagePersister packagePersister;

  @BeforeEach
  void setUpBeforeMethod() {
    packagePersister = new PackagePersister(dataService);
  }

  @Test
  void testPackagePersister() {
    assertThrows(NullPointerException.class, () -> new PackagePersister(null));
  }

  @Test
  void testUpsertPackages() throws Exception {
    String newPackageName = "newPackage";
    String unchangedPackageName = "unchangedPackage";
    String updatedPackageName = "updatedPackage";

    Package newPackage = mock(Package.class);
    when(newPackage.getId()).thenReturn("newPackageId");

    Package unchangedPackage = mock(Package.class);
    when(unchangedPackage.getId()).thenReturn("unchangedPackageId");

    Package updatedPackage = mock(Package.class);
    when(updatedPackage.getId()).thenReturn("updatedPackageId");

    Package existingUpdatedPackage = mock(Package.class);
    when(existingUpdatedPackage.getId()).thenReturn("updatedPackageId");

    @SuppressWarnings("unchecked")
    Query<Package> query = mock(Query.class);
    when(dataService.query(PACKAGE, Package.class)).thenReturn(query);
    when(query.in(eq(PackageMetadata.ID), any(Set.class))).thenReturn(query);
    when(query.findAll()).thenReturn(Stream.of(unchangedPackage, existingUpdatedPackage));

    packagePersister.upsertPackages(Stream.of(newPackage, unchangedPackage, updatedPackage));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> addCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).add(eq(PACKAGE), addCaptor.capture());
    assertEquals(singletonList(newPackage), addCaptor.getValue().collect(toList()));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> updateCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).update(eq(PACKAGE), updateCaptor.capture());
    assertEquals(singletonList(updatedPackage), updateCaptor.getValue().collect(toList()));
  }
}
