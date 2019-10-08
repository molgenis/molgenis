package org.molgenis.data;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryImpl;

class DataServiceImplTest {
  private final List<String> entityTypeIds = asList("Entity1", "Entity2", "Entity3");
  private Repository<Entity> repo1;
  private Repository<Entity> repo2;
  private Repository<Entity> repoToRemove;
  private DataServiceImpl dataService;
  private MetaDataService metaDataService;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void beforeMethod() {
    dataService = new DataServiceImpl();
    repo1 = when(mock(Repository.class).getName()).thenReturn("Entity1").getMock();

    repo2 = mock(Repository.class);
    repo2 = when(mock(Repository.class).getName()).thenReturn("Entity2").getMock();

    repoToRemove = mock(Repository.class);
    repoToRemove = when(mock(Repository.class).getName()).thenReturn("Entity3").getMock();

    metaDataService = mock(MetaDataService.class);
    when(metaDataService.getRepository("Entity1")).thenReturn(of(repo1));
    when(metaDataService.getRepository("Entity2")).thenReturn(of(repo2));
    when(metaDataService.getRepository("Entity3")).thenReturn(of(repoToRemove));
    EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn("Entity1").getMock();
    EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn("Entity2").getMock();
    EntityType entityType3 = when(mock(EntityType.class).getId()).thenReturn("Entity3").getMock();

    when(metaDataService.getEntityTypes())
        .thenAnswer(invocation -> Stream.of(entityType1, entityType2, entityType3));
    dataService.setMetaDataService(metaDataService);
  }

  @Test
  void testHasEntityTypeTrue() {
    String entityTypeId = "MyEntityTypeId";
    when(metaDataService.hasEntityType(entityTypeId)).thenReturn(true);
    assertTrue(dataService.hasEntityType(entityTypeId));
  }

  @Test
  void testHasEntityTypeFalse() {
    String entityTypeId = "MyEntityTypeId";
    assertFalse(dataService.hasEntityType(entityTypeId));
  }

  @Test
  void testGetEntityType() {
    String entityTypeId = "MyEntityTypeId";
    EntityType entityType = mock(EntityType.class);
    when(metaDataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));

    assertEquals(entityType, dataService.getEntityType(entityTypeId));
  }

  @Test
  void testGetEntityTypeUnknownEntityType() {
    String entityTypeId = "MyEntityTypeId";
    when(metaDataService.getEntityType(entityTypeId)).thenReturn(Optional.empty());
    assertThrows(UnknownEntityTypeException.class, () -> dataService.getEntityType(entityTypeId));
  }

  @Test
  void addStream() {
    Stream<Entity> entities = Stream.empty();
    dataService.add("Entity1", entities);
    verify(repo1, times(1)).add(entities);
  }

  @Test
  void updateStream() {
    Stream<Entity> entities = Stream.empty();
    dataService.update("Entity1", entities);
    verify(repo1, times(1)).update(entities);
  }

  @Test
  void deleteStream() {
    Stream<Entity> entities = Stream.empty();
    dataService.delete("Entity1", entities);
    verify(repo1, times(1)).delete(entities);
  }

  @Test
  void deleteAllStream() {
    Stream<Object> entityIds = Stream.empty();
    dataService.deleteAll("Entity1", entityIds);
    verify(repo1, times(1)).deleteAll(entityIds);
  }

  @Test
  void getEntityNames() {
    assertEquals(
        asList("Entity1", "Entity2", "Entity3"), dataService.getEntityTypeIds().collect(toList()));
  }

  @Test
  void hasRepositoryTrue() {
    String entityTypeId = "MyEntityTypeId";
    when(metaDataService.hasRepository(entityTypeId)).thenReturn(true);
    assertTrue(dataService.hasRepository(entityTypeId));
  }

  @Test
  void hasRepositoryFalse() {
    String entityTypeId = "MyEntityTypeId";
    assertFalse(dataService.hasRepository(entityTypeId));
  }

  @Test
  void getRepository() {
    String entityTypeId = "MyEntityTypeId";
    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));
    assertEquals(repository, dataService.getRepository(entityTypeId));
  }

  @Test
  void getRepositoryUnknownEntityType() {
    String entityTypeId = "MyEntityTypeId";
    when(metaDataService.getRepository(entityTypeId))
        .thenThrow(new UnknownEntityTypeException(entityTypeId));
    assertThrows(UnknownEntityTypeException.class, () -> dataService.getRepository(entityTypeId));
  }

  @Test
  void getRepositoryUnknownRepository() {
    String entityTypeId = "MyEntityTypeId";
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());
    assertThrows(UnknownRepositoryException.class, () -> dataService.getRepository(entityTypeId));
  }

  @Test
  void findOneStringObjectFetch() {
    Object id = 0;
    Fetch fetch = new Fetch();
    Entity entity = mock(Entity.class);
    when(repo1.findOneById(id, fetch)).thenReturn(entity);
    assertEquals(entity, dataService.findOneById("Entity1", id, fetch));
    verify(repo1, times(1)).findOneById(id, fetch);
  }

  @Test
  void findOneStringObjectFetchEntityNull() {
    Object id = 0;
    Fetch fetch = new Fetch();
    when(repo1.findOneById(id, fetch)).thenReturn(null);
    assertNull(dataService.findOneById("Entity1", id, fetch));
    verify(repo1, times(1)).findOneById(id, fetch);
  }

  @Test
  void findOneStringObjectFetchClass() {
    Object id = 0;
    Fetch fetch = new Fetch();
    Class<Entity> clazz = Entity.class;
    Entity entity = mock(Entity.class);
    when(repo1.findOneById(id, fetch)).thenReturn(entity);
    when(metaDataService.getRepository("Entity1", clazz)).thenReturn(of(repo1));
    // how to check return value? converting iterable can't be mocked.
    dataService.findOneById("Entity1", id, fetch, clazz);
    verify(repo1, times(1)).findOneById(id, fetch);
  }

  @Test
  void findOneStringObjectFetchClassEntityNull() {
    Object id = 0;
    Fetch fetch = new Fetch();
    Class<Entity> clazz = Entity.class;
    when(repo1.findOneById(id, fetch)).thenReturn(null);
    when(metaDataService.getRepository("Entity1", clazz)).thenReturn(of(repo1));
    assertNull(dataService.findOneById("Entity1", id, fetch, clazz));
    verify(repo1, times(1)).findOneById(id, fetch);
  }

  @Test
  void findAllStringStream() {
    Object id0 = "id0";
    Stream<Object> ids = Stream.of(id0);
    Entity entity0 = mock(Entity.class);
    when(repo1.findAll(ids)).thenReturn(Stream.of(entity0));
    Stream<Entity> entities = dataService.findAll("Entity1", ids);
    assertEquals(singletonList(entity0), entities.collect(toList()));
  }

  @Test
  void findAllStringStreamClass() {
    Object id0 = "id0";
    Stream<Object> ids = Stream.of(id0);
    Entity entity0 = mock(Entity.class);
    Class<Entity> clazz = Entity.class;
    when(repo1.findAll(ids)).thenReturn(Stream.of(entity0));
    when(metaDataService.getRepository("Entity1", clazz)).thenReturn(of(repo1));
    Stream<Entity> entities = dataService.findAll("Entity1", ids, clazz);
    assertEquals(singletonList(entity0), entities.collect(toList()));
  }

  @Test
  void findAllStringStreamFetch() {
    Object id0 = "id0";
    Stream<Object> ids = Stream.of(id0);
    Entity entity0 = mock(Entity.class);
    Fetch fetch = new Fetch();
    when(repo1.findAll(ids, fetch)).thenReturn(Stream.of(entity0));
    Stream<Entity> entities = dataService.findAll("Entity1", ids, fetch);
    assertEquals(singletonList(entity0), entities.collect(toList()));
  }

  @Test
  void findAllStringStreamFetchClass() {
    Object id0 = "id0";
    Stream<Object> ids = Stream.of(id0);
    Entity entity0 = mock(Entity.class);
    Class<Entity> clazz = Entity.class;
    Fetch fetch = new Fetch();
    when(repo1.findAll(ids, fetch)).thenReturn(Stream.of(entity0));
    when(metaDataService.getRepository("Entity1", clazz)).thenReturn(of(repo1));
    Stream<Entity> entities = dataService.findAll("Entity1", ids, fetch, clazz);
    assertEquals(singletonList(entity0), entities.collect(toList()));
  }

  @Test
  void findAllStreamString() {
    Entity entity0 = mock(Entity.class);
    when(repo1.findAll(new QueryImpl<>())).thenReturn(Stream.of(entity0));
    Stream<Entity> entities = dataService.findAll("Entity1");
    assertEquals(singletonList(entity0), entities.collect(toList()));
  }

  @Test
  void findAllStreamStringClass() {
    Class<Entity> clazz = Entity.class;
    Entity entity0 = mock(Entity.class);
    when(repo1.findAll(new QueryImpl<>())).thenReturn(Stream.of(entity0));
    when(metaDataService.getRepository("Entity1", clazz)).thenReturn(of(repo1));
    Stream<Entity> entities = dataService.findAll("Entity1", clazz);
    assertEquals(singletonList(entity0), entities.collect(toList()));
  }

  @Test
  void findAllStreamStringQuery() {
    Entity entity0 = mock(Entity.class);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    when(repo1.findAll(query)).thenReturn(Stream.of(entity0));
    Stream<Entity> entities = dataService.findAll("Entity1", query);
    assertEquals(singletonList(entity0), entities.collect(toList()));
  }

  @Test
  void findAllStreamStringQueryClass() {
    Class<Entity> clazz = Entity.class;
    Entity entity0 = mock(Entity.class);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    when(repo1.findAll(query)).thenReturn(Stream.of(entity0));
    when(metaDataService.getRepository("Entity1", clazz)).thenReturn(of(repo1));
    Stream<Entity> entities = dataService.findAll("Entity1", query, clazz);
    assertEquals(singletonList(entity0), entities.collect(toList()));
  }
}
