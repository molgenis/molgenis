package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Iterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class EntityReferenceCreatorImplTest {
  private DataService dataService;
  private EntityReferenceCreatorImpl entityManagerImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    dataService = mock(DataService.class);
    EntityFactoryRegistry entityFactoryRegistry = mock(EntityFactoryRegistry.class);
    entityManagerImpl = new EntityReferenceCreatorImpl(dataService, entityFactoryRegistry);
  }

  @Test
  void EntityReferenceCreatorImpl() {
    assertThrows(NullPointerException.class, () -> new EntityReferenceCreatorImpl(null, null));
  }

  @Test
  void getReference() {
    String entityTypeId = "entity";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
    Attribute lblAttr = when(mock(Attribute.class).getName()).thenReturn("label").getMock();
    when(entityType.getIdAttribute()).thenReturn(idAttr);
    when(entityType.getLabelAttribute()).thenReturn(lblAttr);

    String label = "label";
    Integer id = 0;
    Entity entity = when(mock(Entity.class).getLabelValue()).thenReturn(label).getMock();
    when(dataService.findOneById(entityTypeId, id)).thenReturn(entity);

    Entity entityReference = entityManagerImpl.getReference(entityType, id);
    assertEquals(id, entityReference.getIdValue());
    verifyNoMoreInteractions(dataService);
    assertEquals(entityReference.getLabelValue(), label);
    verify(dataService, times(1)).findOneById(entityTypeId, id);
  }

  @Test
  void getReferences() {
    String entityTypeId = "entity";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
    Attribute lblAttr = when(mock(Attribute.class).getName()).thenReturn("label").getMock();
    when(entityType.getIdAttribute()).thenReturn(idAttr);
    when(entityType.getLabelAttribute()).thenReturn(lblAttr);

    String label0 = "label0";
    Integer id0 = 0;
    Entity entity0 = when(mock(Entity.class).getLabelValue()).thenReturn(label0).getMock();
    when(dataService.findOneById(entityTypeId, id0)).thenReturn(entity0);

    String label1 = "label1";
    Integer id1 = 1;
    Entity entity1 = when(mock(Entity.class).getLabelValue()).thenReturn(label1).getMock();
    when(dataService.findOneById(entityTypeId, id1)).thenReturn(entity1);

    Iterable<Entity> entityReferences =
        entityManagerImpl.getReferences(entityType, Arrays.asList(id0, id1));
    Iterator<Entity> it = entityReferences.iterator();
    assertTrue(it.hasNext());

    Entity entityReference0 = it.next();
    assertEquals(id0, entityReference0.getIdValue());
    verifyNoMoreInteractions(dataService);
    assertEquals(label0, entityReference0.getLabelValue());
    verify(dataService, times(1)).findOneById(entityTypeId, id0);

    assertTrue(it.hasNext());
    Entity entityReference1 = it.next();
    assertEquals(id1, entityReference1.getIdValue());
    verifyNoMoreInteractions(dataService);
    assertEquals(label1, entityReference1.getLabelValue());
    verify(dataService, times(1)).findOneById(entityTypeId, id1);

    assertFalse(it.hasNext());
  }
}
