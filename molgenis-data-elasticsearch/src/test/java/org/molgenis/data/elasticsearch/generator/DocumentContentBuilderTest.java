package org.molgenis.data.elasticsearch.generator;

import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.generator.model.Document;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class DocumentContentBuilderTest extends AbstractMockitoTest
{
	@Mock
	private DocumentIdGenerator documentIdGenerator;

	private DocumentContentBuilder documentContentBuilder;

	public DocumentContentBuilderTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(documentIdGenerator.generateId(any(EntityType.class))).thenAnswer(
				invocation -> invocation.<EntityType>getArgument(0).getId());
		when(documentIdGenerator.generateId(any(Attribute.class))).thenAnswer(
				invocation -> invocation.<Attribute>getArgument(0).getIdentifier());
		documentContentBuilder = new DocumentContentBuilder(documentIdGenerator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void DocumentContentBuilder()
	{
		new DocumentContentBuilder(null);
	}

	@Test
	public void createDocumentObject()
	{
		String entityId = "id";
		Document document = documentContentBuilder.createDocument(entityId);
		Document expectedDocument = Document.builder().setId(entityId).build();
		assertEquals(document, expectedDocument);
	}

	@DataProvider(name = "createDocumentBool")
	public static Iterator<Object[]> createDocumentBoolProvider()
	{
		List<Object[]> dataItems = new ArrayList<>();
		dataItems.add(new Object[] { null, "{\"attr\":null}" });
		dataItems.add(new Object[] { true, "{\"attr\":true}" });
		dataItems.add(new Object[] { false, "{\"attr\":false}" });
		return dataItems.iterator();
	}

	@Test(dataProvider = "createDocumentBool")
	public void createDocumentBool(Boolean value, String expectedContent)
	{
		String attrIdentifier = "attr";
		Entity entity = createEntity(attrIdentifier, AttributeType.BOOL);
		when(entity.getBoolean(attrIdentifier)).thenReturn(value);
		Document document = documentContentBuilder.createDocument(entity);
		assertDocumentEquals(document, expectedContent);
	}

	@DataProvider(name = "createDocumentReference")
	public static Iterator<Object[]> createDocumentReferenceProvider()
	{
		List<Object[]> dataItems = new ArrayList<>();
		String refAttr = "refAttr";
		Entity refEntity = createEntity(refAttr, AttributeType.STRING);
		when(refEntity.getString(refAttr)).thenReturn("str");
		for (AttributeType attributeType : EnumSet.of(CATEGORICAL, FILE, XREF))
		{
			dataItems.add(new Object[] { attributeType, null, "{\"attr\":null}" });
			dataItems.add(new Object[] { attributeType, refEntity, "{\"attr\":{\"refAttr\":\"str\"}}" });
		}
		return dataItems.iterator();
	}

	@Test(dataProvider = "createDocumentReference")
	public void createDocumentReferenceAttribute(AttributeType attributeType, Entity value, String expectedContent)
	{
		String attrIdentifier = "attr";
		Entity entity = createEntity(attrIdentifier, attributeType);
		when(entity.getEntity(attrIdentifier)).thenReturn(value);
		Document document = documentContentBuilder.createDocument(entity);
		assertDocumentEquals(document, expectedContent);
	}

	@DataProvider(name = "createDocumentMultiReference")
	public static Iterator<Object[]> createDocumentMultiReferenceProvider()
	{
		List<Object[]> dataItems = new ArrayList<>();
		String refAttr = "refAttr";
		Entity refEntity = createEntity(refAttr, AttributeType.STRING);
		when(refEntity.getString(refAttr)).thenReturn("str");
		for (AttributeType attributeType : EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY))
		{
			dataItems.add(new Object[] { attributeType, emptyList(), "{\"attr\":null}" });
			dataItems.add(
					new Object[] { attributeType, singletonList(refEntity), "{\"attr\":[{\"refAttr\":\"str\"}]}" });
		}
		return dataItems.iterator();
	}

	@Test(dataProvider = "createDocumentMultiReference")
	public void createDocumentMultiReferenceAttribute(AttributeType attributeType, Iterable<Entity> values,
			String expectedContent)
	{
		String attrIdentifier = "attr";
		Entity entity = createEntity(attrIdentifier, attributeType);
		when(entity.getEntities(attrIdentifier)).thenReturn(values);
		Document document = documentContentBuilder.createDocument(entity);
		assertDocumentEquals(document, expectedContent);
	}

	@DataProvider(name = "createDocumentDate")
	public static Iterator<Object[]> createDocumentDateProvider()
	{
		List<Object[]> dataItems = new ArrayList<>();
		dataItems.add(new Object[] { null, "{\"attr\":null}" });
		dataItems.add(new Object[] { LocalDate.parse("2017-06-19"), "{\"attr\":\"2017-06-19\"}" });
		return dataItems.iterator();
	}

	@Test(dataProvider = "createDocumentDate")
	public void createDocumentDate(LocalDate localDate, String expectedContent)
	{
		String attrIdentifier = "attr";
		Entity entity = createEntity(attrIdentifier, AttributeType.DATE);
		when(entity.getLocalDate(attrIdentifier)).thenReturn(localDate);
		Document document = documentContentBuilder.createDocument(entity);
		assertDocumentEquals(document, expectedContent);
	}

	@DataProvider(name = "createDocumentDateTime")
	public static Iterator<Object[]> createDocumentDateTimeProvider()
	{
		List<Object[]> dataItems = new ArrayList<>();
		dataItems.add(new Object[] { null, "{\"attr\":null}" });
		dataItems.add(
				new Object[] { Instant.parse("2017-06-19T14:01:48.079Z"), "{\"attr\":\"2017-06-19T14:01:48.079Z\"}" });
		return dataItems.iterator();
	}

	@Test(dataProvider = "createDocumentDateTime")
	public void createDocumentDateTime(Instant value, String expectedContent)
	{
		String attrIdentifier = "attr";
		Entity entity = createEntity(attrIdentifier, AttributeType.DATE_TIME);
		when(entity.getInstant(attrIdentifier)).thenReturn(value);
		Document document = documentContentBuilder.createDocument(entity);
		assertDocumentEquals(document, expectedContent);
	}

	@DataProvider(name = "createDocumentDecimal")
	public static Iterator<Object[]> createDocumentDecimalProvider()
	{
		List<Object[]> dataItems = new ArrayList<>();
		dataItems.add(new Object[] { null, "{\"attr\":null}" });
		dataItems.add(new Object[] { -1.23d, "{\"attr\":-1.23}" });
		dataItems.add(new Object[] { 0d, "{\"attr\":0.0}" });
		dataItems.add(new Object[] { 4.56d, "{\"attr\":4.56}" });
		dataItems.add(new Object[] { Double.MIN_VALUE, "{\"attr\":" + Double.MIN_VALUE + "}" });
		dataItems.add(new Object[] { Double.MAX_VALUE, "{\"attr\":" + Double.MAX_VALUE + "}" });
		return dataItems.iterator();
	}

	@Test(dataProvider = "createDocumentDecimal")
	public void createDocumentDecimal(Double value, String expectedContent)
	{
		String attrIdentifier = "attr";
		Entity entity = createEntity(attrIdentifier, AttributeType.DECIMAL);
		when(entity.getDouble(attrIdentifier)).thenReturn(value);
		Document document = documentContentBuilder.createDocument(entity);
		assertDocumentEquals(document, expectedContent);
	}

	@DataProvider(name = "createDocumentString")
	public static Iterator<Object[]> createDocumentStringProvider()
	{
		List<Object[]> dataItems = new ArrayList<>();
		for (AttributeType attributeType : EnumSet.of(EMAIL, ENUM, HTML, HYPERLINK, SCRIPT, STRING, TEXT))
		{
			dataItems.add(new Object[] { attributeType, null, "{\"attr\":null}" });
			dataItems.add(new Object[] { attributeType, "abc", "{\"attr\":\"abc\"}" });
			dataItems.add(new Object[] { attributeType, "", "{\"attr\":\"\"}" });
		}
		return dataItems.iterator();
	}

	@Test(dataProvider = "createDocumentString")
	public void createDocumentString(AttributeType attributeType, String value, String expectedContent)
	{
		String attrIdentifier = "attr";
		Entity entity = createEntity(attrIdentifier, attributeType);
		when(entity.getString(attrIdentifier)).thenReturn(value);
		Document document = documentContentBuilder.createDocument(entity);
		assertDocumentEquals(document, expectedContent);
	}

	@DataProvider(name = "createDocumentInt")
	public static Iterator<Object[]> createDocumentIntProvider()
	{
		List<Object[]> dataItems = new ArrayList<>();
		dataItems.add(new Object[] { null, "{\"attr\":null}" });
		dataItems.add(new Object[] { -1, "{\"attr\":-1}" });
		dataItems.add(new Object[] { 1, "{\"attr\":1}" });
		dataItems.add(new Object[] { Integer.MIN_VALUE, "{\"attr\":" + Integer.MIN_VALUE + "}" });
		dataItems.add(new Object[] { Integer.MAX_VALUE, "{\"attr\":" + Integer.MAX_VALUE + "}" });
		return dataItems.iterator();
	}

	@Test(dataProvider = "createDocumentInt")
	public void createDocumentInt(Integer value, String expectedContent)
	{
		String attrIdentifier = "attr";
		Entity entity = createEntity(attrIdentifier, AttributeType.INT);
		when(entity.getInt(attrIdentifier)).thenReturn(value);
		Document document = documentContentBuilder.createDocument(entity);
		assertDocumentEquals(document, expectedContent);
	}

	@DataProvider(name = "createDocumentLong")
	public static Iterator<Object[]> createDocumentLongProvider()
	{
		List<Object[]> dataItems = new ArrayList<>();
		dataItems.add(new Object[] { null, "{\"attr\":null}" });
		dataItems.add(new Object[] { -1L, "{\"attr\":-1}" });
		dataItems.add(new Object[] { 1L, "{\"attr\":1}" });
		dataItems.add(new Object[] { Long.MIN_VALUE, "{\"attr\":" + Long.MIN_VALUE + "}" });
		dataItems.add(new Object[] { Long.MAX_VALUE, "{\"attr\":" + Long.MAX_VALUE + "}" });
		return dataItems.iterator();
	}

	@Test(dataProvider = "createDocumentLong")
	public void createDocumentLong(Long value, String expectedContent)
	{
		String attrIdentifier = "attr";
		Entity entity = createEntity(attrIdentifier, AttributeType.LONG);
		when(entity.getLong(attrIdentifier)).thenReturn(value);
		Document document = documentContentBuilder.createDocument(entity);
		assertDocumentEquals(document, expectedContent);
	}

	@DataProvider(name = "createDocumentDepth")
	public static Iterator<Object[]> createDocumentDepthProvider()
	{
		List<Object[]> dataItems = new ArrayList<>();
		dataItems.add(new Object[] { 1, "{\"attr\":{\"refAttr\":null}}" });
		dataItems.add(new Object[] { 2, "{\"attr\":{\"refAttr\":{\"refRefAttr\":null}}}" });
		dataItems.add(new Object[] { 3, "{\"attr\":{\"refAttr\":{\"refRefAttr\":null}}}" });
		return dataItems.iterator();
	}

	@Test(dataProvider = "createDocumentDepth")
	public void createDocumentDepth(int indexingDepth, String expectedContent)
	{
		String refRefAttrIdentifier = "refRefAttr";
		Entity refRefEntity = createEntity(refRefAttrIdentifier, AttributeType.DECIMAL);
		when(refRefEntity.getDouble(refRefAttrIdentifier)).thenReturn(null);

		String refAttrIdentifier = "refAttr";
		Entity refEntity = createEntity(refAttrIdentifier, AttributeType.XREF);
		when(refEntity.getEntity(refAttrIdentifier)).thenReturn(refRefEntity);

		String attrIdentifier = "attr";
		Entity entity = createEntity(attrIdentifier, AttributeType.XREF, indexingDepth);
		when(entity.getEntity(attrIdentifier)).thenReturn(refEntity);
		Document document = documentContentBuilder.createDocument(entity);
		assertDocumentEquals(document, expectedContent);
	}

	private static Entity createEntity(String attrIdentifier, AttributeType type)
	{
		return createEntity(attrIdentifier, type, 1);
	}

	private static Entity createEntity(String attrIdentifier, AttributeType type, int indexingDepth)
	{
		Attribute attribute = mock(Attribute.class);
		when(attribute.getIdentifier()).thenReturn(attrIdentifier);
		when(attribute.getName()).thenReturn(attrIdentifier);
		when(attribute.getDataType()).thenReturn(type);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("id");
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));
		when(entityType.getLabelAttribute()).thenReturn(attribute);
		when(entityType.getIndexingDepth()).thenReturn(indexingDepth);

		Entity entity = mock(Entity.class);
		when(entity.getEntityType()).thenReturn(entityType);
		when(entity.getIdValue()).thenReturn("id");

		return entity;
	}

	private void assertDocumentEquals(Document document, String expectedContent)
	{
		assertEquals(document.getId(), "id");
		assertNotNull(document.getContent());
		try
		{
			assertEquals(document.getContent().string(), expectedContent);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
