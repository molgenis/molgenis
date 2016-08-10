package org.molgenis.data.meta.system;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.testng.Assert.assertEquals;

public class SystemEntityMetaDataPersisterTest
{
	private DataService dataService;
	private SystemEntityMetaDataRegistry systemEntityMetaRegistry;
	private SystemEntityMetaDataPersister systemEntityMetaDataPersister;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		systemEntityMetaRegistry = mock(SystemEntityMetaDataRegistry.class);
		systemEntityMetaDataPersister = new SystemEntityMetaDataPersister(dataService, systemEntityMetaRegistry);
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
		AttributeMetaData refAttr = when(mock(AttributeMetaData.class).getRefEntity()).thenReturn(refRemovedMeta)
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
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(ENTITY_META_DATA), captor.capture());
		assertEquals(captor.getValue().collect(toList()), Arrays.asList(removedMeta, refRemovedMeta));
	}
}