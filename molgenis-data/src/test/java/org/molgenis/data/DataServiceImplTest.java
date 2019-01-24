package org.molgenis.data;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataServiceImplTest {
  private final List<String> entityTypeIds = asList("Entity1", "Entity2", "Entity3");
  private Repository<Entity> repo1;
  private Repository<Entity> repo2;
  private Repository<Entity> repoToRemove;
  private DataServiceImpl dataService;
  private MetaDataService metaDataService;

  @SuppressWarnings("unchecked")
  @BeforeMethod
  public void beforeMethod() {
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
  public void testHasEntityTypeTrue() {
    String entityTypeId = "MyEntityTypeId";
    when(metaDataService.hasEntityType(entityTypeId)).thenReturn(true);
    assertTrue(dataService.hasEntityType(entityTypeId));
  }

  @Test
  public void testHasEntityTypeFalse() {
    String entityTypeId = "MyEntityTypeId";
    assertFalse(dataService.hasEntityType(entityTypeId));
  }

  @Test
  public void testGetEntityType() {
    String entityTypeId = "MyEntityTypeId";
    EntityType entityType = mock(EntityType.class);
    when(metaDataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));

    assertEquals(dataService.getEntityType(entityTypeId), entityType);
  }

  @Test(expectedExceptions = UnknownEntityTypeException.class)
  public void testGetEntityTypeUnknownEntityType() {
    String entityTypeId = "MyEntityTypeId";
    when(metaDataService.getEntityType(entityTypeId)).thenReturn(Optional.empty());
    dataService.getEntityType(entityTypeId);
  }

  @Test
  public void addStream() {
    Stream<Entity> entities = Stream.empty();
    dataService.add("Entity1", entities);
    verify(repo1, times(1)).add(entities);
  }

  @Test
  public void updateStream() {
    Stream<Entity> entities = Stream.empty();
    dataService.update("Entity1", entities);
    verify(repo1, times(1)).update(entities);
  }

  @Test
  public void deleteStream() {
    Stream<Entity> entities = Stream.empty();
    dataService.delete("Entity1", entities);
    verify(repo1, times(1)).delete(entities);
  }

  @Test
  public void deleteAllStream() {
    Stream<Object> entityIds = Stream.empty();
    dataService.deleteAll("Entity1", entityIds);
    verify(repo1, times(1)).deleteAll(entityIds);
  }

  @Test
  public void getEntityNames() {
    assertEquals(
        dataService.getEntityTypeIds().collect(toList()), asList("Entity1", "Entity2", "Entity3"));
  }

  @Test
  public void hasRepositoryTrue() {
    String entityTypeId = "MyEntityTypeId";
    when(metaDataService.hasRepository(entityTypeId)).thenReturn(true);
    assertTrue(dataService.hasRepository(entityTypeId));
  }

  @Test
  public void hasRepositoryFalse() {
    String entityTypeId = "MyEntityTypeId";
    assertFalse(dataService.hasRepository(entityTypeId));
  }

  @Test
  public void getRepository() {
    String entityTypeId = "MyEntityTypeId";
    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));
    assertEquals(dataService.getRepository(entityTypeId), repository);
  }

  @Test(expectedExceptions = UnknownEntityTypeException.class)
  public void getRepositoryUnknownEntityType() {
    String entityTypeId = "MyEntityTypeId";
    when(metaDataService.getRepository(entityTypeId))
        .thenThrow(new UnknownEntityTypeException(entityTypeId));
    dataService.getRepository(entityTypeId);
  }

  @Test(expectedExceptions = UnknownRepositoryException.class)
  public void getRepositoryUnknownRepository() {
    String entityTypeId = "MyEntityTypeId";
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());
    dataService.getRepository(entityTypeId);
  }

  @Test
  public void findOneStringObjectFetch() {
    Object id = 0;
    Fetch fetch = new Fetch();
    Entity entity = mock(Entity.class);
    when(repo1.findOneById(id, fetch)).thenReturn(entity);
    assertEquals(dataService.findOneById("Entity1", id, fetch), entity);
    verify(repo1, times(1)).findOneById(id, fetch);
  }

  @Test
  public void findOneStringObjectFetchEntityNull() {
    Object id = 0;
    Fetch fetch = new Fetch();
    when(repo1.findOneById(id, fetch)).thenReturn(null);
    assertNull(dataService.findOneById("Entity1", id, fetch));
    verify(repo1, times(1)).findOneById(id, fetch);
  }

  @Test
  public void findOneStringObjectFetchClass() {
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
  public void findOneStringObjectFetchClassEntityNull() {
    Object id = 0;
    Fetch fetch = new Fetch();
    Class<Entity> clazz = Entity.class;
    when(repo1.findOneById(id, fetch)).thenReturn(null);
    when(metaDataService.getRepository("Entity1", clazz)).thenReturn(of(repo1));
    assertNull(dataService.findOneById("Entity1", id, fetch, clazz));
    verify(repo1, times(1)).findOneById(id, fetch);
  }

  @Test
  public void findAllStringStream() {
    Object id0 = "id0";
    Stream<Object> ids = Stream.of(id0);
    Entity entity0 = mock(Entity.class);
    when(repo1.findAll(ids)).thenReturn(Stream.of(entity0));
    Stream<Entity> entities = dataService.findAll("Entity1", ids);
    assertEquals(entities.collect(toList()), singletonList(entity0));
  }

  @Test
  public void findAllStringStreamClass() {
    Object id0 = "id0";
    Stream<Object> ids = Stream.of(id0);
    Entity entity0 = mock(Entity.class);
    Class<Entity> clazz = Entity.class;
    when(repo1.findAll(ids)).thenReturn(Stream.of(entity0));
    when(metaDataService.getRepository("Entity1", clazz)).thenReturn(of(repo1));
    Stream<Entity> entities = dataService.findAll("Entity1", ids, clazz);
    assertEquals(entities.collect(toList()), singletonList(entity0));
  }

  @Test
  public void findAllStringStreamFetch() {
    Object id0 = "id0";
    Stream<Object> ids = Stream.of(id0);
    Entity entity0 = mock(Entity.class);
    Fetch fetch = new Fetch();
    when(repo1.findAll(ids, fetch)).thenReturn(Stream.of(entity0));
    Stream<Entity> entities = dataService.findAll("Entity1", ids, fetch);
    assertEquals(entities.collect(toList()), singletonList(entity0));
  }

  @Test
  public void findAllStringStreamFetchClass() {
    Object id0 = "id0";
    Stream<Object> ids = Stream.of(id0);
    Entity entity0 = mock(Entity.class);
    Class<Entity> clazz = Entity.class;
    Fetch fetch = new Fetch();
    when(repo1.findAll(ids, fetch)).thenReturn(Stream.of(entity0));
    when(metaDataService.getRepository("Entity1", clazz)).thenReturn(of(repo1));
    Stream<Entity> entities = dataService.findAll("Entity1", ids, fetch, clazz);
    assertEquals(entities.collect(toList()), singletonList(entity0));
  }

  @Test
  public void findAllStreamString() {
    Entity entity0 = mock(Entity.class);
    when(repo1.findAll(new QueryImpl<>())).thenReturn(Stream.of(entity0));
    Stream<Entity> entities = dataService.findAll("Entity1");
    assertEquals(entities.collect(toList()), singletonList(entity0));
  }

  @Test
  public void findAllStreamStringClass() {
    Class<Entity> clazz = Entity.class;
    Entity entity0 = mock(Entity.class);
    when(repo1.findAll(new QueryImpl<>())).thenReturn(Stream.of(entity0));
    when(metaDataService.getRepository("Entity1", clazz)).thenReturn(of(repo1));
    Stream<Entity> entities = dataService.findAll("Entity1", clazz);
    assertEquals(entities.collect(toList()), singletonList(entity0));
  }

  @Test
  public void findAllStreamStringQuery() {
    Entity entity0 = mock(Entity.class);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    when(repo1.findAll(query)).thenReturn(Stream.of(entity0));
    Stream<Entity> entities = dataService.findAll("Entity1", query);
    assertEquals(entities.collect(toList()), singletonList(entity0));
  }

  @Test
  public void findAllStreamStringQueryClass() {
    Class<Entity> clazz = Entity.class;
    Entity entity0 = mock(Entity.class);
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    when(repo1.findAll(query)).thenReturn(Stream.of(entity0));
    when(metaDataService.getRepository("Entity1", clazz)).thenReturn(of(repo1));
    Stream<Entity> entities = dataService.findAll("Entity1", query, clazz);
    assertEquals(entities.collect(toList()), singletonList(entity0));
  }
}
