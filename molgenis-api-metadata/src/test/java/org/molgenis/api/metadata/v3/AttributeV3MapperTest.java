package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributeResponseData;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.metadata.v3.model.Range;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class AttributeV3MapperTest extends AbstractMockitoTest {

  @Mock
  private AttributeFactory attributeFactory;
  @Mock
  private MetaDataService metaDataService;
  @Mock
  private SortMapper sortMapper;
  @Mock
  private SortConverter sortConverter;
  @Mock
  private EntityManager entityManager;
  @Mock
  private EntityTypeMetadata entityTypeMetadata;

  private AttributeV3Mapper attributeV3Mapper;

  @BeforeEach
  void setUpBeforeEach() {
    attributeV3Mapper =
        new AttributeV3Mapper(
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
        () -> new AttributeV3Mapper(null, null, null, null, null, null));
  }

  @Test
  void mapAttributes() throws URISyntaxException {
    MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
    mockHttpServletRequest.setRequestURI("/api/metadata/MyEntityType/attributes");
    mockHttpServletRequest.setQueryString("page=1");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));

    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("MyEntityTypeId");

    Attribute attribute = mock(Attribute.class);
    when(attribute.getIdentifier()).thenReturn("MyAttributeId");
    when(attribute.getName()).thenReturn("MyAttributeName");
    when(attribute.getSequenceNumber()).thenReturn(2);
    when(attribute.getEntity()).thenReturn(entityType);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);
    when(attribute.getLookupAttributeIndex()).thenReturn(null);
    when(attribute.getCascadeDelete()).thenReturn(null);

    AttributeResponseData attributeResponseData =
        AttributeResponseData.builder()
            .setId("MyAttributeId")
            .setName("MyAttributeName")
            .setSequenceNr(2)
            .setType("string")
            .setIdAttribute(false)
            .setLabelAttribute(false)
            .setNullable(false)
            .setAuto(false)
            .setVisible(false)
            .setUnique(false)
            .setReadOnly(false)
            .setAggregatable(false)
            .build();

    AttributeResponse attributeResponse =
        AttributeResponse.builder()
            .setLink(
                LinksResponse.create(
                    null,
                    new URI(
                        "http://localhost/api/metadata/MyEntityTypeId/attributes/MyAttributeId"),
                    null))
            .setData(attributeResponseData)
            .build();

    AttributesResponse attributesResponse =
        AttributesResponse.builder()
            .setLinks(
                LinksResponse.create(
                    new URI("http://localhost/api/metadata/MyEntityType/attributes?page=0"),
                    new URI("http://localhost/api/metadata/MyEntityType/attributes?page=1"),
                    new URI("http://localhost/api/metadata/MyEntityType/attributes?page=2")))
            .setItems(ImmutableList.of(attributeResponse))
            .setPage(PageResponse.create(1, 5, 5, 1))
            .build();

    assertEquals(
        attributesResponse,
        attributeV3Mapper.mapAttributes(
            Attributes.create(ImmutableList.of(attribute), 5), 1, 1, 5));
  }

  @Test
  void mapAttribute() throws URISyntaxException {
    MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));

    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("MyEntityTypeId");

    Attribute attribute = mock(Attribute.class);
    when(attribute.getIdentifier()).thenReturn("MyAttributeId");
    when(attribute.getName()).thenReturn("MyAttributeName");
    when(attribute.getSequenceNumber()).thenReturn(2);
    when(attribute.getEntity()).thenReturn(entityType);
    when(attribute.getDataType()).thenReturn(AttributeType.ENUM);
    when(attribute.getEnumOptions()).thenReturn(asList("option0", "option1"));
    when(attribute.getLookupAttributeIndex()).thenReturn(null);
    when(attribute.getCascadeDelete()).thenReturn(null);

    AttributeResponseData attributeResponseData =
        AttributeResponseData.builder()
            .setId("MyAttributeId")
            .setName("MyAttributeName")
            .setSequenceNr(2)
            .setType("enum")
            .setEnumOptions(ImmutableList.of("option0", "option1"))
            .setIdAttribute(false)
            .setLabelAttribute(false)
            .setNullable(false)
            .setAuto(false)
            .setVisible(false)
            .setUnique(false)
            .setReadOnly(false)
            .setAggregatable(false)
            .setLabelI18n(I18nValue.builder().setTranslations(ImmutableMap.of()).build())
            .setDescriptionI18n(I18nValue.builder().setTranslations(ImmutableMap.of()).build())
            .build();

    AttributeResponse attributeResponse =
        AttributeResponse.builder()
            .setLink(
                LinksResponse.create(
                    null,
                    new URI(
                        "http://localhost/api/metadata/MyEntityTypeId/attributes/MyAttributeId"),
                    null))
            .setData(attributeResponseData)
            .build();
    assertEquals(attributeResponse, attributeV3Mapper.mapAttribute(attribute, true));
  }

  @Test
  void toAttributes() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    CreateAttributeRequest createAttributeRequest =
        new CreateAttributeRequest(
            "MyAttributeId",
            "MyAttributeName",
            "long",
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            false,
            false,
            null,
            null,
            false,
            emptyList(),
            Range.create(-1L, 1L),
            false,
            false,
            null,
            null,
            null,
            null,
            null);
    CreateEntityTypeRequest entityTypeRequest =
        new CreateEntityTypeRequest(
            "MyEntityTypeId",
            null,
            null,
            true,
            "MyPackageId",
            null,
            singletonList(createAttributeRequest),
            null,
            null,
            emptyList());

    Package entityTypePackage = mock(Package.class);

    EntityType entityType = mock(EntityType.class);

    Map<String, Attribute> attributes =
        attributeV3Mapper.toAttributes(
            singletonList(createAttributeRequest), entityTypeRequest, entityType);

    assertAll(
        () -> assertTrue(attributes.containsKey("MyAttributeId")),
        () -> assertEquals(1, attributes.size()),
        () -> verify(attribute).setIdentifier("MyAttributeId"),
        () -> verify(attribute).setName("MyAttributeName"),
        () -> verify(attribute).setEntity(entityType),
        () -> verify(attribute).setSequenceNumber(0),
        () -> verify(attribute).setDataType(AttributeType.LONG),
        () -> verify(attribute).setOrderBy(null),
        () -> verify(attribute).setExpression(null),
        () -> verify(attribute).setNillable(false),
        () -> verify(attribute).setAuto(false),
        () -> verify(attribute).setVisible(false),
        () -> verify(attribute).setAggregatable(false),
        () -> verify(attribute).setEnumOptions(emptyList()),
        () -> verify(attribute).setRange(new org.molgenis.data.Range(-1L, 1L)),
        () -> verify(attribute).setReadOnly(false),
        () -> verify(attribute).setUnique(false),
        () -> verify(attribute).setNullableExpression(null),
        () -> verify(attribute).setVisibleExpression(null),
        () -> verify(attribute).setValidationExpression(null),
        () -> verify(attribute).setDefaultValue(null),
        () -> verifyNoMoreInteractions(attribute));
  }

  @Test
  void toAttributesMapSequenceNr() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", 1);
    attributeValueMap.put("sequenceNumber", 1);
    EntityType entityType = mock(EntityType.class);
    attributeV3Mapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

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
    attributeV3Mapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

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
    attributeV3Mapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    assertAll(
        () -> verify(attribute).setReadOnly(true),
        () -> verify(attribute).setVisible(true),
        () -> verify(attribute).setAuto(true),
        () -> verify(attribute).setNillable(true),
        () -> verify(attribute).setCascadeDelete(true),
        () -> verify(attribute).setUnique(true),
        () -> verify(attribute).setAggregatable(true));
  }

  @Test
  void toAttributesRange() {
      Attribute attribute = mock(Attribute.class);
      when(attributeFactory.create()).thenReturn(attribute);

      Map<String, Long> range = new HashMap<>();
      range.put("min",1l);
      range.put("max", 10l);
      Map<String, Object> attributeValueMap = new HashMap<>();
      attributeValueMap.put("id", 1);
      attributeValueMap.put("range", range);
      EntityType entityType = mock(EntityType.class);
      attributeV3Mapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

      verify(attribute).setRange(new org.molgenis.data.Range(1l,10l));
  }

  @Test
  void toAttributesI18n() {
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    Map<String, Object> i18n = new HashMap<>();
    Map<String, String> translations = new HashMap<>();
    translations.put("nl", "nederlands");
    i18n.put("defaultValue","default");
    i18n.put("translations", translations);
    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", 1);
    attributeValueMap.put("label", i18n);
    attributeValueMap.put("description", i18n);
    EntityType entityType = mock(EntityType.class);
    attributeV3Mapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    assertAll(
        () -> verify(attribute).setLabel("default"),
        () -> verify(attribute).setLabel("nl","nederlands"),
        () -> verify(attribute).setDescription("default"),
        () -> verify(attribute).setDescription("nl","nederlands"));
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
    attributeV3Mapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    verify(attribute).setParent(parentAttr);
  }


  @Test
  void toAttributesMappedBy() {
    Attribute attribute = mock(Attribute.class);
    when(attribute.getIdentifier()).thenReturn("attribute");
    when(attributeFactory.create()).thenReturn(attribute);

    Attribute mappedByAttr = mock(Attribute.class);

    Repository<Attribute> repo = mock(Repository.class);
    when(metaDataService
        .getRepository(AttributeMetadata.ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(
        Optional.of(repo));
    when(repo.findOneById("test")).thenReturn(mappedByAttr);

    Map<String, Object> attributeValueMap = new HashMap<>();
    attributeValueMap.put("id", "attribute");
    attributeValueMap.put("mappedByAttribute", "test");
    EntityType entityType = mock(EntityType.class);
    attributeV3Mapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

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
    attributeV3Mapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

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
    attributeV3Mapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

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
    attributeV3Mapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

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
    attributeV3Mapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

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

    attributeV3Mapper.toAttributes(Collections.singletonList(attributeValueMap), entityType);

    verify(attribute).setRefEntity(entityType);
  }

}
