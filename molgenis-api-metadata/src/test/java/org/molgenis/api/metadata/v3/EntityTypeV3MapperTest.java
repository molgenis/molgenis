package org.molgenis.api.metadata.v3;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.EntityTypeResponse;
import org.molgenis.api.metadata.v3.model.EntityTypeResponseData;
import org.molgenis.api.metadata.v3.model.EntityTypesResponse;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.w3c.dom.Attr;

class EntityTypeV3MapperTest extends AbstractMockitoTest {

  @Mock
  private EntityTypeFactory entityTypeFactory;
  @Mock
  private AttributeV3Mapper attributeV3Mapper;
  @Mock
  private MetaDataService metaDataService;
  private EntityTypeV3Mapper entityTypeV3Mapper;

  @BeforeEach
  void setUpBeforeEach() {
    entityTypeV3Mapper =
        new EntityTypeV3Mapper(entityTypeFactory, attributeV3Mapper, metaDataService);
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
            .setAbstract_(false)
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
        entityTypesResponse,
        entityTypeV3Mapper.toEntityTypesResponse(entityTypes, size, number, total));
  }

  @Test
  void toEntityTypeResponse() throws URISyntaxException {
    MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
    mockHttpServletRequest.setMethod("GET");
    mockHttpServletRequest.setRequestURI("/api/metadata/MyEntityTypeId");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));

    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("MyEntityTypeId");
    when(entityType.getLabel()).thenReturn("My Entity Type");
    when(entityType.getLabel("en")).thenReturn("My Entity Type");
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
            .setDescriptionI18n(I18nValue.builder().setTranslations(ImmutableMap.of()).build())
            .setAttributes(
                AttributesResponse.builder()
                    .setLinks(
                        LinksResponse.create(
                            null,
                            new URI("http://localhost/api/metadata/MyEntityTypeId/attributes"),
                            null))
                    .setItems(ImmutableList.of())
                    .build())
            .setAbstract_(false)
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

  @Test
  void toEntityType() {
    String id = "MyEntityTypeId";
    I18nValue label = I18nValue.builder().setDefaultValue("My Entity Type").build();
    I18nValue description = null;
    Boolean abstract_ = false;
    String packageId = "MyPackageId";
    String extendsEntityTypeId = "MyExtendsEntityTypeId";
    List<CreateAttributeRequest> attributes = emptyList();
    String idAttribute = null;
    String labelAttribute = null;
    List<String> lookupAttributes = ImmutableList.of();
    CreateEntityTypeRequest createEntityTypeRequest =
        new CreateEntityTypeRequest(
            id,
            label,
            description,
            abstract_,
            packageId,
            extendsEntityTypeId,
            attributes,
            idAttribute,
            labelAttribute,
            lookupAttributes);

    Package aPackage = mock(Package.class);
    when(metaDataService.getPackage(packageId)).thenReturn(Optional.of(aPackage));
    EntityType extendsEntityType = mock(EntityType.class);
    when(metaDataService.getEntityType(extendsEntityTypeId))
        .thenReturn(Optional.of(extendsEntityType));
    EntityType entityType = mock(EntityType.class);
    RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
    when(repositoryCollection.getName()).thenReturn("PostgreSQL");
    when(metaDataService.getDefaultBackend()).thenReturn(repositoryCollection);
    when(entityTypeFactory.create()).thenReturn(entityType);
    EntityType mappedEntityType = entityTypeV3Mapper.toEntityType(createEntityTypeRequest);

    assertAll(
        () -> verify(mappedEntityType).setId("MyEntityTypeId"),
        () -> verify(mappedEntityType).setLabel("My Entity Type"),
        () -> verify(mappedEntityType).setBackend("PostgreSQL"),
        () -> verify(mappedEntityType).setPackage(aPackage),
        () -> verify(mappedEntityType).setExtends(extendsEntityType),
        () -> verify(mappedEntityType).setOwnAllAttributes(emptyList()),
        () -> verify(mappedEntityType).setAbstract(false),
        () -> verifyNoMoreInteractions(mappedEntityType));
  }

  @Test
  void toEntityTypeMap() {
    EntityType entityType = mock(EntityType.class);

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("abstract_", true);

    entityTypeV3Mapper.toEntityType(entityType, valueMap);

    verify(entityType).setAbstract(true);
  }

  @Test
  void toEntityTypePack() {
    EntityType entityType = mock(EntityType.class);
    Package pack = mock(Package.class);
    when(metaDataService.getPackage("pack1")).thenReturn(Optional.of(pack));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("package_", "pack1");
    entityTypeV3Mapper.toEntityType(entityType, valueMap);

    verify(entityType).setPackage(pack);
  }

  @Test
  void toEntityTypeExtends() {
    EntityType entityType = mock(EntityType.class);
    EntityType parent = mock(EntityType.class);
    when(metaDataService.getEntityType("parent")).thenReturn(Optional.of(parent));
    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("extends_", "parent");

    entityTypeV3Mapper.toEntityType(entityType, valueMap);

    verify(entityType).setExtends(parent);
  }

  @Test
  void toEntityTypeI18n() {
    EntityType entityType = mock(EntityType.class);

    Map<String, Object> i18n = new HashMap<>();
    Map<String, String> translations = new HashMap<>();
    translations.put("nl", "nederlands");
    i18n.put("defaultValue", "default");
    i18n.put("translations", translations);
    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("label", i18n);
    valueMap.put("description", i18n);

    when(attributeV3Mapper.mapI18nValue(i18n)).thenReturn(
        I18nValue.builder().setDefaultValue("default")
            .setTranslations(singletonMap("nl", "nederlands")).build());

    entityTypeV3Mapper.toEntityType(entityType, valueMap);

    assertAll(
        () -> verify(entityType).setLabel("default"),
        () -> verify(entityType).setLabel("nl", "nederlands"),
        () -> verify(entityType).setDescription("default"),
        () -> verify(entityType).setDescription("nl", "nederlands"));
  }

  @Test
  void toEntityTypeAttrs() {
    EntityType entityType = mock(EntityType.class);

    Map<String, Object> valueMap = new HashMap<>();
    Map<String,Object> attrMap1 = new HashMap<>();
    Map<String,Object> attrMap2 = new HashMap<>();
    valueMap.put("attributes", Arrays.asList(attrMap1, attrMap2));

    Map<String, Attribute> attrs = new HashMap<>();
    Attribute attr1 = mock(Attribute.class);
    Attribute attr2 = mock(Attribute.class);
    attrs.put("1",attr1);
    attrs.put("2",attr2);
    when(attributeV3Mapper.toAttributes(Arrays.asList(attrMap1,attrMap2), entityType)).thenReturn(attrs);

    entityTypeV3Mapper.toEntityType(entityType, valueMap);

    verify(entityType).setOwnAllAttributes(attrs.values());
  }

  @Test
  void toEntityTypeSpecialAttrs() {
    EntityType entityType = mock(EntityType.class);

    Map<String, Object> valueMap = new HashMap<>();
    Map<String,Object> attrMap1 = new HashMap<>();
    Map<String,Object> attrMap2 = new HashMap<>();
    valueMap.put("attributes", Arrays.asList(attrMap1, attrMap2));
    valueMap.put("idAttribute", "id");
    valueMap.put("labelAttribute", "label");
    valueMap.put("lookupAttributes", Arrays.asList("lookup1", "lookup2"));

    Map<String, Attribute> attrs = new HashMap<>();
    Attribute attr1 = mock(Attribute.class);
    when(attr1.getIdentifier()).thenReturn("id");
    Attribute attr2 = mock(Attribute.class);
    when(attr2.getIdentifier()).thenReturn("label");
    Attribute attr3 = mock(Attribute.class);
    when(attr3.getIdentifier()).thenReturn("lookup1");
    Attribute attr4 = mock(Attribute.class);
    when(attr4.getIdentifier()).thenReturn("lookup2");
    Attribute attr5 = mock(Attribute.class);
    when(attr5.getIdentifier()).thenReturn("lookup3");
    attrs.put("id",attr1);
    attrs.put("label",attr2);
    attrs.put("lookup1",attr3);
    attrs.put("lookup2",attr4);
    attrs.put("lookup3",attr5);
    when(attributeV3Mapper.toAttributes(Arrays.asList(attrMap1,attrMap2), entityType)).thenReturn(attrs);

    entityTypeV3Mapper.toEntityType(entityType, valueMap);

    assertAll(
        () -> verify(attr1).setIdAttribute(true),
        () -> verify(attr1).setLabelAttribute(false),
        () -> verify(attr1).setLookupAttributeIndex(null),
        () -> verify(attr2).setIdAttribute(false),
        () -> verify(attr2).setLabelAttribute(true),
        () -> verify(attr2).setLookupAttributeIndex(null),
        () -> verify(attr3).setIdAttribute(false),
        () -> verify(attr3).setLabelAttribute(false),
        () -> verify(attr3).setLookupAttributeIndex(0),
        () -> verify(attr4).setIdAttribute(false),
        () -> verify(attr4).setLabelAttribute(false),
        () -> verify(attr4).setLookupAttributeIndex(1),
        () -> verify(attr5).setIdAttribute(false),
        () -> verify(attr5).setLabelAttribute(false),
        () -> verify(attr5).setLookupAttributeIndex(null));
  }
}
