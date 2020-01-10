package org.molgenis.data;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.support.DynamicEntity;

class EntityManagerImplTest {
  private DataService dataService;
  private EntityManagerImpl entityManagerImpl;
  private EntityFactoryRegistry entityFactoryRegistry;
  private EntityPopulator entityPopulator;
  private EntityReferenceCreator entityReferenceCreator;

  @BeforeEach
  void setUpBeforeMethod() {
    dataService = mock(DataService.class);
    entityFactoryRegistry = mock(EntityFactoryRegistry.class);
    entityPopulator = mock(EntityPopulator.class);
    entityReferenceCreator = mock(EntityReferenceCreator.class);
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
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();

    Entity entity0 = new DynamicEntity(entityType); // do not mock, setters will be called
    Entity entity1 = new DynamicEntity(entityType); // do not mock, setters will be called
    Stream<Entity> entities = Stream.of(entity0, entity1);

    Fetch fetch = null;
    assertEquals(entityManagerImpl.resolveReferences(entityType, entities, fetch), entities);
  }

  @Test
  void resolveReferencesStreamNoFetch() {
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
    Attribute labelAttr = when(mock(Attribute.class).getName()).thenReturn("labelAttr").getMock();
    when(labelAttr.getDataType()).thenReturn(STRING);
    when(entityType.getLabelAttribute()).thenReturn(labelAttr);
    when(entityType.getAtomicAttributes()).thenReturn(singletonList(labelAttr));

    Entity entity0 = new DynamicEntity(entityType); // do not mock, setters will be called
    Entity entity1 = new DynamicEntity(entityType); // do not mock, setters will be called

    Fetch fetch = null;
    Stream<Entity> entities =
        entityManagerImpl.resolveReferences(entityType, Stream.of(entity0, entity1), fetch);
    assertEquals(asList(entity0, entity1), entities.collect(toList()));
  }
}
