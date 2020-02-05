package org.molgenis.data.security.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.security.permission.model.LabelledObjectIdentity.create;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.exception.InvalidTypeIdException;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.domain.ObjectIdentityImpl;

class EntityHelperTest extends AbstractMockitoTest {
  @Mock DataService dataService;
  @Mock Repository repository;
  private EntityHelper entityHelper;

  @BeforeEach
  void setUpBeforeMethod() {
    entityHelper = new EntityHelper(dataService);
  }

  @Test
  void testGetEntityTypeIdFromType() {
    assertEquals("typeId", entityHelper.getEntityTypeIdFromType("entity-typeId"));
  }

  @Test
  void testGetObjectIdentity() {
    assertEquals(
        new ObjectIdentityImpl("typeId", "identifier"),
        entityHelper.getObjectIdentity("typeId", "identifier"));
  }

  @Test
  void testGetTypeLabel() {
    Repository repository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getLabel()).thenReturn("typeLabel");
    when(repository.getEntityType()).thenReturn(entityType);

    when(dataService.getRepository("typeId")).thenReturn(repository);
    assertEquals("typeLabel", entityHelper.getLabel("entity-typeId"));
  }

  @Test
  void testGetRowLabel() {
    Repository repository = mock(Repository.class);
    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    when(repository.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repository);
    EntityType entityType = mock(EntityType.class);
    Attribute idAttr = mock(Attribute.class);
    when(idAttr.getDataType()).thenReturn(STRING);
    when(entityType.getIdAttribute()).thenReturn(idAttr);
    when(dataService.getEntityType("typeId")).thenReturn(entityType);
    assertEquals("label", entityHelper.getLabel("entity-typeId", "identifier"));
  }

  @Test
  void testGetLabelledObjectIdentity() {
    Repository repository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getLabel()).thenReturn("typeLabel");
    Attribute idAttr = mock(Attribute.class);
    when(idAttr.getDataType()).thenReturn(STRING);
    when(entityType.getIdAttribute()).thenReturn(idAttr);
    when(repository.getEntityType()).thenReturn(entityType);
    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    when(repository.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repository);
    when(dataService.getEntityType("typeId")).thenReturn(entityType);
    assertEquals(
        create("entity-typeId", "typeId", "typeLabel", "identifier", "label"),
        entityHelper.getLabelledObjectIdentity(
            new ObjectIdentityImpl("entity-typeId", "identifier")));
  }

  @Test
  void testGetLabelledObjectIdentityIntId() {
    Repository repository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getLabel()).thenReturn("typeLabel");
    Attribute idAttr = mock(Attribute.class);
    when(idAttr.getDataType()).thenReturn(INT);
    when(entityType.getIdAttribute()).thenReturn(idAttr);
    when(repository.getEntityType()).thenReturn(entityType);
    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    when(repository.findOneById(1)).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repository);
    when(dataService.getEntityType("typeId")).thenReturn(entityType);
    assertEquals(
        create("entity-typeId", "typeId", "typeLabel", "1", "label"),
        entityHelper.getLabelledObjectIdentity(new ObjectIdentityImpl("entity-typeId", 1)));
  }

  @Test
  void testCheckEntityExistsFail() {
    when(dataService.hasEntityType("typeId")).thenReturn(true);
    when(dataService.getRepository("typeId")).thenReturn(repository);
    when(repository.findOneById("identifier")).thenReturn(null);
    assertThrows(
        UnknownEntityException.class,
        () ->
            entityHelper.checkEntityExists(new ObjectIdentityImpl("entity-typeId", "identifier")));
  }

  @Test
  void testCheckEntityExists() {
    when(dataService.hasEntityType("typeId")).thenReturn(true);

    when(dataService.getRepository("typeId")).thenReturn(repository);
    when(repository.findOneById("identifier")).thenReturn(mock(Entity.class));
    entityHelper.checkEntityExists(new ObjectIdentityImpl("entity-typeId", "identifier"));
  }

  @Test
  void testCheckEntityTypeExistsFail() {
    when(dataService.hasEntityType("typeId")).thenReturn(false);
    assertThrows(
        UnknownEntityTypeException.class,
        () -> entityHelper.checkEntityTypeExists("entity-typeId"));
  }

  @Test
  void testCheckEntityTypeExists() {
    when(dataService.hasEntityType("typeId")).thenReturn(true);
    entityHelper.checkEntityTypeExists("entity-typeId");
  }

  @Test
  void testGetEntityTypeIdFromClassPlugin() {
    assertEquals("sys_Plugin", entityHelper.getEntityTypeIdFromType("plugin"));
  }

  @Test
  void testGetEntityTypeIdFromClassET() {
    assertEquals("sys_md_EntityType", entityHelper.getEntityTypeIdFromType("entityType"));
  }

  @Test
  void testGetEntityTypeIdFromClassGroup() {
    assertEquals("sys_sec_Group", entityHelper.getEntityTypeIdFromType("group"));
  }

  @Test
  void testGetEntityTypeIdFromClassPack() {
    assertEquals("sys_md_Package", entityHelper.getEntityTypeIdFromType("package"));
  }

  @Test
  void testGetEntityTypeIdFromClassEntity() {
    assertEquals("test", entityHelper.getEntityTypeIdFromType("entity-test"));
  }

  @Test
  void testGetEntityTypeIdFromClassError() {
    assertThrows(InvalidTypeIdException.class, () -> entityHelper.getEntityTypeIdFromType("error"));
  }
}
