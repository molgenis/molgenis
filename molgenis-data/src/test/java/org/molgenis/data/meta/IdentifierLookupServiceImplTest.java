package org.molgenis.data.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class IdentifierLookupServiceImplTest
{
	private DataService dataService;
	private IdentifierLookupServiceImpl identifierLookupService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		identifierLookupService = new IdentifierLookupServiceImpl(dataService);
	}

	@DataProvider(name = "getEntityTypeIdEntityTypeWithPackageProvider")
	public static Iterator<Object[]> getEntityTypeIdEntityTypeWithPackageProvider()
	{
		List<Object[]> dataList = new ArrayList<>(3);
		dataList.add(new Object[] { "a_b_c_myEntityType", "idC" });
		dataList.add(new Object[] { "a_b_myEntityType", "idB" });
		dataList.add(new Object[] { "a_myEntityType", "idA" });
		return dataList.iterator();
	}

	@Test(dataProvider = "getEntityTypeIdEntityTypeWithPackageProvider")
	public void testGetEntityTypeIdEntityTypeWithPackage(String fullyQualifiedEntityName, String expectedEntityTypeId)
	{
		Package packageA = mock(Package.class);
		when(packageA.getName()).thenReturn("a");
		when(packageA.getParent()).thenReturn(null);

		Package packageB = mock(Package.class);
		when(packageB.getName()).thenReturn("b");
		when(packageB.getParent()).thenReturn(packageA);

		Package packageC = mock(Package.class);
		when(packageC.getName()).thenReturn("c");
		when(packageC.getParent()).thenReturn(packageB);

		String entityTypeName = "myEntityType";

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("id");
		when(entityType.getName()).thenReturn(entityTypeName);
		EntityType entityTypeA = mock(EntityType.class);
		when(entityTypeA.getId()).thenReturn("idA");
		when(entityTypeA.getName()).thenReturn(entityTypeName);
		when(entityTypeA.getPackage()).thenReturn(packageA);
		EntityType entityTypeB = mock(EntityType.class);
		when(entityTypeB.getId()).thenReturn("idB");
		when(entityTypeB.getName()).thenReturn(entityTypeName);
		when(entityTypeB.getPackage()).thenReturn(packageB);
		EntityType entityTypeC = mock(EntityType.class);
		when(entityTypeC.getId()).thenReturn("idC");
		when(entityTypeC.getName()).thenReturn(entityTypeName);
		when(entityTypeC.getPackage()).thenReturn(packageC);

		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		when(q.eq(EntityTypeMetadata.SIMPLE_NAME, entityTypeName)).thenReturn(q);
		when(q.fetch(any(Fetch.class))).thenReturn(q);
		when(q.findAll()).then(invocation -> Stream.of(entityType, entityTypeA, entityTypeB, entityTypeC));
		when(dataService.query(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(q);

		assertEquals(identifierLookupService.getEntityTypeId(fullyQualifiedEntityName), expectedEntityTypeId);
	}

	@Test
	public void testGetEntityTypeIdEntityTypeWithoutPackage()
	{
		String entityTypeName = "myEntityType";
		String entityTypeId = "id";

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		when(entityType.getName()).thenReturn(entityTypeName);

		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		when(q.eq(EntityTypeMetadata.SIMPLE_NAME, entityTypeName)).thenReturn(q);
		when(q.and()).thenReturn(q);
		when(q.eq(EntityTypeMetadata.PACKAGE, null)).thenReturn(q);
		when(q.fetch(any(Fetch.class))).thenReturn(q);
		when(q.findOne()).thenReturn(entityType);
		when(dataService.query(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(q);

		assertEquals(identifierLookupService.getEntityTypeId(entityTypeName), entityTypeId);
	}

	@Test
	public void testGetEntityTypeIdEntityTypeWithoutPackageUnknownEntityType()
	{
		String entityTypeName = "myUnknownEntityType";

		@SuppressWarnings("unchecked")
		Query<EntityType> q = mock(Query.class);
		when(q.eq(EntityTypeMetadata.SIMPLE_NAME, entityTypeName)).thenReturn(q);
		when(q.and()).thenReturn(q);
		when(q.eq(EntityTypeMetadata.PACKAGE, null)).thenReturn(q);
		when(q.fetch(any(Fetch.class))).thenReturn(q);
		when(q.findOne()).thenReturn(null);
		when(dataService.query(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(q);

		assertNull(identifierLookupService.getEntityTypeId(entityTypeName));
	}

	@DataProvider(name = "getPackageIdParentPackageProvider")
	public static Iterator<Object[]> getPackageIdParentPackageProvider()
	{
		List<Object[]> dataList = new ArrayList<>(2);
		dataList.add(new Object[] { "myPackage_myPackage_myPackage", "idC" });
		dataList.add(new Object[] { "myPackage_myPackage", "idB" });
		return dataList.iterator();
	}

	@Test(dataProvider = "getPackageIdParentPackageProvider")
	public void testGetPackageIdParentPackage(String fullyQualifiedPackageName, String expectedPackageId)
	{
		String myPackageName = "myPackage";

		Package packageA = mock(Package.class);
		when(packageA.getId()).thenReturn("idA");
		when(packageA.getName()).thenReturn(myPackageName);
		when(packageA.getParent()).thenReturn(null);

		Package packageB = mock(Package.class);
		when(packageB.getId()).thenReturn("idB");
		when(packageB.getName()).thenReturn(myPackageName);
		when(packageB.getParent()).thenReturn(packageA);

		Package packageC = mock(Package.class);
		when(packageC.getId()).thenReturn("idC");
		when(packageC.getName()).thenReturn(myPackageName);
		when(packageC.getParent()).thenReturn(packageB);

		@SuppressWarnings("unchecked")
		Query<Package> q = mock(Query.class);
		when(q.eq(PackageMetadata.SIMPLE_NAME, myPackageName)).thenReturn(q);
		when(q.fetch(any(Fetch.class))).thenReturn(q);
		when(q.findAll()).then(invocation -> Stream.of(packageA, packageB, packageC));
		when(dataService.query(PACKAGE, Package.class)).thenReturn(q);

		assertEquals(identifierLookupService.getPackageId(fullyQualifiedPackageName), expectedPackageId);
	}

	@Test
	public void testGetPackageIdNoParentPackage()
	{
		String myPackageName = "myPackage";
		String myPackageId = "idA";

		Package packageA = mock(Package.class);
		when(packageA.getId()).thenReturn(myPackageId);
		when(packageA.getName()).thenReturn(myPackageName);
		when(packageA.getParent()).thenReturn(null);

		@SuppressWarnings("unchecked")
		Query<Package> q = mock(Query.class);
		when(q.eq(PackageMetadata.SIMPLE_NAME, myPackageName)).thenReturn(q);
		when(q.and()).thenReturn(q);
		when(q.eq(PackageMetadata.PARENT, null)).thenReturn(q);
		when(q.fetch(any(Fetch.class))).thenReturn(q);
		when(q.findOne()).thenReturn(packageA);
		when(dataService.query(PACKAGE, Package.class)).thenReturn(q);

		assertEquals(identifierLookupService.getPackageId(myPackageName), myPackageId);
	}

	@Test
	public void testGetPackageIdNoParentPackageUnknownPackage()
	{
		String packageName = "myUnknownPackage";

		@SuppressWarnings("unchecked")
		Query<Package> q = mock(Query.class);
		when(q.eq(PackageMetadata.SIMPLE_NAME, packageName)).thenReturn(q);
		when(q.and()).thenReturn(q);
		when(q.eq(PackageMetadata.PARENT, null)).thenReturn(q);
		when(q.fetch(any(Fetch.class))).thenReturn(q);
		when(q.findOne()).thenReturn(null);
		when(dataService.query(PACKAGE, Package.class)).thenReturn(q);

		assertNull(identifierLookupService.getPackageId(packageName));
	}
}