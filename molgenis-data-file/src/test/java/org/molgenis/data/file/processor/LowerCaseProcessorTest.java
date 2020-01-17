package org.molgenis.data.file.processor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LowerCaseProcessorTest {
  @Test
  void testLowerCaseProcessor() {
    LowerCaseProcessor lowerCaseProcessor = new LowerCaseProcessor(true, false);
    assertAll(
        () -> assertTrue(lowerCaseProcessor.processHeader()),
        () -> assertFalse(lowerCaseProcessor.processData()));
  }

  @Test
  void process() {
    assertEquals("a", new LowerCaseProcessor().process("A"));
  }

  @Test
  void processNull() {
    assertNull(new LowerCaseProcessor().process(null));
  }
}
