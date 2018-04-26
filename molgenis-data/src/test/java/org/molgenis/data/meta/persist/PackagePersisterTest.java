package org.molgenis.data.meta.persist;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.testng.Assert.assertEquals;

public class PackagePersisterTest extends AbstractMockitoTest
{
	@Mock
	private DataService dataService;

	private PackagePersister packagePersister;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		packagePersister = new PackagePersister(dataService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testPackagePersister()
	{
		new PackagePersister(null);
	}

	@Test
	public void testUpsertPackages() throws Exception
	{
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
		assertEquals(addCaptor.getValue().collect(toList()), singletonList(newPackage));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Package>> updateCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(dataService).update(eq(PACKAGE), updateCaptor.capture());
		assertEquals(updateCaptor.getValue().collect(toList()), singletonList(updatedPackage));
	}
}