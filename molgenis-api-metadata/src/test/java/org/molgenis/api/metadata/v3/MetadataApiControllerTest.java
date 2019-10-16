package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.molgenis.api.model.Query.Operator.IN;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.metadata.v3.model.DeleteEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.DeleteEntityTypesRequest;
import org.molgenis.api.model.Query;
import org.molgenis.test.AbstractMockitoTest;

class MetadataApiControllerTest extends AbstractMockitoTest {
  @Mock private MetadataApiService metadataApiService;
  @Mock private EntityTypeV3Mapper entityTypeV3Mapper;
  @Mock private AttributeV3Mapper attributeV3Mapper;
  private MetadataApiController metadataApiController;

  @BeforeEach
  void setUpBeforeEach() {
    metadataApiController =
        new MetadataApiController(metadataApiService, null, entityTypeV3Mapper, attributeV3Mapper);
  }

  @Test
  void testMetadataApiController() {
    assertThrows(
        NullPointerException.class,
        () -> new MetadataApiController(null, null, null, attributeV3Mapper));
  }

  @Test
  void testDeleteEntityType() {
    DeleteEntityTypeRequest deleteEntityTypeRequest = new DeleteEntityTypeRequest();
    String entityTypeId = "MyEntityTypeId";
    deleteEntityTypeRequest.setEntityTypeId(entityTypeId);
    metadataApiController.deleteEntityType(deleteEntityTypeRequest);
    verify(metadataApiService).deleteEntityType(entityTypeId);
  }

  @Test
  void testDeleteEntityTypes() {
    Query query = Query.create("id", IN, asList("MyEntityTypeId0", "MyEntityTypeId1"));
    DeleteEntityTypesRequest deleteEntityTypesRequest = new DeleteEntityTypesRequest();
    deleteEntityTypesRequest.setQ(query);
    metadataApiController.deleteEntityTypes(deleteEntityTypesRequest);
    verify(metadataApiService).deleteEntityTypes(query);
  }
}
