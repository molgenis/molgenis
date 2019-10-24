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
  void testDeleteEntityType() {
    String entityTypeId = "MyEntityTypeId";
    EntityType entityType = mock(EntityType.class);
    MetadataDeleteJobExecution jobExecution = mock(MetadataDeleteJobExecution.class);
    when(jobExecution.getEntityType()).thenReturn(entityType);
    when(metadataApiJobService.scheduleDelete(entityTypeId)).thenReturn(jobExecution);

    metadataApiController.deleteEntityType(entityTypeId);

    verify(metadataApiJobService).scheduleDelete(entityTypeId);
  }

  @Test
  void testDeleteEntityTypes() {
    Query query = Query.create("id", IN, asList("MyEntityTypeId0", "MyEntityTypeId1"));
    DeleteEntityTypesRequest deleteEntityTypesRequest = new DeleteEntityTypesRequest();
    deleteEntityTypesRequest.setQ(query);
    EntityType entityType = mock(EntityType.class);
    MetadataDeleteJobExecution jobExecution = mock(MetadataDeleteJobExecution.class);
    when(jobExecution.getEntityType()).thenReturn(entityType);
    when(metadataApiJobService.scheduleDelete(query)).thenReturn(jobExecution);

    metadataApiController.deleteEntityTypes(deleteEntityTypesRequest);

    verify(metadataApiJobService).scheduleDelete(query);
  }
}
