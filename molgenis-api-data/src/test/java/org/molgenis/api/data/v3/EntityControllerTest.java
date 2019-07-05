package org.molgenis.api.data.v3;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.mockito.Mock;
import org.molgenis.api.model.Selection;
import org.molgenis.data.Entity;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityControllerTest extends AbstractMockitoTest {
  @Mock private DataServiceV3 dataServiceV3;
  @Mock private EntityMapper entityMapper;
  private EntityController entityController;

  @BeforeMethod
  public void setUpBeforeMethod() {
    entityController = new EntityController(dataServiceV3, entityMapper);
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCreateEntity() throws URISyntaxException {
    String entityTypeId = "MyEntityTypeId";
    String entityId = "MyId";
    Map<String, Object> entityMap = mock(Map.class);
    Entity entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn(entityId);
    when(dataServiceV3.create(entityTypeId, entityMap)).thenReturn(entity);

    ResponseEntity responseEntity = entityController.createEntity(entityTypeId, entityMap);
    ResponseEntity<Object> expectedResponseEntity =
        ResponseEntity.created(new URI("http://localhost/api/entity/MyEntityTypeId/MyId")).build();
    assertEquals(responseEntity, expectedResponseEntity);
  }

  @Test
  public void testGetEntity() {
    String entityTypeId = "MyEntityTypeId";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.FULL_SELECTION;

    EntityRequest entityRequest = new EntityRequest();
    entityRequest.setEntityTypeId(entityTypeId);
    entityRequest.setEntityId(entityId);
    entityRequest.setFilter(filter);
    entityRequest.setExpand(expand);

    Entity entity = mock(Entity.class);
    when(dataServiceV3.find(entityTypeId, entityId, filter, expand)).thenReturn(entity);

    EntityResponse entityResponse = mock(EntityResponse.class);
    when(entityMapper.map(entity, filter, expand)).thenReturn(entityResponse);
    assertEquals(entityController.getEntity(entityRequest), entityResponse);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testUpdateEntity() {
    String entityTypeId = "MyEntityTypeId";
    String entityId = "MyId";

    Map<String, Object> requestValueMap = mock(Map.class);
    entityController.updateEntity(entityTypeId, entityId, requestValueMap);

    verify(dataServiceV3).update(entityTypeId, entityId, requestValueMap);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testUpdatePartialEntity() {
    String entityTypeId = "MyEntityTypeId";
    String entityId = "MyId";

    Map<String, Object> requestValueMap = mock(Map.class);
    entityController.updatePartialEntity(entityTypeId, entityId, requestValueMap);

    verify(dataServiceV3).updatePartial(entityTypeId, entityId, requestValueMap);
  }

  @Test
  public void testDeleteEntity() {
    String entityTypeId = "MyEntityTypeId";
    String entityId = "MyId";

    DeleteEntityRequest deleteEntityRequest = new DeleteEntityRequest();
    deleteEntityRequest.setEntityTypeId(entityTypeId);
    deleteEntityRequest.setEntityId(entityId);

    entityController.deleteEntity(deleteEntityRequest);

    verify(dataServiceV3).delete(entityTypeId, entityId);
  }
}
