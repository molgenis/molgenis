package org.molgenis.js.magma;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.molgenis.js.magma.JsMagmaScriptContext.KEY_ID_VALUE;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class EntityProxyTest extends AbstractMockitoTest {

  @Mock Entity entity;
  @Mock EntityType entityType;
  @Mock Attribute attribute;
  EntityProxy entityProxy;

  @BeforeEach
  public void setUp() {
    entityProxy = (EntityProxy) EntityProxy.toGraalValue(entity);
  }

  static Object[][] testToGraalValueLiterals() {
    return new Object[][] {
      new Object[] {Instant.ofEpochMilli(123456789L), 123456789L},
      new Object[] {
        LocalDate.parse("2020-03-27"),
        LocalDate.parse("2020-03-27")
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
      },
      new Object[] {42, 42}
    };
  }

  @MethodSource
  @ParameterizedTest
  void testToGraalValueLiterals(Object o, Object expected) {
    assertEquals(expected, EntityProxy.toGraalValue(o));
  }

  @Test
  void testGetMemberKeysAddsIdValueKey() {
    when(entity.getEntityType()).thenReturn(entityType);
    when(entityType.getAtomicAttributes()).thenReturn(List.of());

    ProxyArray keys = (ProxyArray) entityProxy.getMemberKeys();
    assertEquals(1, keys.getSize());
    assertEquals(KEY_ID_VALUE, keys.get(0));
  }

  @Test
  void testGetMemberKeysReturnsAtomicAttributes() {
    when(entity.getEntityType()).thenReturn(entityType);
    when(entityType.getAtomicAttributes()).thenReturn(List.of(attribute));
    when(attribute.getName()).thenReturn("attribute");

    ProxyArray keys = (ProxyArray) entityProxy.getMemberKeys();
    assertEquals(2, keys.getSize());
    assertEquals("attribute", keys.get(0));
  }

  @Test
  void testGetMember() {
    when(entity.get("attribute")).thenReturn("hello");

    assertEquals("hello", entityProxy.getMember("attribute"));
  }

  @Test
  void testHasMember() {
    when(entity.getEntityType()).thenReturn(entityType);
    when(entityType.getAttribute("attribute")).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(AttributeType.STRING);

    assertTrue(entityProxy.hasMember("attribute"));
  }

  @Test
  void testHasMemberCompound() {
    when(entity.getEntityType()).thenReturn(entityType);
    when(entityType.getAttribute("attribute")).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(AttributeType.COMPOUND);

    assertFalse(entityProxy.hasMember("attribute"));
  }

  @Test
  void testHasMemberIdValueKey() {
    assertTrue(entityProxy.hasMember(KEY_ID_VALUE));
  }

  @Test
  void testHasMemberUnknown() {
    when(entity.getEntityType()).thenReturn(entityType);

    assertFalse(entityProxy.hasMember("foo"));
  }
}
