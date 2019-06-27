package org.molgenis.api.data.v3;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityControllerTest extends AbstractMockitoTest {
  @Mock private DataServiceV3 dataServiceV3;
  @Mock private EntityMapper entityMapper;
  private EntityController entityController;

  @BeforeMethod
  public void setUpBeforeMethod() {
    entityController = new EntityController(dataServiceV3, entityMapper);
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
