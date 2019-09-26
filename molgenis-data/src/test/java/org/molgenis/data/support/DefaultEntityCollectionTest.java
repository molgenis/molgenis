package org.molgenis.data.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;

class DefaultEntityCollectionTest {
  @SuppressWarnings("unchecked")
  @Test
  void isLazy() {
    Iterable<Entity> entities = mock(Iterable.class);
    Iterable<String> attrNames = mock(Iterable.class);
    assertFalse(new DefaultEntityCollection(entities, attrNames).isLazy());
  }
}
