package org.molgenis.data.support;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.XREF;

import java.util.Iterator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class DynamicEntityTest {
  static Iterator<Object[]> setNoExceptionProvider() {
    return newArrayList(
            new Object[] {ONE_TO_MANY, mock(Iterable.class)},
            new Object[] {XREF, mock(Entity.class)})
        .iterator();
  }

  @ParameterizedTest
  @MethodSource("setNoExceptionProvider")
  void setNoException(AttributeType attrType, Object value) {
    set(attrType, value); // test if no exception occurs
  }

  static Iterator<Object[]> setExceptionProvider() {
    return newArrayList(
            new Object[] {ONE_TO_MANY, mock(Entity.class)},
            new Object[] {XREF, mock(Iterable.class)},
            new Object[] {DECIMAL, Double.NaN})
        .iterator();
  }

  @ParameterizedTest
  @MethodSource("setExceptionProvider")
  void setException(AttributeType attrType, Object value) {
    assertThrows(MolgenisDataException.class, () -> set(attrType, value));
  }

  private static void set(AttributeType attrType, Object value) {
    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    String attrName = "attr";
    when(attr.getName()).thenReturn(attrName);
    when(attr.getDataType()).thenReturn(attrType);
    when(entityType.getAttribute(attrName)).thenReturn(attr);
    DynamicEntity dynamicEntity = new DynamicEntity(entityType);
    dynamicEntity.set(attrName, value);
  }
}
