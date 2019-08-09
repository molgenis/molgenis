package org.molgenis.api.data.v3;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Query.Operator;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.api.model.Sort.Order.Direction;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataServiceV3ImplTest extends AbstractMockitoTest {
  @Mock private MetaDataService metaDataService;
  @Mock private EntityManagerV3 entityServiceV3;
  @Mock private QueryV3Mapper queryMapperV3;
  @Mock private FetchMapper fetchMapper;
  private DataServiceV3Impl dataServiceV3Impl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    dataServiceV3Impl =
        new DataServiceV3Impl(metaDataService, entityServiceV3, queryMapperV3, fetchMapper);
  }

  // TODO implement
  @Test
  public void testCreate() {
    // FIXME: implement this test
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFind() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(STRING);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("id");
    when(fetchMapper.toFetch(entityType, filter, expand)).thenReturn(fetch);

    Entity entity = mock(Entity.class);
    when(repository.findOneById(entityId, fetch)).thenReturn(entity);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    assertEquals(dataServiceV3Impl.find(entityTypeId, entityId, filter, expand), entity);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFindExpand() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.FULL_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(STRING);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("id").field("refAttr");
    when(fetchMapper.toFetch(entityType, filter, expand)).thenReturn(fetch);

    Entity entity = mock(Entity.class);
    when(repository.findOneById(entityId, fetch)).thenReturn(entity);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    assertEquals(dataServiceV3Impl.find(entityTypeId, entityId, filter, expand), entity);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = UnknownEntityException.class)
  public void testFindUnknownEntity() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(STRING);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    dataServiceV3Impl.find(entityTypeId, entityId, filter, expand);
  }

  @Test(expectedExceptions = UnknownRepositoryException.class)
  public void testFindUnknownRepository() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());

    dataServiceV3Impl.find(entityTypeId, entityId, filter, expand);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFindAll() {
    String entityTypeId = "MyEntityType";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);

    Entity entity1 = mock(Entity.class);
    Entity entity2 = mock(Entity.class);

    Query q = Query.builder().setOperator(Operator.MATCHES).setValue("value").build();
    org.molgenis.data.Query<Entity> findAllQuery = mock(org.molgenis.data.Query.class);
    when(repository.findAll(findAllQuery)).thenReturn(Stream.of(entity1, entity2));
    org.molgenis.data.Query<Entity> countQuery = mock(org.molgenis.data.Query.class);
    when(repository.count(countQuery)).thenReturn(100L);
    when(queryMapperV3.map(q, repository)).thenReturn(findAllQuery).thenReturn(countQuery);
    Sort sort = Sort.create("field", Direction.ASC);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    Entities actual = dataServiceV3Impl.findAll(entityTypeId, q, filter, expand, sort, 10, 1);

    assertEquals(
        actual, Entities.builder().setEntities(asList(entity1, entity2)).setTotal(100).build());
  }

  @Test(expectedExceptions = UnknownRepositoryException.class)
  public void testFindAllUnknownRepositoryUnknownEntity() {
    String entityTypeId = "MyEntityType";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());

    dataServiceV3Impl.findAll(entityTypeId, null, filter, expand, Sort.EMPTY_SORT, 1, 1);
  }

  // TODO implement
  @Test
  public void testUpdate() {
    // FIXME: implement this test
  }

  // TODO implement
  @Test
  public void testUpdatePartially() {
    // FIXME: implement this test
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDelete() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(STRING);
    when(idAttribute.getName()).thenReturn("id");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    Entity entity = mock(Entity.class);
    when(repository.findOneById(entityId, new Fetch().field("id"))).thenReturn(entity);
    dataServiceV3Impl.delete(entityTypeId, entityId);

    verify(repository).deleteById(entityId);
  }

  @Test(expectedExceptions = UnknownRepositoryException.class)
  public void testDeleteUnknownEntityType() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());

    dataServiceV3Impl.delete(entityTypeId, entityId);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = UnknownEntityException.class)
  public void testDeleteUnknownEntity() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(STRING);
    when(idAttribute.getName()).thenReturn("id");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    dataServiceV3Impl.delete(entityTypeId, entityId);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeleteAll() {
    String entityTypeId = "MyEntityType";

    Repository<Entity> repository = mock(Repository.class);
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    Entity entity1 = mock(Entity.class);
    Entity entity2 = mock(Entity.class);

    Query query = Query.builder().setOperator(Operator.MATCHES).setValue("value").build();
    org.molgenis.data.Query<Entity> molgenisQuery = mock(org.molgenis.data.Query.class);
    when(queryMapperV3.map(query, repository)).thenReturn(molgenisQuery);

    dataServiceV3Impl.deleteAll(entityTypeId, query);

    ArgumentCaptor<Stream> captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).delete(captor.capture());
    Stream<Entity> entityStream = captor.getValue();
    entityStream.collect(Collectors.toList()).containsAll(Arrays.asList(entity1, entity2));
  }

  @Test(expectedExceptions = UnknownRepositoryException.class)
  public void testDeleteAllUnknownEntityType() {
    String entityTypeId = "MyEntityType";

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());

    dataServiceV3Impl.deleteAll(entityTypeId, null);
  }
}
