package org.molgenis.api.data.v3;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.mockito.Mock;
import org.molgenis.api.data.v3.EntityCollection.Page;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.QueryRule.Operator;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.api.model.Sort.Order.Direction;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityControllerTest extends AbstractMockitoTest {
  @Mock private DataServiceV3 dataServiceV3;
  @Mock private EntityMapper entityMapper;
  @Mock private MessageSource messageSource;
  private EntityController entityController;

  @BeforeMethod
  public void setUpBeforeMethod() {
    entityController = new EntityController(dataServiceV3, entityMapper);
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
    MessageSourceHolder.setMessageSource(messageSource);
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

  @Test
  public void testGetEntities() {
    String entityTypeId = "MyEntityTypeId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.FULL_SELECTION;
    Query query = new Query().addRule("field", Operator.EQUALS, Collections.singletonList("value"));
    Sort sort = Sort.create("field", Direction.ASC);

    EntitiesRequest entityRequest = new EntitiesRequest();
    entityRequest.setEntityTypeId(entityTypeId);
    entityRequest.setQ(query);
    entityRequest.setSort(sort);
    entityRequest.setFilter(filter);
    entityRequest.setExpand(expand);
    entityRequest.setSize(10);
    entityRequest.setNumber(2);

    List<Entity> entities = new ArrayList<>();
    when(dataServiceV3.find(entityTypeId, query, sort, filter, expand, 10, 2)).thenReturn(entities);
    when(dataServiceV3.count(entityTypeId, query)).thenReturn(30);

    EntityCollection entityCollection =
        EntityCollection.builder()
            .setEntityTypeId(entityTypeId)
            .setEntities(entities)
            .setPage(Page.builder().setOffset(20).setPageSize(10).setTotal(30).build())
            .build();

    EntitiesResponse entitiesResponse = mock(EntitiesResponse.class);
    when(entityMapper.map(entityCollection, filter, expand)).thenReturn(entitiesResponse);

    assertEquals(entityController.getEntities(entityRequest), entitiesResponse);
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

  @Test
  public void testDeleteEntities() {
    String entityTypeId = "MyEntityTypeId";

    Query query = new Query();
    query.addRule("test", Operator.EQUALS, Collections.singletonList("value"));

    DeleteEntitiesRequest deleteEntitiesRequest = new DeleteEntitiesRequest();
    deleteEntitiesRequest.setEntityTypeId(entityTypeId);
    deleteEntitiesRequest.setQ(query);

    entityController.deleteEntities(deleteEntitiesRequest);
    QueryImpl molgenisQuery = new QueryImpl<>();
    molgenisQuery.eq("test", "value");
    verify(dataServiceV3).delete(entityTypeId, query);
  }
}
