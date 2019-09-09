package org.molgenis.data.security;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.EMAIL;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.STRING;

import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class EntityIdentityUtilsTest {
  @Test
  void testToTypeEntityType() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("MyEntityTypeId");
    assertEquals(EntityIdentityUtils.toType(entityType), "entity-MyEntityTypeId");
  }

  @Test
  void testToTypeString() {
    assertEquals(EntityIdentityUtils.toType("MyEntityTypeId"), "entity-MyEntityTypeId");
  }

  static Iterator<Object[]> testToIdTypeProvider() {
    return asList(
            new Object[] {EMAIL, String.class},
            new Object[] {HYPERLINK, String.class},
            new Object[] {STRING, String.class},
            new Object[] {INT, Integer.class},
            new Object[] {LONG, Long.class})
        .iterator();
  }

  @ParameterizedTest
  @MethodSource("testToIdTypeProvider")
  void testToIdType(AttributeType attributeType, Class<?> expectedIdType) {
    EntityType entityType = mock(EntityType.class);
    Attribute idAttribute =
        when(mock(Attribute.class).getDataType()).thenReturn(attributeType).getMock();
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    assertEquals(EntityIdentityUtils.toIdType(entityType), expectedIdType);
  }
}
