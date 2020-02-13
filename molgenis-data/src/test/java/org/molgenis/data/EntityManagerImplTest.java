package org.molgenis.data;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.test.AbstractMockitoTest;

class EntityManagerImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private EntityFactoryRegistry entityFactoryRegistry;
  @Mock private EntityPopulator entityPopulator;
  @Mock private EntityReferenceCreator entityReferenceCreator;
  private EntityManagerImpl entityManagerImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    entityManagerImpl =
        new EntityManagerImpl(
            dataService, entityFactoryRegistry, entityPopulator, entityReferenceCreator);
  }

  @Test
  void getReference() {
    EntityType entityType = mock(EntityType.class);
    Object id = mock(Object.class);
    entityManagerImpl.getReference(entityType, id);
    verify(entityReferenceCreator).getReference(entityType, id);
  }

  @Test
  void getReferences() {
    EntityType entityType = mock(EntityType.class);
    Iterable<?> ids = mock(Iterable.class);
    entityManagerImpl.getReferences(entityType, ids);
    verify(entityReferenceCreator).getReferences(entityType, ids);
  }

  @Test
  void resolveReferencesNoFetch() {
    EntityType entityType = mock(EntityType.class);

    Entity entity0 = new DynamicEntity(entityType); // do not mock, setters will be called
    Entity entity1 = new DynamicEntity(entityType); // do not mock, setters will be called
    Stream<Entity> entities = Stream.of(entity0, entity1);

    Fetch fetch = null;
    assertEquals(entityManagerImpl.resolveReferences(entityType, entities, fetch), entities);
  }

  @Test
  void resolveReferencesStreamNoFetch() {
    EntityType entityType = mock(EntityType.class);

    Entity entity0 = new DynamicEntity(entityType); // do not mock, setters will be called
    Entity entity1 = new DynamicEntity(entityType); // do not mock, setters will be called

    Fetch fetch = null;
    Stream<Entity> entities =
        entityManagerImpl.resolveReferences(entityType, Stream.of(entity0, entity1), fetch);
    assertEquals(asList(entity0, entity1), entities.collect(toList()));
  }
}
