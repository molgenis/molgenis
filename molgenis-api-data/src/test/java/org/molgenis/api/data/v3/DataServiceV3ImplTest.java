package org.molgenis.api.data.v3;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.api.model.Sort.Order.Direction;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.AttributeType;
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
  private DataServiceV3Impl dataServiceV3Impl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    dataServiceV3Impl = new DataServiceV3Impl(metaDataService, entityServiceV3, queryMapperV3);
  }

  // TODO implement
  @Test
  public void testCreate() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFind() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getName()).thenReturn("id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getAtomicAttributes()).thenReturn(Collections.singletonList(idAttribute));

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("id");

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
    when(idAttribute.getName()).thenReturn("id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    EntityType refEntityType = mock(EntityType.class);

    Attribute refAttribute = mock(Attribute.class);
    when(refAttribute.getName()).thenReturn("refAttr");
    when(refAttribute.getDataType()).thenReturn(AttributeType.XREF);
    when(refAttribute.getRefEntity()).thenReturn(refEntityType);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getAtomicAttributes()).thenReturn(asList(idAttribute, refAttribute));

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("id").field("refAttr");

    Entity entity = mock(Entity.class);
    when(repository.findOneById(entityId, fetch)).thenReturn(entity);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    assertEquals(dataServiceV3Impl.find(entityTypeId, entityId, filter, expand), entity);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFindMulti() {
    String entityTypeId = "MyEntityType";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getName()).thenReturn("id");

    Attribute refAttribute = mock(Attribute.class);
    when(refAttribute.getName()).thenReturn("refAttr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAtomicAttributes()).thenReturn(asList(idAttribute, refAttribute));

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);

    Entity entity1 = mock(Entity.class);
    Entity entity2 = mock(Entity.class);

    org.molgenis.api.model.Query q = mock(org.molgenis.api.model.Query.class);
    Query molgenisQuery = mock(Query.class);
    when(molgenisQuery.findAll()).thenReturn(Stream.of(entity1, entity2));
    when(queryMapperV3.map(q, repository)).thenReturn(molgenisQuery);
    Sort sort = Sort.create("field", Direction.ASC);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    List<Entity> actual = dataServiceV3Impl.find(entityTypeId, q, sort, filter, expand, 10, 1);

    verify(molgenisQuery).offset(10);
    verify(molgenisQuery).pageSize(10);
    assertTrue(actual.containsAll(Arrays.asList(entity1, entity2)));
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = UnknownEntityTypeException.class)
  public void testFindMultiUnknownEntity() {
    String entityTypeId = "MyEntityType";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Entity entity1 = mock(Entity.class);
    Entity entity2 = mock(Entity.class);

    org.molgenis.api.model.Query q = mock(org.molgenis.api.model.Query.class);
    Query molgenisQuery = mock(Query.class);
    Sort sort = Sort.create("field", Direction.ASC);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());

    List<Entity> actual = dataServiceV3Impl.find(entityTypeId, q, sort, filter, expand, 10, 1);

    verify(molgenisQuery).offset(10);
    verify(molgenisQuery).pageSize(10);
    assertTrue(actual.containsAll(Arrays.asList(entity1, entity2)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFindMultiExpand() {
    String entityTypeId = "MyEntityType";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.FULL_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getName()).thenReturn("id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    EntityType refEntityType = mock(EntityType.class);

    Attribute refAttribute = mock(Attribute.class);
    when(refAttribute.getName()).thenReturn("refAttr");
    when(refAttribute.getDataType()).thenReturn(AttributeType.XREF);
    when(refAttribute.getRefEntity()).thenReturn(refEntityType);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAtomicAttributes()).thenReturn(asList(idAttribute, refAttribute));

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("id").field("refAttr");

    Entity entity1 = mock(Entity.class);
    Entity entity2 = mock(Entity.class);

    org.molgenis.api.model.Query q = mock(org.molgenis.api.model.Query.class);
    Query molgenisQuery = mock(Query.class);
    when(molgenisQuery.findAll()).thenReturn(Stream.of(entity1, entity2));
    when(queryMapperV3.map(q, repository)).thenReturn(molgenisQuery);
    Sort sort = Sort.create("field", Direction.ASC);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    List<Entity> actual = dataServiceV3Impl.find(entityTypeId, q, sort, filter, expand, 10, 1);

    verify(molgenisQuery).fetch(fetch);
    verify(molgenisQuery).offset(10);
    verify(molgenisQuery).pageSize(10);
    assertTrue(actual.containsAll(Arrays.asList(entity1, entity2)));
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
  @Test(expectedExceptions = UnknownEntityException.class)
  public void testFindUnknownEntity() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getName()).thenReturn("id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getAtomicAttributes()).thenReturn(Collections.singletonList(idAttribute));

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    dataServiceV3Impl.find(entityTypeId, entityId, filter, expand);
  }

  // TODO implement
  @Test
  public void testUpdate() {
    throw new UnsupportedOperationException();
  }

  // TODO implement
  @Test
  public void testUpdatePartially() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeleteById() {
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

  @SuppressWarnings("unchecked")
  @Test
  public void testDeleteByQuery() {
    String entityTypeId = "MyEntityType";

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(STRING);
    when(idAttribute.getName()).thenReturn("id");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    Entity entity1 = mock(Entity.class);
    Entity entity2 = mock(Entity.class);

    org.molgenis.api.model.Query q = mock(org.molgenis.api.model.Query.class);
    Query molgenisQuery = mock(Query.class);
    when(molgenisQuery.findAll()).thenReturn(Stream.of(entity1, entity2));
    when(queryMapperV3.map(q, repository)).thenReturn(molgenisQuery);

    dataServiceV3Impl.delete(entityTypeId, q);

    ArgumentCaptor<Stream> captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).delete(captor.capture());
    Stream<Entity> entityStream = captor.getValue();
    entityStream.collect(Collectors.toList()).containsAll(Arrays.asList(entity1, entity2));
  }

  @Test(expectedExceptions = UnknownEntityTypeException.class)
  public void testDeleteByQueryUnknownEntityType() {
    String entityTypeId = "MyEntityType";

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(STRING);
    when(idAttribute.getName()).thenReturn("id");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());

    Entity entity1 = mock(Entity.class);
    Entity entity2 = mock(Entity.class);

    org.molgenis.api.model.Query q = mock(org.molgenis.api.model.Query.class);
    Query molgenisQuery = mock(Query.class);
    when(molgenisQuery.findAll()).thenReturn(Stream.of(entity1, entity2));

    dataServiceV3Impl.delete(entityTypeId, q);
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
}
