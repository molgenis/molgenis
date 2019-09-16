package org.molgenis.data.file.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class LowerCaseProcessorTest {
  @Test
  void process() {
    assertEquals("a", new LowerCaseProcessor().process("A"));
  }

  @Test
  void processNull() {
    assertNull(new LowerCaseProcessor().process(null));
  }
}
