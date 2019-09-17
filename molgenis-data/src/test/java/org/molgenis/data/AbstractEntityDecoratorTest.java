package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class AbstractEntityDecoratorTest extends AbstractMockitoTest {

  private TestEntity testEntity;

  @Mock private Entity delegateEntity;

  @BeforeEach
  void beforeMethod() {
    testEntity = new TestEntity(delegateEntity);
  }

  private class TestEntity extends AbstractEntityDecorator {
    TestEntity(Entity entity) {
      super(entity);
    }
  }

  @Test
  void testDelegate() {
    assertEquals(delegateEntity, testEntity.delegate());
  }

  @Test
  void testGetEntityType() {
    testEntity.getEntityType();
    verify(delegateEntity).getEntityType();
  }

  @Test
  void testGetAttributeNames() {
    testEntity.getAttributeNames();
    verify(delegateEntity).getAttributeNames();
  }

  @Test
  void testGetIdValue() {
    testEntity.getIdValue();
    verify(delegateEntity).getIdValue();
  }

  @Test
  void testSetIdValue() {
    testEntity.setIdValue("");
    verify(delegateEntity).setIdValue("");
  }

  @Test
  void testGetLabelValue() {
    testEntity.getLabelValue();
    verify(delegateEntity).getLabelValue();
  }

  @Test
  void testGet() {
    testEntity.get("");
    verify(delegateEntity).get("");
  }

  @Test
  void testGetString() {
    testEntity.getString("");
    verify(delegateEntity).getString("");
  }

  @Test
  void testGetInt() {
    testEntity.getInt("");
    verify(delegateEntity).getInt("");
  }

  @Test
  void testGetLong() {
    testEntity.getLong("");
    verify(delegateEntity).getLong("");
  }

  @Test
  void testGetBoolean() {
    testEntity.getBoolean("");
    verify(delegateEntity).getBoolean("");
  }

  @Test
  void testGetDouble() {
    testEntity.getDouble("");
    verify(delegateEntity).getDouble("");
  }

  @Test
  void testGetInstant() {
    testEntity.getInstant("");
    verify(delegateEntity).getInstant("");
  }

  @Test
  void testGetLocalDate() {
    testEntity.getLocalDate("");
    verify(delegateEntity).getLocalDate("");
  }

  @Test
  void testGetEntity() {
    testEntity.getEntity("");
    verify(delegateEntity).getEntity("");
  }

  @Test
  void testGetEntityTyped() {
    testEntity.getEntity("", EntityType.class);
    verify(delegateEntity).getEntity("", EntityType.class);
  }

  @Test
  void testGetEntities() {
    testEntity.getEntities("");
    verify(delegateEntity).getEntities("");
  }

  @Test
  void testGetEntitiesTyped() {
    testEntity.getEntities("", EntityType.class);
    verify(delegateEntity).getEntities("", EntityType.class);
  }

  @Test
  void testSet() {
    testEntity.set("", "");
    verify(delegateEntity).set("", "");
  }

  @Test
  void testSetEntity() {
    Entity entity = mock(Entity.class);
    testEntity.set(entity);
    verify(delegateEntity).set(entity);
  }
}
