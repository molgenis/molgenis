package org.molgenis.data.rest.service;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class RestServiceTest
{
	private RestService restService;
	private EntityManager entityManager;
	private DataService dataService;
	private FileMetaFactory fileMetaFactory;
	private IdGenerator idGenerator;
	private ServletUriComponentsBuilderFactory servletUriComponentsBuilderFactory;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		idGenerator = mock(IdGenerator.class);
		FileStore fileStore = mock(FileStore.class);
		fileMetaFactory = mock(FileMetaFactory.class);
		entityManager = mock(EntityManager.class);
		servletUriComponentsBuilderFactory = mock(ServletUriComponentsBuilderFactory.class);
		this.restService = new RestService(dataService, idGenerator, fileStore, fileMetaFactory, entityManager,
				servletUriComponentsBuilderFactory);
	}

	@Test
	public void toEntityValue()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDataType()).thenReturn(MREF);
		assertEquals(restService.toEntityValue(attr, null, "test"), emptyList());
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
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getDataType()).thenReturn(INT);
		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getId()).thenReturn(refEntityName);
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		Attribute attr = mock(Attribute.class);
		when(attr.getDataType()).thenReturn(attrType);
		when(attr.getRefEntity()).thenReturn(refEntityType);
		when(entityManager.getReference(refEntityType, 0)).thenReturn(entity0);
		when(entityManager.getReference(refEntityType, 1)).thenReturn(entity1);
		Object entityValue = restService.toEntityValue(attr, "0,1", "test"); // string
		assertEquals(entityValue, Arrays.asList(entity0, entity1));
	}

	@Test(dataProvider = "toEntityValueMrefProvider")
	public void toEntityValueMrefToStringAttr(AttributeType attrType)
	{
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		String refEntityName = "refEntity";
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);
		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getId()).thenReturn(refEntityName);
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		Attribute attr = mock(Attribute.class);
		when(attr.getDataType()).thenReturn(attrType);
		when(attr.getRefEntity()).thenReturn(refEntityType);
		when(entityManager.getReference(refEntityType, "0")).thenReturn(entity0);
		when(entityManager.getReference(refEntityType, "1")).thenReturn(entity1);
		Object entityValue = restService.toEntityValue(attr, "0,1", "test"); // string
		assertEquals(entityValue, Arrays.asList(entity0, entity1));
	}

	@Test
	public void toEntityValueXref()
	{
		Entity entity0 = mock(Entity.class);
		String refEntityName = "refEntity";
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);
		EntityType refEntityMeta = mock(EntityType.class);
		when(refEntityMeta.getId()).thenReturn(refEntityName);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		Attribute attr = mock(Attribute.class);
		when(attr.getDataType()).thenReturn(XREF);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(entityManager.getReference(refEntityMeta, "0")).thenReturn(entity0);
		assertEquals(restService.toEntityValue(attr, "0", "test"), entity0);
	}

	@Test
	public void toEntityDateStringValueValid() throws ParseException
	{
		Attribute dateAttr = when(mock(Attribute.class).getName()).thenReturn("dateAttr").getMock();
		when(dateAttr.getDataType()).thenReturn(DATE);
		assertEquals(restService.toEntityValue(dateAttr, "2000-12-31", "test"), LocalDate.parse("2000-12-31"));
	}

	@Test
	public void toEntityDateTimeStringValueValid() throws ParseException
	{
		Attribute dateAttr = when(mock(Attribute.class).getName()).thenReturn("dateAttr").getMock();
		when(dateAttr.getDataType()).thenReturn(DATE_TIME);

		Instant expected = Instant.parse("2000-12-31T10:34:56.789Z");
		assertEquals(restService.toEntityValue(dateAttr, "2000-12-31T10:34:56.789Z", "test"), expected);
	}

	@Test
	public void toEntityFileValueValid() throws ParseException
	{
		String generatedId = "id";
		String downloadUriAsString = "http://somedownloaduri";
		ServletUriComponentsBuilder mockBuilder = mock(ServletUriComponentsBuilder.class);
		UriComponents downloadUri = mock(UriComponents.class);
		FileMeta fileMeta = mock(FileMeta.class);

		Attribute fileAttr = when(mock(Attribute.class).getName()).thenReturn("fileAttr").getMock();
		when(fileAttr.getDataType()).thenReturn(FILE);
		when(idGenerator.generateId()).thenReturn(generatedId);
		when(fileMetaFactory.create(generatedId)).thenReturn(fileMeta);
		when(mockBuilder.replacePath(anyString())).thenReturn(mockBuilder);
		when(mockBuilder.replaceQuery(null)).thenReturn(mockBuilder);
		when(downloadUri.toUriString()).thenReturn(downloadUriAsString);
		when(mockBuilder.build()).thenReturn(downloadUri);
		when(servletUriComponentsBuilderFactory.fromCurrentRequest()).thenReturn(mockBuilder);

		byte[] content = { 'a', 'b' };
		MockMultipartFile mockMultipartFile = new MockMultipartFile("name", "fileName", "contentType", content);

		assertEquals(restService.toEntityValue(fileAttr, mockMultipartFile, null), fileMeta);
	}

	@Test
	public void toEntityFileValueWithoutFileInRequest() throws ParseException
	{
		int entityId = 12345;
		String fileName = "File name";
		String fileAttrName = "fileAttr";
		String oldEntityTypeId = "oldEntityTypeId";

		EntityType oldEntityType = mock(EntityType.class);
		Entity oldEntity = mock(Entity.class);
		FileMeta storedFileMeta = mock(FileMeta.class);
		Attribute fileAttr = mock(Attribute.class);
		Attribute idAttr = mock(Attribute.class);

		when(fileAttr.getName()).thenReturn(fileAttrName);
		when(fileAttr.getEntity()).thenReturn(oldEntityType);
		when(fileAttr.getDataType()).thenReturn(FILE);
		when(oldEntityType.getId()).thenReturn(oldEntityTypeId);
		when(oldEntityType.getIdAttribute()).thenReturn(idAttr);
		when(idAttr.getDataType()).thenReturn(INT);
		when(oldEntity.getEntity(fileAttrName)).thenReturn(storedFileMeta);
		when(storedFileMeta.get(FileMetaMetaData.FILENAME)).thenReturn(fileName);
		when(dataService.findOneById(fileAttr.getEntity().getId(), entityId)).thenReturn(oldEntity);

		Object result = restService.toEntityValue(fileAttr, fileName, entityId);
		assertEquals(result, storedFileMeta);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Failed to parse attribute \\[dateAttr\\] value \\[invalidDate\\] as date. Valid date format is \\[YYYY-MM-DD\\].")
	public void toEntityDateStringValueInvalid()
	{
		Attribute dateAttr = when(mock(Attribute.class).getName()).thenReturn("dateAttr").getMock();
		when(dateAttr.getDataType()).thenReturn(DATE);
		restService.toEntityValue(dateAttr, "invalidDate", "test");
	}

	@Test
	public void updateMappedByEntitiesEntity()
	{
		String refEntityName = "refEntityName";
		EntityType refEntityMeta = mock(EntityType.class);
		when(refEntityMeta.getId()).thenReturn(refEntityName);

		String mappedByAttrName = "mappedByAttr";
		Attribute mappedByAttr = mock(Attribute.class);
		when(mappedByAttr.getName()).thenReturn(mappedByAttrName);

		EntityType entityMeta = mock(EntityType.class);
		Attribute oneToManyAttr = mock(Attribute.class);
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
		when(entity.getEntityType()).thenReturn(entityMeta);
		restService.updateMappedByEntities(entity);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(dataService).update(eq(refEntityName), captor.capture());
		List<Entity> refEntities = captor.getValue().collect(toList());
		assertEquals(refEntities, newArrayList(refEntity0, refEntity1));
		verify(refEntity0).set(mappedByAttrName, entity);
		verify(refEntity1).set(mappedByAttrName, entity);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void updateMappedByEntitiesEntityEntity()
	{
		String refEntityName = "refEntityName";
		EntityType refEntityMeta = mock(EntityType.class);
		when(refEntityMeta.getId()).thenReturn(refEntityName);

		String mappedByAttrName = "mappedByAttr";
		Attribute mappedByAttr = mock(Attribute.class);
		when(mappedByAttr.getName()).thenReturn(mappedByAttrName);

		EntityType entityMeta = mock(EntityType.class);
		Attribute oneToManyAttr = mock(Attribute.class);
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
		when(entity.getEntityType()).thenReturn(entityMeta);

		Entity existingEntity = mock(Entity.class);
		when(existingEntity.getEntities(oneToManyAttrName)).thenReturn(newArrayList(refEntity1, refEntity2));
		when(existingEntity.getEntityType()).thenReturn(entityMeta);

		restService.updateMappedByEntities(entity, existingEntity);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(dataService).update(eq(refEntityName), captor.capture());
		List<Entity> refEntities = captor.getValue().collect(toList());
		assertEquals(refEntities, newArrayList(refEntity0, refEntity2));
		verify(refEntity0).set(mappedByAttrName, entity);
		verify(refEntity2).set(mappedByAttrName, null);
		verifyNoMoreInteractions(dataService);
	}
}
