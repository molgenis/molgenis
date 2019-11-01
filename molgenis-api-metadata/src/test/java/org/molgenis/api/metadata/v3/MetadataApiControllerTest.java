package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.api.model.Query.Operator.IN;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.model.DeleteAttributeRequest;
import org.molgenis.api.metadata.v3.model.DeleteAttributesRequest;
import org.molgenis.api.metadata.v3.model.DeleteEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.DeleteEntityTypesRequest;
import org.molgenis.api.model.Query;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.i18n.MessageSourceHolder;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class MetadataApiControllerTest extends AbstractMockitoTest {

  @Mock private MetadataApiService metadataApiService;
  @Mock private MetadataApiJobService metadataApiJobService;
  @Mock private EntityTypeV3Mapper entityTypeV3Mapper;
  @Mock private AttributeV3Mapper attributeV3Mapper;
  @Mock private MessageSource messageSource;
  private MetadataApiController metadataApiController;

  @BeforeEach
  void setUpBeforeEach() {
    metadataApiController =
        new MetadataApiController(
            metadataApiService, metadataApiJobService, entityTypeV3Mapper, attributeV3Mapper);
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
    MessageSourceHolder.setMessageSource(messageSource);
  }

  @Test
  void testMetadataApiController() {
    assertThrows(
        NullPointerException.class,
        () -> new MetadataApiController(null, null, null, attributeV3Mapper));
  }

  @Test
  void testDeleteAttribute() {
    String entityTypeId = "MyEntityTypeId";
    String attributeId = "attrId";
    DeleteAttributeRequest deleteAttributeRequest = new DeleteAttributeRequest();
    deleteAttributeRequest.setEntityTypeId(entityTypeId);
    deleteAttributeRequest.setAttributeId(attributeId);
    MetadataDeleteJobExecution jobExecution = mockJobExecution();
    when(metadataApiJobService.scheduleDeleteAttribute(entityTypeId, attributeId))
        .thenReturn(jobExecution);

    metadataApiController.deleteAttribute(deleteAttributeRequest);

    verify(metadataApiJobService).scheduleDeleteAttribute(entityTypeId, attributeId);
  }

  @Test
  void testDeleteAttributes() {
    String entityTypeId = "MyEntityTypeId";
    Query query = Query.create("name", IN, asList("name1", "name2"));
    DeleteAttributesRequest deleteAttributesRequest = new DeleteAttributesRequest();
    deleteAttributesRequest.setEntityTypeId(entityTypeId);
    deleteAttributesRequest.setQ(query);
    MetadataDeleteJobExecution jobExecution = mockJobExecution();
    when(metadataApiJobService.scheduleDeleteAttribute(entityTypeId, query))
        .thenReturn(jobExecution);

    metadataApiController.deleteAttributes(deleteAttributesRequest);

    verify(metadataApiJobService).scheduleDeleteAttribute(entityTypeId, query);
  }

  @Test
  void testDeleteEntityType() {
    String entityTypeId = "MyEntityTypeId";
    DeleteEntityTypeRequest deleteEntityTypeRequest = new DeleteEntityTypeRequest();
    deleteEntityTypeRequest.setEntityTypeId(entityTypeId);
    MetadataDeleteJobExecution jobExecution = mockJobExecution();
    when(metadataApiJobService.scheduleDeleteEntityType(entityTypeId)).thenReturn(jobExecution);

    metadataApiController.deleteEntityType(deleteEntityTypeRequest);

    verify(metadataApiJobService).scheduleDeleteEntityType(entityTypeId);
  }

  @Test
  void testDeleteEntityTypes() {
    Query query = Query.create("id", IN, asList("MyEntityTypeId0", "MyEntityTypeId1"));
    DeleteEntityTypesRequest deleteEntityTypesRequest = new DeleteEntityTypesRequest();
    deleteEntityTypesRequest.setQ(query);
    MetadataDeleteJobExecution jobExecution = mockJobExecution();
    when(metadataApiJobService.scheduleDeleteEntityType(query)).thenReturn(jobExecution);

    metadataApiController.deleteEntityTypes(deleteEntityTypesRequest);

    verify(metadataApiJobService).scheduleDeleteEntityType(query);
  }

  private static MetadataDeleteJobExecution mockJobExecution() {
    EntityType entityType = mock(EntityType.class);
    MetadataDeleteJobExecution jobExecution = mock(MetadataDeleteJobExecution.class);
    when(jobExecution.getEntityType()).thenReturn(entityType);
    return jobExecution;
  }
}
