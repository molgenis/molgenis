package org.molgenis.data.meta.persist;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Package;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.meta.model.PackageMetadata.NAME;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.testng.Assert.assertEquals;

public class PackagePersisterTest
{
	@Mock
	private DataService dataService;

	private PackagePersister packagePersister;

	@BeforeClass
	public void setUpBeforeClass()
	{
		initMocks(this);
		packagePersister = new PackagePersister(dataService);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		reset(dataService);
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
		String newPackageFqn = "org_newPackage";
		String unchangedPackageName = "unchangedPackage";
		String unchangedPackageFqn = "org_unchangedPackage";
		String updatedPackageName = "updatedPackage";
		String updatedPackageFqn = "org_updatedPackage";

		Package newPackage = mock(Package.class);
		when(newPackage.getId()).thenReturn("newPackageId");
		when(newPackage.getName()).thenReturn(newPackageName);
		when(newPackage.getFullyQualifiedName()).thenReturn(newPackageFqn);

		Package unchangedPackage = mock(Package.class);
		when(unchangedPackage.getId()).thenReturn("unchangedPackageId");
		when(unchangedPackage.getName()).thenReturn(unchangedPackageName);
		when(unchangedPackage.getFullyQualifiedName()).thenReturn(unchangedPackageFqn);

		Package updatedPackage = mock(Package.class);
		when(updatedPackage.getId()).thenReturn("upadtedPackageId");
		when(updatedPackage.getName()).thenReturn(updatedPackageName);
		when(updatedPackage.getFullyQualifiedName()).thenReturn(updatedPackageFqn);

		Package existingUpdatedPackage = mock(Package.class);
		when(existingUpdatedPackage.getId()).thenReturn("existingUpdatedPackageId");
		when(existingUpdatedPackage.getName()).thenReturn(updatedPackageName);
		when(existingUpdatedPackage.getFullyQualifiedName()).thenReturn(updatedPackageFqn);

		Package sameNameDifferentFqnPackage = mock(Package.class);
		when(sameNameDifferentFqnPackage.getId()).thenReturn("sameNameDifferentFqnPackageId");
		when(sameNameDifferentFqnPackage.getName()).thenReturn(updatedPackageName);
		when(sameNameDifferentFqnPackage.getFullyQualifiedName()).thenReturn("usr_" + updatedPackageName);

		Query<Package> query = mock(Query.class);
		when(dataService.query(PACKAGE, Package.class)).thenReturn(query);
		when(query.in(NAME, newHashSet(newPackageName, unchangedPackageName, updatedPackageName))).thenReturn(query);
		when(query.findAll())
				.thenReturn(Stream.of(unchangedPackage, existingUpdatedPackage, sameNameDifferentFqnPackage));

		packagePersister.upsertPackages(Stream.of(newPackage, unchangedPackage, updatedPackage));

		ArgumentCaptor<Stream<Package>> addCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(PACKAGE), addCaptor.capture());
		assertEquals(addCaptor.getValue().collect(toList()), singletonList(newPackage));

		ArgumentCaptor<Stream<Package>> updateCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).update(eq(PACKAGE), updateCaptor.capture());
		assertEquals(updateCaptor.getValue().collect(toList()), singletonList(updatedPackage));
	}
}