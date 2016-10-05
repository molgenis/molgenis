package org.molgenis.data.rest.service;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMetaFactory;
import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;

import static com.google.common.collect.Lists.newArrayList;
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
	@DataProvider(name = "toEntityValueMrefProvider")
	public static Iterator<Object[]> toEntityValueMrefProvider()
	{
		return newArrayList(new Object[] { MREF }, new Object[] { ONE_TO_MANY }).iterator();
	}

	@Test(dataProvider = "toEntityValueMrefProvider")
	public void toEntityValueMrefToIntAttr(AttributeType attrType)
	{
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		String refEntityName = "refEntity";
		AttributeMetaData refIdAttr = mock(AttributeMetaData.class);
		when(refIdAttr.getDataType()).thenReturn(INT);
		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getName()).thenReturn(refEntityName);
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		AttributeMetaData attr = mock(AttributeMetaData.class);
		when(attr.getDataType()).thenReturn(attrType);
		when(attr.getRefEntity()).thenReturn(refEntityType);
		when(entityManager.getReference(refEntityType, 0)).thenReturn(entity0);
		when(entityManager.getReference(refEntityType, 1)).thenReturn(entity1);
		Object entityValue = restService.toEntityValue(attr, "0,1"); // string
		assertEquals(entityValue, Arrays.asList(entity0, entity1));
	}

	@Test(dataProvider = "toEntityValueMrefProvider")
	public void toEntityValueMrefToStringAttr(AttributeType attrType)
	{
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		String refEntityName = "refEntity";
		AttributeMetaData refIdAttr = mock(AttributeMetaData.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);
		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getName()).thenReturn(refEntityName);
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		AttributeMetaData attr = mock(AttributeMetaData.class);
		when(attr.getDataType()).thenReturn(attrType);
		when(attr.getRefEntity()).thenReturn(refEntityType);
		when(entityManager.getReference(refEntityType, "0")).thenReturn(entity0);
		when(entityManager.getReference(refEntityType, "1")).thenReturn(entity1);
		Object entityValue = restService.toEntityValue(attr, "0,1"); // string
		assertEquals(entityValue, Arrays.asList(entity0, entity1));
	}

	@Test
	public void toEntityValueXref()
	{
		Entity entity0 = mock(Entity.class);
		String refEntityName = "refEntity";
		AttributeMetaData refIdAttr = mock(AttributeMetaData.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);
		EntityType refEntityMeta = mock(EntityType.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		AttributeMetaData attr = mock(AttributeMetaData.class);
		when(attr.getDataType()).thenReturn(XREF);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(entityManager.getReference(refEntityMeta, "0")).thenReturn(entity0);
		assertEquals(restService.toEntityValue(attr, "0"), entity0);
	}

	@Test
	public void toEntityDateStringValueValid() throws ParseException
	{
		AttributeMetaData dateAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("dateAttr").getMock();
		when(dateAttr.getDataType()).thenReturn(DATE);
		assertEquals(restService.toEntityValue(dateAttr, "2000-12-31"),
				MolgenisDateFormat.getDateFormat().parse("2000-12-31"));
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Attribute \\[dateAttr\\] value \\[invalidDate\\] does not match date format \\[yyyy-MM-dd\\]")
	public void toEntityDateStringValueInvalid()
	{
		AttributeMetaData dateAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("dateAttr").getMock();
		when(dateAttr.getDataType()).thenReturn(DATE);
		restService.toEntityValue(dateAttr, "invalidDate");
	}
}
