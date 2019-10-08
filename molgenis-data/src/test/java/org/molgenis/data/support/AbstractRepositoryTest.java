package org.molgenis.data.support;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class AbstractRepositoryTest {
  private static AbstractRepository abstractRepository;
  private static EntityType entityType;

  @Captor private ArgumentCaptor<Stream<Entity>> addStreamCaptor;

  @Captor private ArgumentCaptor<Stream<Entity>> updateStreamCaptor;

  @Captor private ArgumentCaptor<Stream<Object>> objectStreamCaptor;

  @BeforeEach
  void beforeMethod() {
    MockitoAnnotations.initMocks(this);
    Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
    entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
    when(entityType.getIdAttribute()).thenReturn(idAttr);
    abstractRepository =
        Mockito.spy(
            new AbstractRepository() {

              @Override
              public Iterator<Entity> iterator() {
                return null;
              }

              public EntityType getEntityType() {
                return entityType;
              }

              @Override
              public Set<RepositoryCapability> getCapabilities() {
                return Collections.emptySet();
              }
            });
  }

  @Test
  void addStream() {
    assertThrows(UnsupportedOperationException.class, () -> abstractRepository.add(Stream.empty()));
  }

  @Test
  void deleteStream() {
    assertThrows(
        UnsupportedOperationException.class, () -> abstractRepository.delete(Stream.empty()));
  }

  @Test
  void updateStream() {
    assertThrows(
        UnsupportedOperationException.class, () -> abstractRepository.update(Stream.empty()));
  }

  @Test
  void findOneObjectFetch() {
    assertThrows(
        UnsupportedOperationException.class, () -> abstractRepository.findOneById(0, new Fetch()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void findAllStream() {
    Object id0 = "id0";
    Object id1 = "id1";
    Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(id0).getMock();
    Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(id1).getMock();
    Stream<Object> entityIds = Stream.of(id0, id1);

    doReturn(Stream.of(entity0, entity1))
        .when(abstractRepository)
        .findAll(ArgumentMatchers.any(Query.class));

    Stream<Entity> expectedEntities = abstractRepository.findAll(entityIds);
    assertEquals(asList(entity0, entity1), expectedEntities.collect(toList()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void findAllStreamFetch() {
    Fetch fetch = new Fetch();
    Object id0 = "id0";
    Object id1 = "id1";
    Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(id0).getMock();
    Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(id1).getMock();
    Stream<Object> entityIds = Stream.of(id0, id1);

    doReturn(Stream.of(entity0, entity1))
        .when(abstractRepository)
        .findAll(ArgumentMatchers.any(Query.class));

    Stream<Entity> expectedEntities = abstractRepository.findAll(entityIds, fetch);
    assertEquals(asList(entity0, entity1), expectedEntities.collect(toList()));
  }

  //	// Note: streamFetch cannot be tested because mocking default methods is not supported by
  // Mockito

  @Test
  void testUpsertBatch() {
    Object id0 = "id0";
    Object id1 = "id1";
    Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(id0).getMock();
    Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(id1).getMock();
    List<Entity> batch = Arrays.asList(entity0, entity1);

    doReturn(Stream.of(entity0))
        .when(abstractRepository)
        .findAll(objectStreamCaptor.capture(), any(Fetch.class));
    doReturn(1).when(abstractRepository).add(addStreamCaptor.capture());
    doNothing().when(abstractRepository).update(updateStreamCaptor.capture());

    abstractRepository.upsertBatch(batch);

    assertEquals(
        addStreamCaptor.getValue().collect(Collectors.toList()),
        Collections.singletonList(entity1),
        "New entity should get added.");
    assertEquals(
        updateStreamCaptor.getValue().collect(Collectors.toList()),
        Collections.singletonList(entity0),
        "Existing entity should get updated.");
  }
}
