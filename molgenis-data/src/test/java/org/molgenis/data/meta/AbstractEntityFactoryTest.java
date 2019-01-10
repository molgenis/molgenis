package org.molgenis.data.meta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.SystemEntityTestUtils.getRandomString;
import static org.testng.Assert.*;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityFactory;

public abstract class AbstractEntityFactoryTest extends AbstractMolgenisSpringTest {

  protected void testCreate(EntityFactory entityFactory, Class expectedClass) {
    Entity actual = entityFactory.create();
    assertNotNull(actual);
    assertEquals(actual.getClass(), expectedClass);
  }

  protected void testCreateWithId(EntityFactory entityFactory, Class expectedClass) {
    String testId = getRandomString();
    //noinspection unchecked
    Entity actual = entityFactory.create(testId);
    assertNotNull(actual);
    assertEquals(actual.getIdValue(), testId);
    assertEquals(actual.getClass(), expectedClass);
  }

  protected void testCreateWithEntity(EntityFactory entityFactory, Class expectedClass) {
    String testId = getRandomString();
    Entity entity = mock(Entity.class);
    when(entity.getIdValue()).thenReturn(testId);

    Entity actual = entityFactory.create(entity);
    assertNotNull(actual);
    assertEquals(actual.getIdValue(), entity.getIdValue());
    assertEquals(actual.getClass(), expectedClass);
  }

  @SuppressWarnings("unused")
  public abstract void testCreate();

  @SuppressWarnings("unused")
  public abstract void testCreateWithId();

  @SuppressWarnings("unused")
  public abstract void testCreateWithEntity();
}
