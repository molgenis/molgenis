package org.molgenis.api.data.v3;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.mockito.Mock;
import org.molgenis.api.data.v3.EntityCollection.Page;
import org.molgenis.api.data.v3.model.DeleteEntitiesRequest;
import org.molgenis.api.data.v3.model.DeleteEntityRequest;
import org.molgenis.api.data.v3.model.EntitiesResponse;
import org.molgenis.api.data.v3.model.EntityResponse;
import org.molgenis.api.data.v3.model.ReadEntitiesRequest;
import org.molgenis.api.data.v3.model.ReadEntityRequest;
import org.molgenis.api.data.v3.model.ReadSubresourceRequest;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Query.Operator;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.api.model.Sort.Order.Direction;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.i18n.MessageSourceHolder;
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
        ResponseEntity.created(new URI("http://localhost/api/data/MyEntityTypeId/MyId")).build();
    assertEquals(responseEntity, expectedResponseEntity);
  }

  @Test
  public void testGetEntity() {
    String entityTypeId = "MyEntityTypeId";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.FULL_SELECTION;

    ReadEntityRequest entityRequest = new ReadEntityRequest();
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
    Query query = Query.builder().setOperator(Operator.MATCHES).setValue("value").build();
    Sort sort = Sort.create("field", Direction.ASC);

    ReadEntitiesRequest entityRequest = new ReadEntitiesRequest();
    entityRequest.setEntityTypeId(entityTypeId);
    entityRequest.setQ(query);
    entityRequest.setSort(sort);
    entityRequest.setFilter(filter);
    entityRequest.setExpand(expand);
    entityRequest.setSize(10);
    entityRequest.setPage(2);

    Entities entities = Entities.create(emptyList(), 30);
    when(dataServiceV3.findAll(entityTypeId, query, filter, expand, sort, 10, 2))
        .thenReturn(entities);

    EntityCollection entityCollection =
        EntityCollection.builder()
            .setEntityTypeId(entityTypeId)
            .setEntities(emptyList())
            .setPage(Page.builder().setOffset(20).setPageSize(10).setTotal(30).build())
            .build();

    EntitiesResponse entitiesResponse = mock(EntitiesResponse.class);
    when(entityMapper.map(entityCollection, filter, expand, Optional.of(query), sort, 10, 2, 30))
        .thenReturn(entitiesResponse);

    assertEquals(entityController.getEntities(entityRequest), entitiesResponse);
  }

  @Test
  public void testGetField() {
    String entityTypeId = "MyEntityTypeId";
    String entityId = "EntityId";
    String fieldId = "Field";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.FULL_SELECTION;
    Query query = Query.builder().setOperator(Operator.MATCHES).setValue("value").build();
    Sort sort = Sort.create("field", Direction.ASC);

    ReadSubresourceRequest readSubResourceRequest = new ReadSubresourceRequest();
    readSubResourceRequest.setEntityTypeId(entityTypeId);
    readSubResourceRequest.setEntityId(entityId);
    readSubResourceRequest.setFieldId(fieldId);
    readSubResourceRequest.setQ(query);
    readSubResourceRequest.setSort(sort);
    readSubResourceRequest.setFilter(filter);
    readSubResourceRequest.setExpand(expand);
    readSubResourceRequest.setSize(10);
    readSubResourceRequest.setPage(2);

    Entities entities = Entities.create(emptyList(), 30);
    when(dataServiceV3.findSubresources(
            entityTypeId, entityId, fieldId, query, filter, expand, sort, 10, 2))
        .thenReturn(entities);

    EntityCollection entityCollection =
        EntityCollection.builder()
            .setEntityTypeId(entityTypeId)
            .setEntities(emptyList())
            .setPage(Page.builder().setOffset(20).setPageSize(10).setTotal(30).build())
            .build();

    EntitiesResponse entitiesResponse = mock(EntitiesResponse.class);
    when(entityMapper.map(
            "MyEntityTypeId",
            "EntityId",
            "Field",
            entityCollection,
            filter,
            expand,
            Optional.of(query),
            sort,
            10,
            2,
            30))
        .thenReturn(entitiesResponse);

    assertEquals(entityController.getReferencedEntities(readSubResourceRequest), entitiesResponse);
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

    Query query =
        Query.builder().setItem("test").setOperator(Operator.MATCHES).setValue("value").build();

    DeleteEntitiesRequest deleteEntitiesRequest = new DeleteEntitiesRequest();
    deleteEntitiesRequest.setEntityTypeId(entityTypeId);
    deleteEntitiesRequest.setQ(query);

    entityController.deleteEntities(deleteEntitiesRequest);
    QueryImpl molgenisQuery = new QueryImpl<>();
    molgenisQuery.eq("test", "value");
    verify(dataServiceV3).deleteAll(entityTypeId, query);
  }
}
