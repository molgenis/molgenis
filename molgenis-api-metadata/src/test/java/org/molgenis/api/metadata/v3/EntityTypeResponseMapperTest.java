package org.molgenis.api.metadata.v3;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.EntityTypeResponse;
import org.molgenis.api.metadata.v3.model.EntityTypeResponseData;
import org.molgenis.api.metadata.v3.model.EntityTypesResponse;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class EntityTypeResponseMapperTest extends AbstractMockitoTest {

  @Mock private AttributeResponseMapper attributeV3Mapper;
  private EntityTypeResponseMapperImpl entityTypeV3Mapper;

  @BeforeEach
  void setUpBeforeEach() {
    entityTypeV3Mapper = new EntityTypeResponseMapperImpl(attributeV3Mapper);
  }

  @Test
  void toEntityTypesResponse() throws URISyntaxException {
    MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
    mockHttpServletRequest.setMethod("GET");
    mockHttpServletRequest.setRequestURI("/api/metadata");
    mockHttpServletRequest.setQueryString("page=1");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));

    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("MyEntityTypeId");

    int total = 5;
    EntityTypes entityTypes =
        EntityTypes.builder().setEntityTypes(singletonList(entityType)).setTotal(total).build();

    int size = 1;
    int number = 1;

    EntityTypeResponseData entityTypeResponseData =
        EntityTypeResponseData.builder()
            .setId("MyEntityTypeId")
            .setAttributes(
                AttributesResponse.builder()
                    .setLinks(
                        LinksResponse.create(
                            null,
                            new URI("http://localhost/api/metadata/MyEntityTypeId/attributes"),
                            null))
                    .build())
            .setAbstract(false)
            .setIndexingDepth(0)
            .build();

    EntityTypeResponse entityTypeResponse =
        EntityTypeResponse.builder()
            .setLinks(
                LinksResponse.create(
                    null, new URI("http://localhost/api/metadata/MyEntityTypeId"), null))
            .setData(entityTypeResponseData)
            .build();

    EntityTypesResponse entityTypesResponse =
        EntityTypesResponse.builder()
            .setLinks(
                LinksResponse.create(
                    new URI("http://localhost/api/metadata?page=0"),
                    new URI("http://localhost/api/metadata?page=1"),
                    new URI("http://localhost/api/metadata?page=2")))
            .setItems(singletonList(entityTypeResponse))
            .setPage(PageResponse.create(size, total, 5, number))
            .build();
    assertEquals(
        entityTypesResponse, entityTypeV3Mapper.toEntityTypesResponse(entityTypes, size, number));
  }

  @Test
  void toEntityTypeResponse() throws URISyntaxException {
    MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
    mockHttpServletRequest.setMethod("GET");
    mockHttpServletRequest.setRequestURI("/api/metadata/MyEntityTypeId");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));

    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("MyEntityTypeId");
    doReturn("My Entity Type").when(entityType).getLabel();
    doReturn("My Entity Type").when(entityType).getLabel("en");
    doReturn("My Entity Type description").when(entityType).getDescription();
    doReturn("My Entity Type description").when(entityType).getDescription("en");
    when(entityType.getDescription()).thenReturn("My Entity Type description");
    when(entityType.getString("labelEn")).thenReturn("My Entity Type (en)");

    EntityTypeResponseData entityTypeResponseData =
        EntityTypeResponseData.builder()
            .setId("MyEntityTypeId")
            .setLabel("My Entity Type")
            .setLabelI18n(
                I18nValue.builder()
                    .setDefaultValue("My Entity Type")
                    .setTranslations(ImmutableMap.of("en", "My Entity Type (en)"))
                    .build())
            .setDescription("My Entity Type description")
            .setDescriptionI18n(
                I18nValue.builder()
                    .setDefaultValue("My Entity Type description")
                    .setTranslations(ImmutableMap.of())
                    .build())
            .setAttributes(
                AttributesResponse.builder()
                    .setLinks(
                        LinksResponse.create(
                            null,
                            new URI("http://localhost/api/metadata/MyEntityTypeId/attributes"),
                            null))
                    .setItems(ImmutableList.of())
                    .build())
            .setAbstract(false)
            .setIndexingDepth(0)
            .build();

    EntityTypeResponse entityTypeResponse =
        EntityTypeResponse.builder()
            .setLinks(
                LinksResponse.create(
                    null, new URI("http://localhost/api/metadata/MyEntityTypeId"), null))
            .setData(entityTypeResponseData)
            .build();

    assertEquals(
        entityTypeResponse, entityTypeV3Mapper.toEntityTypeResponse(entityType, true, true));
  }
}
