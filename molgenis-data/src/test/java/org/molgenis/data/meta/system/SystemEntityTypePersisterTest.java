package org.molgenis.data.meta.system;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.AttributeMetadata.REF_ENTITY_TYPE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.testng.Assert.assertEquals;

public class SystemEntityTypePersisterTest extends AbstractMockitoTest
{
	@Mock
	private DataService dataService;
	@Mock
	private SystemEntityTypeRegistry systemEntityTypeRegistry;
	@Mock
	private EntityTypeDependencyResolver entityTypeDependencyResolver;
	@Mock
	private SystemPackageRegistry systemPackageRegistry;

	private SystemEntityTypePersister systemEntityTypePersister;

	@Mock
	private AttributeMetadata attrMetaMeta;
	@Mock
	private MetaDataService metaDataService;

	@Captor
	private ArgumentCaptor<Stream<Object>> objectIdCaptor;

	public SystemEntityTypePersisterTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		RepositoryCollection defaultRepoCollection = mock(RepositoryCollection.class);
		when(metaDataService.getDefaultBackend()).thenReturn(defaultRepoCollection);
		when(dataService.getMeta()).thenReturn(metaDataService);
		systemEntityTypePersister = new SystemEntityTypePersister(dataService, systemEntityTypeRegistry,
				entityTypeDependencyResolver, systemPackageRegistry);
	}

	@Test
	public void removeNonExistingSystemEntities() throws Exception
	{
		Package systemPackage = mock(Package.class);
		when(systemPackage.getId()).thenReturn(PACKAGE_SYSTEM);

		EntityType refRemovedMeta = when(mock(EntityType.class).getId()).thenReturn("refRemoved").getMock();
		when(refRemovedMeta.getPackage()).thenReturn(systemPackage);
		when(refRemovedMeta.toString()).thenReturn("refRemoved");
		when(refRemovedMeta.getAtomicAttributes()).thenReturn(emptyList());

		EntityType removedMeta = when(mock(EntityType.class).getId()).thenReturn("removed").getMock();
		when(removedMeta.getPackage()).thenReturn(systemPackage);
		when(removedMeta.toString()).thenReturn("removed");
		Attribute refAttr = when(mock(Attribute.class).getRefEntity()).thenReturn(refRemovedMeta).getMock();
		when(removedMeta.getAtomicAttributes()).thenReturn(singletonList(refAttr));

		EntityType refEntityType = when(mock(EntityType.class).getId()).thenReturn("refEntity").getMock();
		when(refEntityType.getPackage()).thenReturn(systemPackage);
		when(refEntityType.toString()).thenReturn("refEntity");
		when(refEntityType.getAtomicAttributes()).thenReturn(emptyList());

		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getPackage()).thenReturn(systemPackage);
		when(entityType.toString()).thenReturn("entity");
		when(entityType.getAtomicAttributes()).thenReturn(emptyList());

		when(systemEntityTypeRegistry.hasSystemEntityType("removed")).thenReturn(false);
		when(systemEntityTypeRegistry.hasSystemEntityType("refRemoved")).thenReturn(false);
		when(systemEntityTypeRegistry.hasSystemEntityType("entity")).thenReturn(true);
		when(systemEntityTypeRegistry.hasSystemEntityType("refEntity")).thenReturn(true);

		when(dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(
				Stream.of(refEntityType, entityType, refRemovedMeta, removedMeta));
		systemEntityTypePersister.removeNonExistingSystemEntityTypes();
		verify(metaDataService).deleteEntityType(newArrayList(refRemovedMeta, removedMeta));
	}

	@Test
	public void persistSystemPackageChange()
	{
		Attribute attr = mock(Attribute.class);
		when(attrMetaMeta.getAttribute(REF_ENTITY_TYPE)).thenReturn(attr);
		when(attr.setDataType(any())).thenReturn(attr);
		when(dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class)).thenAnswer(invocation -> Stream.empty());

		when(dataService.findAll(eq(ENTITY_TYPE_META_DATA), objectIdCaptor.capture(), eq(EntityType.class))).thenAnswer(
				invocation -> Stream.empty());
		when(systemEntityTypeRegistry.getSystemEntityTypes()).thenAnswer(invocation -> Stream.empty());

		String packageId0 = "packageId0";
		String packageName0 = "packageName0";
		SystemPackage package0 = when(mock(SystemPackage.class).getId()).thenReturn(packageName0).getMock();
		when(package0.getId()).thenReturn(packageId0);
		String packageId1 = "packageId1";
		String packageName1 = "packageName1";
		SystemPackage package1 = when(mock(SystemPackage.class).getId()).thenReturn(packageName1).getMock();
		when(package1.getId()).thenReturn(packageId1);
		when(systemPackageRegistry.getSystemPackages()).thenReturn(Stream.of(package0, package1));
		when(dataService.findAll(PACKAGE, Package.class)).thenAnswer(invocation -> Stream.of(package0));
		systemEntityTypePersister.persist();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Package>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(metaDataService).upsertPackages(captor.capture());
		assertEquals(captor.getValue().collect(toList()), newArrayList(package0, package1));
	}

	// regression test for https://github.com/molgenis/molgenis/issues/5168
	@SuppressWarnings("unchecked")
	@Test
	public void persistSystemPackageNoChange()
	{
		Attribute attr = mock(Attribute.class);
		when(attrMetaMeta.getAttribute(REF_ENTITY_TYPE)).thenReturn(attr);
		when(attr.setDataType(any())).thenReturn(attr);
		when(dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class)).thenAnswer(invocation -> Stream.empty());

		when(dataService.findAll(eq(ENTITY_TYPE_META_DATA), objectIdCaptor.capture(), eq(EntityType.class))).thenAnswer(
				invocation -> Stream.empty());
		when(systemEntityTypeRegistry.getSystemEntityTypes()).thenAnswer(invocation -> Stream.empty());

		String packageId0 = "packageId0";
		String packageName0 = "packageName0";
		SystemPackage package0 = when(mock(SystemPackage.class).getId()).thenReturn(packageName0).getMock();
		when(package0.getId()).thenReturn(packageId0);
		String packageId1 = "packageId1";
		String packageName1 = "packageName1";
		SystemPackage package1 = when(mock(SystemPackage.class).getId()).thenReturn(packageName1).getMock();
		when(package1.getId()).thenReturn(packageId1);
		when(systemPackageRegistry.getSystemPackages()).thenReturn(Stream.of(package0, package1));
		when(dataService.findAll(PACKAGE, Package.class)).thenAnswer(invocation -> Stream.of(package0, package1));
		systemEntityTypePersister.persist();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Package>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(metaDataService).upsertPackages(captor.capture());
		assertEquals(captor.getValue().collect(toList()), newArrayList(package0, package1));
	}

	private static class EmptyStreamAnswer implements Answer<Stream<EntityType>>
	{
		@Override
		public Stream<EntityType> answer(InvocationOnMock invocation) throws Throwable
		{
			return Stream.empty();
		}
	}
}