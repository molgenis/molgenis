package org.molgenis.data.rest.service;

import org.mockito.ArgumentCaptor;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
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
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class RestServiceTest
{
	private RestService restService;
	private EntityManager entityManager;
	private DataService dataService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
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
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		AttributeMetaData attr = mock(AttributeMetaData.class);
		when(attr.getDataType()).thenReturn(attrType);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(entityManager.getReference(refEntityMeta, 0)).thenReturn(entity0);
		when(entityManager.getReference(refEntityMeta, 1)).thenReturn(entity1);
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
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		AttributeMetaData attr = mock(AttributeMetaData.class);
		when(attr.getDataType()).thenReturn(attrType);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(entityManager.getReference(refEntityMeta, "0")).thenReturn(entity0);
		when(entityManager.getReference(refEntityMeta, "1")).thenReturn(entity1);
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
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
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

	@Test
	public void updateMappedByEntitiesEntity() {
		String refEntityName = "refEntityName";
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);

		String mappedByAttrName = "mappedByAttr";
		AttributeMetaData mappedByAttr = mock(AttributeMetaData.class);
		when(mappedByAttr.getName()).thenReturn(mappedByAttrName);

		EntityMetaData entityMeta = mock(EntityMetaData.class);
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		String oneToManyAttrName = "oneToManyAttr";
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.isMappedBy()).thenReturn(true);
		when(oneToManyAttr.getMappedBy()).thenReturn(mappedByAttr);
		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(entityMeta.getMappedByAttributes()).thenReturn(Stream.of(oneToManyAttr));

		Entity refEntity0 = mock(Entity.class);
		Entity refEntity1 = mock(Entity.class);

		Entity entity = mock(Entity.class);
		when(entity.getEntities(oneToManyAttrName)).thenReturn(newArrayList(refEntity0, refEntity1));
		when(entity.getEntityMetaData()).thenReturn(entityMeta);
		restService.updateMappedByEntities(entity);

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).update(eq(refEntityName), captor.capture());
		List<Entity> refEntities = captor.getValue().collect(toList());
		assertEquals(refEntities, newArrayList(refEntity0, refEntity1));
		verify(refEntity0).set(mappedByAttrName, entity);
		verify(refEntity1).set(mappedByAttrName, entity);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void updateMappedByEntitiesEntityEntity() {
		String refEntityName = "refEntityName";
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);

		String mappedByAttrName = "mappedByAttr";
		AttributeMetaData mappedByAttr = mock(AttributeMetaData.class);
		when(mappedByAttr.getName()).thenReturn(mappedByAttrName);

		EntityMetaData entityMeta = mock(EntityMetaData.class);
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		String oneToManyAttrName = "oneToManyAttr";
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.isMappedBy()).thenReturn(true);
		when(oneToManyAttr.getMappedBy()).thenReturn(mappedByAttr);
		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(entityMeta.getMappedByAttributes()).thenReturn(Stream.of(oneToManyAttr));

		Entity refEntity0 = when(mock(Entity.class).getIdValue()).thenReturn("refEntity0").getMock();
		Entity refEntity1 = when(mock(Entity.class).getIdValue()).thenReturn("refEntity1").getMock();
		Entity refEntity2 = when(mock(Entity.class).getIdValue()).thenReturn("refEntity2").getMock();

		Entity entity = mock(Entity.class);
		when(entity.getEntities(oneToManyAttrName)).thenReturn(newArrayList(refEntity0, refEntity1));
		when(entity.getEntityMetaData()).thenReturn(entityMeta);

		Entity existingEntity = mock(Entity.class);
		when(existingEntity.getEntities(oneToManyAttrName)).thenReturn(newArrayList(refEntity1, refEntity2));
		when(existingEntity.getEntityMetaData()).thenReturn(entityMeta);

		restService.updateMappedByEntities(entity, existingEntity);

		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).update(eq(refEntityName), captor.capture());
		List<Entity> refEntities = captor.getValue().collect(toList());
		assertEquals(refEntities, newArrayList(refEntity0, refEntity2));
		verify(refEntity0).set(mappedByAttrName, entity);
		verify(refEntity2).set(mappedByAttrName, null);
		verifyNoMoreInteractions(dataService);
	}
}
