package org.molgenis.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.util.UnexpectedEnumExceptionTest.MyEnum.MY_ENUM_CONSTANT;

import org.junit.jupiter.api.Test;

class UnexpectedEnumExceptionTest {
  enum MyEnum {
    MY_ENUM_CONSTANT
  }

  @Test
  void testUnexpectedEnumException() {
    assertEquals(
        "Unexpected enum constant 'MY_ENUM_CONSTANT' for type 'MyEnum'",
        new UnexpectedEnumException(MY_ENUM_CONSTANT).getMessage());
  }
}
