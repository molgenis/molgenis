package org.molgenis.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UnexpectedEnumExceptionTest {
  enum MyEnum {
    MY_ENUM_CONSTANT
  }

  @Test
  void testUnexpectedEnumException() {
    assertEquals(
        new UnexpectedEnumException(MyEnum.MY_ENUM_CONSTANT).getMessage(),
        "Unexpected enum constant 'MY_ENUM_CONSTANT' for type 'MyEnum'");
  }
}
