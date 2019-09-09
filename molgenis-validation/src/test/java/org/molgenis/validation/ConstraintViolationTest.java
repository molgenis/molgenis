package org.molgenis.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ConstraintViolationTest {
  @Test
  void renumberRowIndexRowNumber() {
    ConstraintViolation constraintViolation = new ConstraintViolation("test", 1L);
    constraintViolation.renumberRowIndex(Arrays.asList(3, 2, 1));
    assertEquals(constraintViolation.getRowNr(), Long.valueOf(3L));
  }

  @Test
  void renumberRowIndexNoRowNumber() {
    ConstraintViolation constraintViolation = new ConstraintViolation("test", null);
    constraintViolation.renumberRowIndex(Arrays.asList(3, 2, 1));
    assertNull(constraintViolation.getRowNr());
  }
}
