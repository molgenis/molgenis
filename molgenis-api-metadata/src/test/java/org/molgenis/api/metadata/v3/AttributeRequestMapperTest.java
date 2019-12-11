package org.molgenis.api.metadata.v3;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_READ_ONLY;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_UNIQUE;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.convert.SortConverter;
import org.molgenis.api.data.SortMapper;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.metadata.v3.model.Range;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.test.AbstractMockitoTest;

class AttributeRequestMapperTest extends AbstractMockitoTest {

  @Mock private AttributeFactory attributeFactory;
  @Mock private MetaDataService metaDataService;
  @Mock private SortMapper sortMapper;
  @Mock private SortConverter sortConverter;
  @Mock private EntityManager entityManager;
  @Mock private EntityTypeMetadata entityTypeMetadata;

  private AttributeRequestMapper attributeRequestMapper;

  @BeforeEach
  void setUpBeforeEach() {
    attributeRequestMapper =
        new AttributeRequestMapper(
            attributeFactory,
            metaDataService,
            sortMapper,
            sortConverter,
            entityManager,
            entityTypeMetadata);
  }

  @Test
  void testAttributeV3Mapper() {
    assertThrows(
        NullPointerException.class,
        () -> new AttributeRequestMapper(null, null, null, null, null, null));
  }

  @Test
  void toAttributes() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    CreateAttributeRequest createAttributeRequest =
        CreateAttributeRequest.builder()
            .setId("MyAttributeId")
            .setName("MyAttributeName")
            .setType("long")
            .setRange(Range.create(-1L, 1L))
            .build();
    CreateEntityTypeRequest entityTypeRequest =
        CreateEntityTypeRequest.builder()
            .setLabel(I18nValue.builder().setDefaultValue("My Entity Type").build())
            .setPackage("MyPackageId")
            .setAttributes(ImmutableList.of(createAttributeRequest))
            .build();

    EntityType entityType = mock(EntityType.class);

    List<Attribute> attributes =
        attributeRequestMapper.toAttributes(
            singletonList(createAttributeRequest), entityTypeRequest, entityType);

    assertAll(
        () -> assertEquals(1, attributes.size()),
        () -> verify(attribute).setIdAttribute(null),
        () -> verify(attribute).setLabelAttribute(null),
        () -> verify(attribute).setLookupAttributeIndex(null),
        () -> verify(attribute).setIdentifier("MyAttributeId"),
        () -> verify(attribute).setName("MyAttributeName"),
        () -> verify(attribute).setEntity(entityType),
        () -> verify(attribute).setSequenceNumber(0),
        () -> verify(attribute).setDataType(AttributeType.LONG),
        () -> verify(attribute).setOrderBy(null),
        () -> verify(attribute).setExpression(null),
        () -> verify(attribute).setRange(new org.molgenis.data.Range(-1L, 1L)),
        () -> verify(attribute).setNullableExpression(null),
        () -> verify(attribute).setVisibleExpression(null),
        () -> verify(attribute).setValidationExpression(null),
        () -> verify(attribute).setDefaultValue(null),
        () -> verify(attribute).getIdentifier(),
        () -> verifyNoMoreInteractions(attribute));
  }

  @Test
  void toAttributesMapSequenceNr() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", 1);
    attributeValueMap.put("sequenceNr", 1);
    EntityType entityType = mock(EntityType.class);
    attributeRequestMapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    verify(attribute).setSequenceNumber(1);
  }

  @Test
  void toAttributesMapName() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", 1);
    attributeValueMap.put("name", "test");
    EntityType entityType = mock(EntityType.class);
    attributeRequestMapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    verify(attribute).setName("test");
  }

  @Test
  void toAttributesBoolean() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", 1);
    attributeValueMap.put("readonly", true);
    attributeValueMap.put("visible", true);
    attributeValueMap.put("auto", true);
    attributeValueMap.put("nullable", true);
    attributeValueMap.put("cascadeDelete", true);
    attributeValueMap.put("aggregatable", true);
    attributeValueMap.put("unique", true);
    EntityType entityType = mock(EntityType.class);
    attributeRequestMapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    assertAll(
        () -> verify(attribute).set(IS_READ_ONLY, true),
        () -> verify(attribute).setVisible(true),
        () -> verify(attribute).setAuto(true),
        () -> verify(attribute).setNillable(true),
        () -> verify(attribute).setCascadeDelete(true),
        () -> verify(attribute).set(IS_UNIQUE, true),
        () -> verify(attribute).setAggregatable(true));
  }

  @Test
  void toAttributesRange() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    Map<String, Long> range = new HashMap<>();
    range.put("min", 1l);
    range.put("max", 10l);
    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", 1);
    attributeValueMap.put("range", range);
    EntityType entityType = mock(EntityType.class);
    attributeRequestMapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    verify(attribute).setRange(new org.molgenis.data.Range(1l, 10l));
  }

  @Test
  void toAttributesI18n() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    Map<String, Object> i18n = new HashMap<>();
    Map<String, String> translations = new HashMap<>();
    translations.put("nl", "nederlands");
    i18n.put("defaultValue", "default");
    i18n.put("translations", translations);
    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", 1);
    attributeValueMap.put("label", i18n);
    attributeValueMap.put("description", i18n);
    EntityType entityType = mock(EntityType.class);
    attributeRequestMapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    assertAll(
        () -> verify(attribute).setLabel("default"),
        () -> verify(attribute).setLabel("nl", "nederlands"),
        () -> verify(attribute).setDescription("default"),
        () -> verify(attribute).setDescription("nl", "nederlands"));
  }

  @Test
  void toAttributesParent() {
    Attribute attribute = mock(Attribute.class);
    when(attribute.getIdentifier()).thenReturn("attribute");
    when(attributeFactory.create()).thenReturn(attribute);

    Attribute parentAttr = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);
    EntityType parent = mock(EntityType.class);
    when(parent.getAttribute("test")).thenReturn(parentAttr);
    when(entityType.getExtends()).thenReturn(parent);

    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", "attribute");
    attributeValueMap.put("parent", "test");
    attributeRequestMapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    verify(attribute).setParent(parentAttr);
  }

  @Test
  void toAttributesMappedBy() {
    Attribute attribute = mock(Attribute.class);
    when(attribute.getIdentifier()).thenReturn("attribute");
    when(attributeFactory.create()).thenReturn(attribute);

    Attribute mappedByAttr = mock(Attribute.class);

    Repository<Attribute> repo = mock(Repository.class);
    when(metaDataService.getRepository(AttributeMetadata.ATTRIBUTE_META_DATA, Attribute.class))
        .thenReturn(Optional.of(repo));
    when(repo.findOneById("test")).thenReturn(mappedByAttr);

    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", "attribute");
    attributeValueMap.put("mappedByAttribute", "test");
    EntityType entityType = mock(EntityType.class);
    attributeRequestMapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    verify(attribute).setMappedBy(mappedByAttr);
  }

  @Test
  void toAttributesString() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", 1);
    attributeValueMap.put("expression", "expression");
    attributeValueMap.put("visibleExpression", "visibleExpression");
    attributeValueMap.put("nullableExpression", "nillableExpression");
    attributeValueMap.put("defaultValue", "defaultValue");
    attributeValueMap.put("validationExpression", "validationExpression");
    EntityType entityType = mock(EntityType.class);
    attributeRequestMapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    assertAll(
        () -> verify(attribute).setExpression("expression"),
        () -> verify(attribute).setNullableExpression("nillableExpression"),
        () -> verify(attribute).setValidationExpression("validationExpression"),
        () -> verify(attribute).setDefaultValue("defaultValue"),
        () -> verify(attribute).setVisibleExpression("visibleExpression"));
  }

  @Test
  void toAttributesEnum() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", 1);
    attributeValueMap.put("enumOptions", Arrays.asList("1", "2"));
    EntityType entityType = mock(EntityType.class);
    attributeRequestMapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    verify(attribute).setEnumOptions(Arrays.asList("1", "2"));
  }

  @Test
  void toAttributesOrder() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);
    org.molgenis.api.model.Sort sort = mock(org.molgenis.api.model.Sort.class);
    when(sortConverter.convert("-id")).thenReturn(sort);
    Sort molgenisSort = new Sort("id", Direction.DESC);
    when(sortMapper.map(sort)).thenReturn(molgenisSort);

    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", 1);
    attributeValueMap.put("orderBy", "-id");
    EntityType entityType = mock(EntityType.class);
    attributeRequestMapper.toAttributes(singletonList(attributeValueMap), entityType);

    verify(attribute).setOrderBy(molgenisSort);
  }

  @Test
  void toAttributesType() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", 1);
    attributeValueMap.put("type", "onetomany");
    EntityType entityType = mock(EntityType.class);
    attributeRequestMapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    verify(attribute).setDataType(AttributeType.ONE_TO_MANY);
  }

  @Test
  void toAttributesRefEntity() {
    Attribute attribute = mock(Attribute.class);
    when(attribute.getIdentifier()).thenReturn("attribute");
    when(attributeFactory.create()).thenReturn(attribute);

    EntityType entityType = mock(EntityType.class);
    when(entityManager.getReference(entityTypeMetadata, "ref")).thenReturn(entityType);

    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", "attribute");
    attributeValueMap.put("refEntityType", "ref");

    attributeRequestMapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    verify(attribute).setRefEntity(entityType);
  }
}
