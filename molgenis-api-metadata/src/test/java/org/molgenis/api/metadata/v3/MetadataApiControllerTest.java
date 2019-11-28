package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.api.model.Query.Operator.IN;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.DeleteAttributesRequest;
import org.molgenis.api.metadata.v3.model.DeleteEntityTypesRequest;
import org.molgenis.api.metadata.v3.model.EntityTypeResponse;
import org.molgenis.api.metadata.v3.model.EntityTypesResponse;
import org.molgenis.api.metadata.v3.model.ReadAttributesRequest;
import org.molgenis.api.metadata.v3.model.ReadEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.ReadEntityTypesRequest;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Sort;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.i18n.MessageSourceHolder;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class MetadataApiControllerTest extends AbstractMockitoTest {

  @Mock private MetadataApiService metadataApiService;
  @Mock private EntityTypeV3Mapper entityTypeV3Mapper;
  @Mock private AttributeV3Mapper attributeV3Mapper;
  @Mock private MessageSource messageSource;
  private MetadataApiController metadataApiController;

  @BeforeEach
  void setUpBeforeEach() {
    metadataApiController =
        new MetadataApiController(metadataApiService, entityTypeV3Mapper, attributeV3Mapper);
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
    MessageSourceHolder.setMessageSource(messageSource);
  }

  @Test
  void testMetadataApiController() {
    assertThrows(
        NullPointerException.class, () -> new MetadataApiController(null, null, attributeV3Mapper));
  }

  @Test
  void testGetEntityTypes() {
    int page = 2;
    int size = 1;
    Sort sort = mock(Sort.class);
    Query query = mock(Query.class);

    ReadEntityTypesRequest readEntityTypesRequest = new ReadEntityTypesRequest();
    readEntityTypesRequest.setPage(page);
    readEntityTypesRequest.setSize(size);
    readEntityTypesRequest.setSort(sort);
    readEntityTypesRequest.setQ(query);

    int total = 3;
    EntityTypes entityTypes = when(mock(EntityTypes.class).getTotal()).thenReturn(total).getMock();
    when(metadataApiService.findEntityTypes(query, sort, size, page)).thenReturn(entityTypes);

    EntityTypesResponse entityTypesResponse = mock(EntityTypesResponse.class);
    when(entityTypeV3Mapper.toEntityTypesResponse(entityTypes, size, page, total))
        .thenReturn(entityTypesResponse);

    assertEquals(entityTypesResponse, metadataApiController.getEntityTypes(readEntityTypesRequest));
  }

  @Test
  void testGetEntityType() {
    String entityTypeId = "MyEntityTypeId";
    boolean flattenAttrs = true;
    boolean i18n = false;

    ReadEntityTypeRequest readEntityTypeRequest = new ReadEntityTypeRequest();
    readEntityTypeRequest.setFlattenAttrs(flattenAttrs);
    readEntityTypeRequest.setI18n(i18n);

    EntityType entityType = mock(EntityType.class);
    when(metadataApiService.findEntityType(entityTypeId)).thenReturn(entityType);

    EntityTypeResponse entityTypeResponse = mock(EntityTypeResponse.class);
    when(entityTypeV3Mapper.toEntityTypeResponse(entityType, flattenAttrs, i18n))
        .thenReturn(entityTypeResponse);
    assertEquals(
        entityTypeResponse,
        metadataApiController.getEntityType(entityTypeId, readEntityTypeRequest));
  }

  @Test
  void testGetAttribute() {
    String attributeId = "MyAttributeId";
    String entityTypeId = "entityTypeId";

    Attribute attribute = mock(Attribute.class);
    when(metadataApiService.findAttribute(entityTypeId, attributeId)).thenReturn(attribute);

    AttributeResponse attributeResponse = mock(AttributeResponse.class);
    when(attributeV3Mapper.mapAttribute(attribute, false)).thenReturn(attributeResponse);
    assertEquals(attributeResponse, metadataApiController.getAttribute(entityTypeId, attributeId));
  }

  @Test
  void testCreateEntityType() throws URISyntaxException {
    CreateEntityTypeRequest createEntityTypeRequest = mock(CreateEntityTypeRequest.class);
    String entityTypeId = "MyEntityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(entityTypeV3Mapper.toEntityType(createEntityTypeRequest)).thenReturn(entityType);

    ResponseEntity responseEntity =
        ResponseEntity.created(new URI("http://localhost/api/metadata/" + entityTypeId)).build();
    assertEquals(responseEntity, metadataApiController.createEntityType(createEntityTypeRequest));
    verify(metadataApiService).createEntityType(entityType);
  }

  @Test
  void testGetAttributes() {
    String entityTypeId = "MyEntityTypeId";
    Query query = mock(Query.class);
    Sort sort = mock(Sort.class);
    int size = 1;
    int page = 2;

    ReadAttributesRequest readAttributesRequest = new ReadAttributesRequest();
    readAttributesRequest.setQ(query);
    readAttributesRequest.setSort(sort);
    readAttributesRequest.setSize(size);
    readAttributesRequest.setPage(page);

    int total = 3;
    Attributes attributes = when(mock(Attributes.class).getTotal()).thenReturn(total).getMock();
    when(metadataApiService.findAttributes(entityTypeId, query, sort, size, page))
        .thenReturn(attributes);

    AttributesResponse attributesResponse = mock(AttributesResponse.class);
    when(attributeV3Mapper.mapAttributes(attributes, size, page, total))
        .thenReturn(attributesResponse);

    assertEquals(
        attributesResponse,
        metadataApiController.getAttributes(entityTypeId, readAttributesRequest));
  }

  @Test
  void testDeleteAttribute() {
    String entityTypeId = "MyEntityTypeId";
    String attributeId = "attrId";
    MetadataUpsertJobExecution jobExecution = mockUpsertJobExecution();
    when(metadataApiService.deleteAttributeAsync(entityTypeId, attributeId))
        .thenReturn(jobExecution);

    metadataApiController.deleteAttribute(entityTypeId, attributeId);

    verify(metadataApiService).deleteAttributeAsync(entityTypeId, attributeId);
  }

  @Test
  void testDeleteAttributes() {
    String entityTypeId = "MyEntityTypeId";
    Query query = Query.create("name", IN, asList("name1", "name2"));
    DeleteAttributesRequest deleteAttributesRequest = new DeleteAttributesRequest();
    deleteAttributesRequest.setQ(query);
    MetadataUpsertJobExecution jobExecution = mockUpsertJobExecution();
    when(metadataApiService.deleteAttributesAsync(entityTypeId, query)).thenReturn(jobExecution);

    metadataApiController.deleteAttributes(entityTypeId, deleteAttributesRequest);

    verify(metadataApiService).deleteAttributesAsync(entityTypeId, query);
  }

  @Test
  void testUpdateEntityType() throws URISyntaxException {
    String entityTypeId = "MyEntityTypeId";
    CreateEntityTypeRequest createEntityTypeRequest = mock(CreateEntityTypeRequest.class);

    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(entityTypeV3Mapper.toEntityType(createEntityTypeRequest)).thenReturn(entityType);

    MetadataUpsertJobExecution metadataUpsertJobExecution = mock(MetadataUpsertJobExecution.class);
    when(metadataUpsertJobExecution.getEntityType()).thenReturn(entityType);

    when(metadataApiService.updateEntityTypeAsync(entityType))
        .thenReturn(metadataUpsertJobExecution);

    ResponseEntity responseEntity =
        ResponseEntity.accepted()
            .location(new URI("http://localhost/api/data/MyEntityTypeId"))
            .build();
    assertEquals(
        responseEntity,
        metadataApiController.updateEntityType(entityTypeId, createEntityTypeRequest));
  }

  @Test
  void testUpdateAttribute() throws URISyntaxException {
    String entityTypeId = "MyEntityTypeId";
    String attibuteId = "myAttributeId";
    CreateAttributeRequest createAttributeRequest =
        CreateAttributeRequest.builder().setId(attibuteId).setName("updatedMyAttribute").build();

    EntityType entityType = mock(EntityType.class);
    Attribute currentAttribute =
        when(mock(Attribute.class).getIdentifier()).thenReturn(attibuteId).getMock();
    when(entityType.getOwnAllAttributes()).thenReturn(singletonList(currentAttribute));
    when(entityType.getOwnAttributeById(attibuteId)).thenReturn(currentAttribute);
    when(metadataApiService.findEntityType(entityTypeId)).thenReturn(entityType);

    Attribute newAttribute =
        when(mock(Attribute.class).getIdentifier()).thenReturn(attibuteId).getMock();
    when(attributeV3Mapper.toAttribute(createAttributeRequest, entityType))
        .thenReturn(newAttribute);

    EntityType jobEntityType = mock(EntityType.class);
    when(jobEntityType.getId()).thenReturn("MyJobEntityTypeId");
    MetadataUpsertJobExecution metadataUpsertJobExecution = mock(MetadataUpsertJobExecution.class);
    when(metadataUpsertJobExecution.getEntityType()).thenReturn(jobEntityType);
    when(metadataUpsertJobExecution.getIdentifier()).thenReturn("MyJobEntityId");
    when(metadataApiService.updateEntityTypeAsync(entityType))
        .thenReturn(metadataUpsertJobExecution);

    ResponseEntity responseEntity =
        ResponseEntity.accepted()
            .location(new URI("http://localhost/api/data/MyJobEntityTypeId/MyJobEntityId"))
            .build();
    assertEquals(
        responseEntity,
        metadataApiController.updateAttribute(entityTypeId, attibuteId, createAttributeRequest));
    verify(entityType).setOwnAllAttributes(singletonList(newAttribute));
  }

  @Test
  void testDeleteEntityType() {
    String entityTypeId = "MyEntityTypeId";
    MetadataDeleteJobExecution jobExecution = mockDeleteJobExecution();
    when(metadataApiService.deleteEntityTypeAsync(entityTypeId)).thenReturn(jobExecution);

    metadataApiController.deleteEntityType(entityTypeId);

    verify(metadataApiService).deleteEntityTypeAsync(entityTypeId);
  }

  @Test
  void testDeleteEntityTypes() {
    Query query = Query.create("id", IN, asList("MyEntityTypeId0", "MyEntityTypeId1"));
    DeleteEntityTypesRequest deleteEntityTypesRequest = new DeleteEntityTypesRequest();
    deleteEntityTypesRequest.setQ(query);
    MetadataDeleteJobExecution jobExecution = mockDeleteJobExecution();
    when(metadataApiService.deleteEntityTypesAsync(query)).thenReturn(jobExecution);

    metadataApiController.deleteEntityTypes(deleteEntityTypesRequest);

    verify(metadataApiService).deleteEntityTypesAsync(query);
  }

  private static MetadataDeleteJobExecution mockDeleteJobExecution() {
    EntityType entityType = mock(EntityType.class);
    MetadataDeleteJobExecution jobExecution = mock(MetadataDeleteJobExecution.class);
    when(jobExecution.getEntityType()).thenReturn(entityType);
    return jobExecution;
  }

  private static MetadataUpsertJobExecution mockUpsertJobExecution() {
    EntityType entityType = mock(EntityType.class);
    MetadataUpsertJobExecution jobExecution = mock(MetadataUpsertJobExecution.class);
    when(jobExecution.getEntityType()).thenReturn(entityType);
    return jobExecution;
  }
}
