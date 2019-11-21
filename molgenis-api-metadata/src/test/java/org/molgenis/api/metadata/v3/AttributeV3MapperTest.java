package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
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
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.convert.SortConverter;
import org.molgenis.api.data.SortMapper;
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributeResponseData;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.metadata.v3.model.Range;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class AttributeV3MapperTest extends AbstractMockitoTest {
  @Mock private EntityTypeFactory entityTypeFactory;
  @Mock private AttributeFactory attributeFactory;
  @Mock private MetaDataService metaDataService;
  @Mock private SortMapper sortMapper;
  @Mock private SortConverter sortConverter;
  @Mock private EntityManager entityManager;
  @Mock private EntityTypeMetadata entityTypeMetadata;

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
        CreateAttributeRequest.builder()
            .setId("MyAttributeId")
            .setName("MyAttributeName")
            .setType("long")
            .setRange(Range.create(-1L, 1L))
            .build();
    CreateEntityTypeRequest entityTypeRequest =
        CreateEntityTypeRequest.builder()
            .setLabel(I18nValue.builder().build())
            .setPackage("MyPackageId")
            .setAttributes(ImmutableList.of(createAttributeRequest))
            .build();

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
        () -> verify(attribute).setRange(new org.molgenis.data.Range(-1L, 1L)),
        () -> verify(attribute).setReadOnly(false),
        () -> verify(attribute).setUnique(false),
        () -> verify(attribute).setNullableExpression(null),
        () -> verify(attribute).setVisibleExpression(null),
        () -> verify(attribute).setValidationExpression(null),
        () -> verify(attribute).setDefaultValue(null),
        () -> verifyNoMoreInteractions(attribute));
  }
}
