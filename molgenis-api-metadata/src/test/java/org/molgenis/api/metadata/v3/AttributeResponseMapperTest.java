package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributeResponseData;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class AttributeResponseMapperTest extends AbstractMockitoTest {

  @Mock private MetaDataService metaDataService;

  private AttributeResponseMapperImpl attributeResponseMapper;

  @BeforeEach
  void setUpBeforeEach() {
    attributeResponseMapper = new AttributeResponseMapperImpl(metaDataService);
  }

  @Test
  void testAttributeV3Mapper() {
    assertThrows(NullPointerException.class, () -> new AttributeResponseMapperImpl(null));
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
            .setPage(PageResponse.create(1, 5, 1))
            .build();

    assertEquals(
        attributesResponse,
        attributeResponseMapper.toAttributesResponse(
            Attributes.create(ImmutableList.of(attribute), 5), 1, 1));
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
    doReturn("My Attribute").when(attribute).getLabel();
    doReturn(null).when(attribute).getLabel(any(String.class));
    doReturn("My Attribute description").when(attribute).getDescription();
    doReturn(null).when(attribute).getDescription(any(String.class));
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
            .setLabelI18n(
                I18nValue.builder()
                    .setDefaultValue("My Attribute")
                    .setTranslations(ImmutableMap.of())
                    .build())
            .setDescriptionI18n(
                I18nValue.builder()
                    .setDefaultValue("My Attribute description")
                    .setTranslations(ImmutableMap.of())
                    .build())
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
    assertEquals(attributeResponse, attributeResponseMapper.toAttributeResponse(attribute, true));
  }
}
