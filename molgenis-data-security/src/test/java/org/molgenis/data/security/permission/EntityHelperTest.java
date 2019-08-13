package org.molgenis.data.security.permission;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.exception.InvalidTypeIdException;
import org.molgenis.data.security.permission.model.LabelledObjectIdentity;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityHelperTest extends AbstractMockitoTest {
  @Mock DataService dataService;
  @Mock Repository repository;
  private EntityHelper entityHelper;

  @BeforeMethod
  public void setUpBeforeMethod() {
    entityHelper = new EntityHelper(dataService);
  }

  @Test
  public void testGetEntityTypeIdFromType() {
    assertEquals(entityHelper.getEntityTypeIdFromType("entity-typeId"), "typeId");
  }

  @Test
  public void testGetObjectIdentity() {
    assertEquals(
        entityHelper.getObjectIdentity("typeId", "identifier"),
        new ObjectIdentityImpl("typeId", "identifier"));
  }

  @Test
  public void testGetTypeLabel() {
    Repository repository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getLabel()).thenReturn("typeLabel");
    when(repository.getEntityType()).thenReturn(entityType);

    when(dataService.getRepository("typeId")).thenReturn(repository);
    assertEquals(entityHelper.getLabel("entity-typeId"), "typeLabel");
  }

  @Test
  public void testGetRowLabel() {
    Repository repository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    when(repository.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repository);
    assertEquals(entityHelper.getLabel("entity-typeId", "identifier"), "label");
  }

  @Test
  public void testGetLabelledObjectIdentity() {
    Repository repository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getLabel()).thenReturn("typeLabel");
    when(repository.getEntityType()).thenReturn(entityType);
    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("label");
    when(repository.findOneById("identifier")).thenReturn(entity);
    when(dataService.getRepository("typeId")).thenReturn(repository);
    assertEquals(
        entityHelper.getLabelledObjectIdentity(
            new ObjectIdentityImpl("entity-typeId", "identifier")),
        LabelledObjectIdentity.create(
            "entity-typeId", "typeId", "typeLabel", "identifier", "label"));
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testCheckEntityExistsFail() {
    when(dataService.hasEntityType("typeId")).thenReturn(true);
    when(dataService.getRepository("typeId")).thenReturn(repository);
    when(repository.findOneById("identifier")).thenReturn(null);
    entityHelper.checkEntityExists(new ObjectIdentityImpl("entity-typeId", "identifier"));
  }

  @Test
  public void testCheckEntityExists() {
    when(dataService.hasEntityType("typeId")).thenReturn(true);

    when(dataService.getRepository("typeId")).thenReturn(repository);
    when(repository.findOneById("identifier")).thenReturn(mock(Entity.class));
    entityHelper.checkEntityExists(new ObjectIdentityImpl("entity-typeId", "identifier"));
  }

  @Test(expectedExceptions = UnknownEntityTypeException.class)
  public void testCheckEntityTypeExistsFail() {
    when(dataService.hasEntityType("typeId")).thenReturn(false);
    entityHelper.checkEntityTypeExists("entity-typeId");
  }

  @Test
  public void testCheckEntityTypeExists() {
    when(dataService.hasEntityType("typeId")).thenReturn(true);
    entityHelper.checkEntityTypeExists("entity-typeId");
  }

  @Test
  public void testGetEntityTypeIdFromClassPlugin() {
    assertEquals(entityHelper.getEntityTypeIdFromType("plugin"), "sys_Plugin");
  }

  @Test
  public void testGetEntityTypeIdFromClassET() {
    assertEquals(entityHelper.getEntityTypeIdFromType("entityType"), "sys_md_EntityType");
  }

  @Test
  public void testGetEntityTypeIdFromClassGroup() {
    assertEquals(entityHelper.getEntityTypeIdFromType("group"), "sys_sec_Group");
  }

  @Test
  public void testGetEntityTypeIdFromClassPack() {
    assertEquals(entityHelper.getEntityTypeIdFromType("package"), "sys_md_Package");
  }

  @Test
  public void testGetEntityTypeIdFromClassEntity() {
    assertEquals(entityHelper.getEntityTypeIdFromType("entity-test"), "test");
  }

  @Test(expectedExceptions = InvalidTypeIdException.class)
  public void testGetEntityTypeIdFromClassError() {
    assertEquals(entityHelper.getEntityTypeIdFromType("error"), "");
  }
}
