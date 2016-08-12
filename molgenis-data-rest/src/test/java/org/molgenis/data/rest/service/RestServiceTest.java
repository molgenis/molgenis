package org.molgenis.data.rest.service;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMetaFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class RestServiceTest
{
	private RestService restService;
	private EntityManager entityManager;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		DataService dataService = mock(DataService.class);
		IdGenerator idGenerator = mock(IdGenerator.class);
		FileStore fileStore = mock(FileStore.class);
		FileMetaFactory fileMetaFactory = mock(FileMetaFactory.class);
		entityManager = mock(EntityManager.class);
		this.restService = new RestService(dataService, idGenerator, fileStore, fileMetaFactory, entityManager);
	}

	@Test
	public void toEntityValue()
	{
		AttributeMetaData attr = mock(AttributeMetaData.class);
		when(attr.getDataType()).thenReturn(MREF);
		assertEquals(restService.toEntityValue(attr, null), emptyList());
	}

	// https://github.com/molgenis/molgenis/issues/4725
	@Test
	public void toEntityValueMrefToIntAttr()
	{
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		String refEntityName = "refEntity";
		AttributeMetaData refIdAttr = mock(AttributeMetaData.class);
		when(refIdAttr.getDataType()).thenReturn(INT);
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		AttributeMetaData attr = mock(AttributeMetaData.class);
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(entityManager.getReference(refEntityMeta, 0)).thenReturn(entity0);
		when(entityManager.getReference(refEntityMeta, 1)).thenReturn(entity1);
		Object entityValue = restService.toEntityValue(attr, "0,1"); // string
		assertEquals(entityValue, Arrays.asList(entity0, entity1));
	}

	@Test
	public void toEntityValueMrefToStringAttr()
	{
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		String refEntityName = "refEntity";
		AttributeMetaData refIdAttr = mock(AttributeMetaData.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		AttributeMetaData attr = mock(AttributeMetaData.class);
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(entityManager.getReference(refEntityMeta, "0")).thenReturn(entity0);
		when(entityManager.getReference(refEntityMeta, "1")).thenReturn(entity1);
		Object entityValue = restService.toEntityValue(attr, "0,1"); // string
		assertEquals(entityValue, Arrays.asList(entity0, entity1));
	}
}
