package org.molgenis.data.file.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class LowerCaseProcessorTest {
  @Test
  void process() {
    assertEquals(new LowerCaseProcessor().process("A"), "a");
  }

  @Test
  void processNull() {
    assertNull(new LowerCaseProcessor().process(null));
  }
}
