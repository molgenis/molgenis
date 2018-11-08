package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AbstractEntityDecoratorTest extends AbstractMockitoTest {

  private TestEntity testEntity;

  @Mock private Entity delegateEntity;

  @BeforeMethod
  public void beforeMethod() {
    testEntity = new TestEntity(delegateEntity);
  }

  private class TestEntity extends AbstractEntityDecorator {

    TestEntity(Entity entity) {
      super(entity);
    }
  }

  @Test
  public void testDelegate() {
    assertEquals(testEntity.delegate(), delegateEntity);
  }

  @Test
  public void testGetEntityType() {
    testEntity.getEntityType();
    verify(delegateEntity).getEntityType();
  }

  @Test
  public void testGetAttributeNames() {
    testEntity.getAttributeNames();
    verify(delegateEntity).getAttributeNames();
  }

  @Test
  public void testGetIdValue() {
    testEntity.getIdValue();
    verify(delegateEntity).getIdValue();
  }

  @Test
  public void testSetIdValue() {
    testEntity.setIdValue("");
    verify(delegateEntity).setIdValue("");
  }

  @Test
  public void testGetLabelValue() {
    testEntity.getLabelValue();
    verify(delegateEntity).getLabelValue();
  }

  @Test
  public void testGet() {
    testEntity.get("");
    verify(delegateEntity).get("");
  }

  @Test
  public void testGetString() {
    testEntity.getString("");
    verify(delegateEntity).getString("");
  }

  @Test
  public void testGetInt() {
    testEntity.getInt("");
    verify(delegateEntity).getInt("");
  }

  @Test
  public void testGetLong() {
    testEntity.getLong("");
    verify(delegateEntity).getLong("");
  }

  @Test
  public void testGetBoolean() {
    testEntity.getBoolean("");
    verify(delegateEntity).getBoolean("");
  }

  @Test
  public void testGetDouble() {
    testEntity.getDouble("");
    verify(delegateEntity).getDouble("");
  }

  @Test
  public void testGetInstant() {
    testEntity.getInstant("");
    verify(delegateEntity).getInstant("");
  }

  @Test
  public void testGetLocalDate() {
    testEntity.getLocalDate("");
    verify(delegateEntity).getLocalDate("");
  }

  @Test
  public void testGetEntity() {
    testEntity.getEntity("");
    verify(delegateEntity).getEntity("");
  }

  @Test
  public void testGetEntityTyped() {
    testEntity.getEntity("", EntityType.class);
    verify(delegateEntity).getEntity("", EntityType.class);
  }

  @Test
  public void testGetEntities() {
    testEntity.getEntities("");
    verify(delegateEntity).getEntities("");
  }

  @Test
  public void testGetEntitiesTyped() {
    testEntity.getEntities("", EntityType.class);
    verify(delegateEntity).getEntities("", EntityType.class);
  }

  @Test
  public void testSet() {
    testEntity.set("", "");
    verify(delegateEntity).set("", "");
  }

  @Test
  public void testSetEntity() {
    Entity entity = mock(Entity.class);
    testEntity.set(entity);
    verify(delegateEntity).set(entity);
  }
}
