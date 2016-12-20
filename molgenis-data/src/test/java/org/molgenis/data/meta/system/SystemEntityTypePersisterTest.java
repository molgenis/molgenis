package org.molgenis.data.meta.system;

import com.google.common.collect.Maps;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.UuidGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
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

public class SystemEntityTypePersisterTest
{
	private DataService dataService;

	private SystemEntityTypeRegistry systemEntityTypeRegistry;
	private SystemEntityTypePersister systemEntityTypePersister;
	private AttributeMetadata attrMetaMeta;
	private MetaDataService metaDataService;

	@Captor
	ArgumentCaptor<Stream<Object>> objectIdCaptor;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		attrMetaMeta = mock(AttributeMetadata.class);
		metaDataService = mock(MetaDataService.class);
		RepositoryCollection defaultRepoCollection = mock(RepositoryCollection.class);
		when(metaDataService.getDefaultBackend()).thenReturn(defaultRepoCollection);
		dataService = mock(DataService.class);
		when(dataService.getMeta()).thenReturn(metaDataService);
		systemEntityTypeRegistry = mock(SystemEntityTypeRegistry.class);
		EntityTypeDependencyResolver entityTypeDependencyResolver = mock(EntityTypeDependencyResolver.class);
		systemEntityTypePersister = new SystemEntityTypePersister(dataService, systemEntityTypeRegistry,
				entityTypeDependencyResolver, mock(UuidGenerator.class));
	}

	@Test
	public void removeNonExistingSystemEntities() throws Exception
	{
		Package systemPackage = mock(Package.class);
		when(systemPackage.getName()).thenReturn(PACKAGE_SYSTEM);

		EntityType refRemovedMeta = when(mock(EntityType.class).getName()).thenReturn("refRemoved").getMock();
		when(refRemovedMeta.getPackage()).thenReturn(systemPackage);
		when(refRemovedMeta.toString()).thenReturn("refRemoved");
		when(refRemovedMeta.getAtomicAttributes()).thenReturn(emptyList());

		EntityType removedMeta = when(mock(EntityType.class).getName()).thenReturn("removed").getMock();
		when(removedMeta.getPackage()).thenReturn(systemPackage);
		when(removedMeta.toString()).thenReturn("removed");
		Attribute refAttr = when(mock(Attribute.class).getRefEntity()).thenReturn(refRemovedMeta).getMock();
		when(removedMeta.getAtomicAttributes()).thenReturn(singletonList(refAttr));

		EntityType refEntityType = when(mock(EntityType.class).getName()).thenReturn("refEntity").getMock();
		when(refEntityType.getPackage()).thenReturn(systemPackage);
		when(refEntityType.toString()).thenReturn("refEntity");
		when(refEntityType.getAtomicAttributes()).thenReturn(emptyList());

		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getPackage()).thenReturn(systemPackage);
		when(entityType.toString()).thenReturn("entity");
		when(entityType.getAtomicAttributes()).thenReturn(emptyList());

		when(systemEntityTypeRegistry.hasSystemEntityType("removed")).thenReturn(false);
		when(systemEntityTypeRegistry.hasSystemEntityType("refRemoved")).thenReturn(false);
		when(systemEntityTypeRegistry.hasSystemEntityType("entity")).thenReturn(true);
		when(systemEntityTypeRegistry.hasSystemEntityType("refEntity")).thenReturn(true);

		when(dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class))
				.thenReturn(Stream.of(refEntityType, entityType, refRemovedMeta, removedMeta));
		systemEntityTypePersister.removeNonExistingSystemEntities();
		verify(metaDataService).deleteEntityType(newArrayList(refRemovedMeta, removedMeta));
	}

	@Test
	public void persistSystemPackageChange()
	{
		Attribute attr = mock(Attribute.class);
		when(attrMetaMeta.getAttribute(REF_ENTITY_TYPE)).thenReturn(attr);
		when(attr.setDataType(any())).thenReturn(attr);
		when(dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(Stream.empty());

		when(dataService.findAll(eq(ENTITY_TYPE_META_DATA), objectIdCaptor.capture(), eq(EntityType.class)))
				.thenReturn(Stream.empty());
		when(systemEntityTypeRegistry.getSystemEntityTypes()).thenAnswer(new EmptyStreamAnswer());

		ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);
		ApplicationContext applicationContext = mock(ApplicationContext.class);
		String packageName0 = "packageName0";
		Package package0 = when(mock(Package.class).getIdValue()).thenReturn(packageName0).getMock();
		String packageName1 = "packageName1";
		Package package1 = when(mock(Package.class).getIdValue()).thenReturn(packageName1).getMock();
		Map<String, Package> packageMap = Maps.newHashMap();
		packageMap.put(packageName0, package0);
		packageMap.put(packageName1, package1);
		when(applicationContext.getBeansOfType(Package.class)).thenReturn(packageMap);
		when(event.getApplicationContext()).thenReturn(applicationContext);
		when(dataService.findOneById(PACKAGE, packageName0, Package.class)).thenReturn(package0);
		when(dataService.findOneById(PACKAGE, packageName1, Package.class)).thenReturn(null);
		systemEntityTypePersister.persist(event);
		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(PACKAGE), captor.capture());
		assertEquals(captor.getValue().collect(toList()), newArrayList(package1));
	}

	// regression test for https://github.com/molgenis/molgenis/issues/5168
	@Test
	public void persistSystemPackageNoChange()
	{
		Attribute attr = mock(Attribute.class);
		when(attrMetaMeta.getAttribute(REF_ENTITY_TYPE)).thenReturn(attr);
		when(attr.setDataType(any())).thenReturn(attr);
		when(dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(Stream.empty());
		when(dataService.findAll(eq(ENTITY_TYPE_META_DATA), objectIdCaptor.capture(), eq(EntityType.class)))
				.thenReturn(Stream.empty());
		when(systemEntityTypeRegistry.getSystemEntityTypes()).thenAnswer(new EmptyStreamAnswer());

		ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);
		ApplicationContext applicationContext = mock(ApplicationContext.class);
		String packageName0 = "packageName0";
		Package package0 = when(mock(Package.class).getIdValue()).thenReturn(packageName0).getMock();
		String packageName1 = "packageName1";
		Package package1 = when(mock(Package.class).getIdValue()).thenReturn(packageName1).getMock();
		Map<String, Package> packageMap = Maps.newHashMap();
		packageMap.put(packageName0, package0);
		packageMap.put(packageName1, package1);
		when(applicationContext.getBeansOfType(Package.class)).thenReturn(packageMap);
		when(event.getApplicationContext()).thenReturn(applicationContext);
		when(dataService.findOneById(PACKAGE, packageName0, Package.class)).thenReturn(package0);
		when(dataService.findOneById(PACKAGE, packageName1, Package.class)).thenReturn(package1);
		systemEntityTypePersister.persist(event);
		//noinspection unchecked
		verify(dataService, times(0)).add(eq(PACKAGE), any(Stream.class));
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