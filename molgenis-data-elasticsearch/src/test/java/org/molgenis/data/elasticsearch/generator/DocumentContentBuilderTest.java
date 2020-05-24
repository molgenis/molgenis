package org.molgenis.data.elasticsearch.generator;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL_MREF;
import static org.molgenis.data.meta.AttributeType.EMAIL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.HTML;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.SCRIPT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.generator.model.Document;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentContentBuilderTest extends AbstractMockitoTest {
  @Mock private DocumentIdGenerator documentIdGenerator;

  private DocumentContentBuilder documentContentBuilder;

  @BeforeEach
  void setUpBeforeMethod() {
    when(documentIdGenerator.generateId(any(EntityType.class)))
        .thenAnswer(invocation -> invocation.<EntityType>getArgument(0).getId());
    when(documentIdGenerator.generateId(any(Attribute.class)))
        .thenAnswer(invocation -> invocation.<Attribute>getArgument(0).getIdentifier());
    documentContentBuilder = new DocumentContentBuilder(documentIdGenerator);
  }

  @Test
  void DocumentContentBuilder() {
    assertThrows(NullPointerException.class, () -> new DocumentContentBuilder(null));
  }

  @Test
  void createDocumentObject() {
    String entityId = "id";
    Document document = documentContentBuilder.createDocument(entityId);
    Document expectedDocument = Document.builder().setId(entityId).build();
    assertEquals(expectedDocument, document);
  }

  static Iterator<Object[]> createDocumentBoolProvider() {
    List<Object[]> dataItems = new ArrayList<>();
    dataItems.add(new Object[] {null, "{\"attr\":null}"});
    dataItems.add(new Object[] {true, "{\"attr\":true}"});
    dataItems.add(new Object[] {false, "{\"attr\":false}"});
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createDocumentBoolProvider")
  void createDocumentBool(Boolean value, String expectedContent) {
    String attrIdentifier = "attr";
    Entity entity = createEntity(attrIdentifier, AttributeType.BOOL);
    when(entity.getBoolean(attrIdentifier)).thenReturn(value);
    Document document = documentContentBuilder.createDocument(entity);
    assertDocumentEquals(document, expectedContent);
  }

  static Iterator<Object[]> createDocumentReferenceProvider() {
    List<Object[]> dataItems = new ArrayList<>();
    String refAttr = "refAttr";
    Entity refEntity = createEntity(refAttr, AttributeType.STRING);
    when(refEntity.getString(refAttr)).thenReturn("str");
    for (AttributeType attributeType : EnumSet.of(CATEGORICAL, FILE, XREF)) {
      dataItems.add(new Object[] {attributeType, null, "{\"attr\":null}"});
      dataItems.add(new Object[] {attributeType, refEntity, "{\"attr\":{\"refAttr\":\"str\"}}"});
    }
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createDocumentReferenceProvider")
  void createDocumentReferenceAttribute(
      AttributeType attributeType, Entity value, String expectedContent) {
    String attrIdentifier = "attr";
    Entity entity = createEntity(attrIdentifier, attributeType);
    when(entity.getEntity(attrIdentifier)).thenReturn(value);
    Document document = documentContentBuilder.createDocument(entity);
    assertDocumentEquals(document, expectedContent);
  }

  static Iterator<Object[]> createDocumentMultiReferenceProvider() {
    List<Object[]> dataItems = new ArrayList<>();
    String refAttr = "refAttr";
    Entity refEntity = createEntity(refAttr, AttributeType.STRING);
    when(refEntity.getString(refAttr)).thenReturn("str");
    for (AttributeType attributeType : EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY)) {
      dataItems.add(new Object[] {attributeType, emptyList(), "{\"attr\":null}"});
      dataItems.add(
          new Object[] {
            attributeType, singletonList(refEntity), "{\"attr\":[{\"refAttr\":\"str\"}]}"
          });
    }
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createDocumentMultiReferenceProvider")
  void createDocumentMultiReferenceAttribute(
      AttributeType attributeType, Iterable<Entity> values, String expectedContent) {
    String attrIdentifier = "attr";
    Entity entity = createEntity(attrIdentifier, attributeType);
    when(entity.getEntities(attrIdentifier)).thenReturn(values);
    Document document = documentContentBuilder.createDocument(entity);
    assertDocumentEquals(document, expectedContent);
  }

  static Iterator<Object[]> createDocumentDateProvider() {
    List<Object[]> dataItems = new ArrayList<>();
    dataItems.add(new Object[] {null, "{\"attr\":null}"});
    dataItems.add(new Object[] {LocalDate.parse("2017-06-19"), "{\"attr\":\"2017-06-19\"}"});
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createDocumentDateProvider")
  void createDocumentDate(LocalDate localDate, String expectedContent) {
    String attrIdentifier = "attr";
    Entity entity = createEntity(attrIdentifier, AttributeType.DATE);
    when(entity.getLocalDate(attrIdentifier)).thenReturn(localDate);
    Document document = documentContentBuilder.createDocument(entity);
    assertDocumentEquals(document, expectedContent);
  }

  static Iterator<Object[]> createDocumentDateTimeProvider() {
    List<Object[]> dataItems = new ArrayList<>();
    dataItems.add(new Object[] {null, "{\"attr\":null}"});
    dataItems.add(
        new Object[] {
          Instant.parse("2017-06-19T14:01:48.079Z"), "{\"attr\":\"2017-06-19T14:01:48.079Z\"}"
        });
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createDocumentDateTimeProvider")
  void createDocumentDateTime(Instant value, String expectedContent) {
    String attrIdentifier = "attr";
    Entity entity = createEntity(attrIdentifier, AttributeType.DATE_TIME);
    when(entity.getInstant(attrIdentifier)).thenReturn(value);
    Document document = documentContentBuilder.createDocument(entity);
    assertDocumentEquals(document, expectedContent);
  }

  static Iterator<Object[]> createDocumentDecimalProvider() {
    List<Object[]> dataItems = new ArrayList<>();
    dataItems.add(new Object[] {null, "{\"attr\":null}"});
    dataItems.add(new Object[] {-1.23d, "{\"attr\":-1.23}"});
    dataItems.add(new Object[] {0d, "{\"attr\":0.0}"});
    dataItems.add(new Object[] {4.56d, "{\"attr\":4.56}"});
    dataItems.add(new Object[] {Double.MIN_VALUE, "{\"attr\":" + Double.MIN_VALUE + "}"});
    dataItems.add(new Object[] {Double.MAX_VALUE, "{\"attr\":" + Double.MAX_VALUE + "}"});
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createDocumentDecimalProvider")
  void createDocumentDecimal(Double value, String expectedContent) {
    String attrIdentifier = "attr";
    Entity entity = createEntity(attrIdentifier, AttributeType.DECIMAL);
    when(entity.getDouble(attrIdentifier)).thenReturn(value);
    Document document = documentContentBuilder.createDocument(entity);
    assertDocumentEquals(document, expectedContent);
  }

  static Iterator<Object[]> createDocumentStringProvider() {
    List<Object[]> dataItems = new ArrayList<>();
    for (AttributeType attributeType :
        EnumSet.of(EMAIL, ENUM, HTML, HYPERLINK, SCRIPT, STRING, TEXT)) {
      dataItems.add(new Object[] {attributeType, null, "{\"attr\":null}"});
      dataItems.add(new Object[] {attributeType, "abc", "{\"attr\":\"abc\"}"});
      dataItems.add(new Object[] {attributeType, "", "{\"attr\":\"\"}"});
    }
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createDocumentStringProvider")
  void createDocumentString(AttributeType attributeType, String value, String expectedContent) {
    String attrIdentifier = "attr";
    Entity entity = createEntity(attrIdentifier, attributeType);
    when(entity.getString(attrIdentifier)).thenReturn(value);
    Document document = documentContentBuilder.createDocument(entity);
    assertDocumentEquals(document, expectedContent);
  }

  static Iterator<Object[]> createDocumentIntProvider() {
    List<Object[]> dataItems = new ArrayList<>();
    dataItems.add(new Object[] {null, "{\"attr\":null}"});
    dataItems.add(new Object[] {-1, "{\"attr\":-1}"});
    dataItems.add(new Object[] {1, "{\"attr\":1}"});
    dataItems.add(new Object[] {Integer.MIN_VALUE, "{\"attr\":" + Integer.MIN_VALUE + "}"});
    dataItems.add(new Object[] {Integer.MAX_VALUE, "{\"attr\":" + Integer.MAX_VALUE + "}"});
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createDocumentIntProvider")
  void createDocumentInt(Integer value, String expectedContent) {
    String attrIdentifier = "attr";
    Entity entity = createEntity(attrIdentifier, AttributeType.INT);
    when(entity.getInt(attrIdentifier)).thenReturn(value);
    Document document = documentContentBuilder.createDocument(entity);
    assertDocumentEquals(document, expectedContent);
  }

  static Iterator<Object[]> createDocumentLongProvider() {
    List<Object[]> dataItems = new ArrayList<>();
    dataItems.add(new Object[] {null, "{\"attr\":null}"});
    dataItems.add(new Object[] {-1L, "{\"attr\":-1}"});
    dataItems.add(new Object[] {1L, "{\"attr\":1}"});
    dataItems.add(new Object[] {Long.MIN_VALUE, "{\"attr\":" + Long.MIN_VALUE + "}"});
    dataItems.add(new Object[] {Long.MAX_VALUE, "{\"attr\":" + Long.MAX_VALUE + "}"});
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createDocumentLongProvider")
  void createDocumentLong(Long value, String expectedContent) {
    String attrIdentifier = "attr";
    Entity entity = createEntity(attrIdentifier, AttributeType.LONG);
    when(entity.getLong(attrIdentifier)).thenReturn(value);
    Document document = documentContentBuilder.createDocument(entity);
    assertDocumentEquals(document, expectedContent);
  }

  static Iterator<Object[]> createDocumentDepthProvider() {
    List<Object[]> dataItems = new ArrayList<>();
    dataItems.add(new Object[] {1, "{\"attr\":{\"refAttr\":null}}"});
    dataItems.add(new Object[] {2, "{\"attr\":{\"refAttr\":{\"refRefAttr\":null}}}"});
    dataItems.add(new Object[] {3, "{\"attr\":{\"refAttr\":{\"refRefAttr\":null}}}"});
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createDocumentDepthProvider")
  void createDocumentDepth(int indexingDepth, String expectedContent) {
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

  private static Entity createEntity(String attrIdentifier, AttributeType type) {
    return createEntity(attrIdentifier, type, 1);
  }

  private static Entity createEntity(String attrIdentifier, AttributeType type, int indexingDepth) {
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

  private void assertDocumentEquals(Document document, String expectedContent) {
    assertEquals("id", document.getId());
    var content = document.getContent();
    assertNotNull(content);
    try {
      content.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    assertEquals(expectedContent, content.getOutputStream().toString());
  }
}
