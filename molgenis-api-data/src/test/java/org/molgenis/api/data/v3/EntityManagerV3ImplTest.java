package org.molgenis.api.data.v3;

import org.mockito.Mock;
import org.molgenis.data.EntityManager;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityManagerV3ImplTest extends AbstractMockitoTest {
  @Mock private EntityManager entityManager;
  private EntityManagerV3Impl entityManagerV3Impl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    entityManagerV3Impl = new EntityManagerV3Impl(entityManager);
  }

  @Test
  public void testCreate() {
    throw new UnsupportedOperationException();
  }

  @Test
  public void testPopulate() {
    throw new UnsupportedOperationException();
  }
}
