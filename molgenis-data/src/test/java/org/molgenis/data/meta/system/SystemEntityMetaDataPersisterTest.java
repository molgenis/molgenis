package org.molgenis.data.meta.system;

import com.google.common.collect.Maps;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.testng.Assert.assertEquals;

public class SystemEntityMetaDataPersisterTest
{
	private DataService dataService;
	private SystemEntityMetaDataRegistry systemEntityMetaRegistry;
	private SystemEntityMetaDataPersister systemEntityMetaDataPersister;
	private AttributeMetaDataMetaData attrMetaMeta;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		attrMetaMeta = mock(AttributeMetaDataMetaData.class);
		MetaDataService metaDataService = mock(MetaDataService.class);
		RepositoryCollection defaultRepoCollection = mock(RepositoryCollection.class);
		when(metaDataService.getDefaultBackend()).thenReturn(defaultRepoCollection);
		dataService = mock(DataService.class);
		when(dataService.getMeta()).thenReturn(metaDataService);
		systemEntityMetaRegistry = mock(SystemEntityMetaDataRegistry.class);
		systemEntityMetaDataPersister = new SystemEntityMetaDataPersister(dataService, systemEntityMetaRegistry);
		systemEntityMetaDataPersister.setAttributeMetaDataMetaData(attrMetaMeta);
	}

	@Test
	public void removeNonExistingSystemEntities() throws Exception
	{
		Package systemPackage = mock(Package.class);
		when(systemPackage.getName()).thenReturn(PACKAGE_SYSTEM);

		EntityMetaData refRemovedMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refRemoved").getMock();
		when(refRemovedMeta.getPackage()).thenReturn(systemPackage);
		when(refRemovedMeta.toString()).thenReturn("refRemoved");
		when(refRemovedMeta.getAtomicAttributes()).thenReturn(emptyList());

		EntityMetaData removedMeta = when(mock(EntityMetaData.class).getName()).thenReturn("removed").getMock();
		when(removedMeta.getPackage()).thenReturn(systemPackage);
		when(removedMeta.toString()).thenReturn("removed");
		Attribute refAttr = when(mock(Attribute.class).getRefEntity()).thenReturn(refRemovedMeta)
				.getMock();
		when(removedMeta.getAtomicAttributes()).thenReturn(singletonList(refAttr));

		EntityMetaData refEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity").getMock();
		when(refEntityMeta.getPackage()).thenReturn(systemPackage);
		when(refEntityMeta.toString()).thenReturn("refEntity");
		when(refEntityMeta.getAtomicAttributes()).thenReturn(emptyList());

		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getPackage()).thenReturn(systemPackage);
		when(entityMeta.toString()).thenReturn("entity");
		when(entityMeta.getAtomicAttributes()).thenReturn(emptyList());

		when(systemEntityMetaRegistry.hasSystemEntityMetaData("removed")).thenReturn(false);
		when(systemEntityMetaRegistry.hasSystemEntityMetaData("refRemoved")).thenReturn(false);
		when(systemEntityMetaRegistry.hasSystemEntityMetaData("entity")).thenReturn(true);
		when(systemEntityMetaRegistry.hasSystemEntityMetaData("refEntity")).thenReturn(true);

		when(dataService.findAll(ENTITY_META_DATA, EntityMetaData.class))
				.thenReturn(Stream.of(refEntityMeta, entityMeta, refRemovedMeta, removedMeta));
		systemEntityMetaDataPersister.removeNonExistingSystemEntities();
		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(ENTITY_META_DATA), captor.capture());
		assertEquals(captor.getValue().collect(toList()), Arrays.asList(removedMeta, refRemovedMeta));
	}

	@Test
	public void persistSystemPackageChange()
	{
		Attribute attr = mock(Attribute.class);
		when(attrMetaMeta.getAttribute(REF_ENTITY)).thenReturn(attr);
		when(attr.setDataType(any())).thenReturn(attr);
		when(dataService.findAll(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(Stream.empty());
		when(systemEntityMetaRegistry.getSystemEntityMetaDatas()).thenReturn(Stream.empty());

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
		systemEntityMetaDataPersister.persist(event);
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
		when(attrMetaMeta.getAttribute(REF_ENTITY)).thenReturn(attr);
		when(attr.setDataType(any())).thenReturn(attr);
		when(dataService.findAll(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(Stream.empty());
		when(systemEntityMetaRegistry.getSystemEntityMetaDatas()).thenReturn(Stream.empty());

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
		systemEntityMetaDataPersister.persist(event);
		//noinspection unchecked
		verify(dataService, times(0)).add(eq(PACKAGE), any(Stream.class));
	}
}