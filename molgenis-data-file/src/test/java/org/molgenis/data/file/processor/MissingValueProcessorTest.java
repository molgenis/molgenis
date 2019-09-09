package org.molgenis.data.file.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MissingValueProcessorTest {
  @Test
  void processNull() {
    assertEquals(new MissingValueProcessor("unknown", false).process(null), "unknown");
  }

  @Test
  void processNull_processEmpty() {
    assertEquals(new MissingValueProcessor("unknown", true).process(null), "unknown");
  }

  @Test
  void processEmpty() {
    assertEquals(new MissingValueProcessor("unknown", true).process(""), "unknown");
  }

  @Test
  void processEmpty_processEmpty() {
    assertEquals(new MissingValueProcessor("unknown", false).process(""), "");
  }

  @Test
  void process() {
    assertEquals(new MissingValueProcessor("unknown", true).process("value"), "value");
  }

  @Test
  void process_processEmpty() {
    assertEquals(new MissingValueProcessor("unknown", true).process("value"), "value");
  }

  @Test
  void equals() {
    assertEquals(
        new MissingValueProcessor("unknown", true), new MissingValueProcessor("unknown", true));
  }
}
