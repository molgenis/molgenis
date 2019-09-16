package org.molgenis.data.file.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MissingValueProcessorTest {
  @Test
  void processNull() {
    assertEquals("unknown", new MissingValueProcessor("unknown", false).process(null));
  }

  @Test
  void processNull_processEmpty() {
    assertEquals("unknown", new MissingValueProcessor("unknown", true).process(null));
  }

  @Test
  void processEmpty() {
    assertEquals("unknown", new MissingValueProcessor("unknown", true).process(""));
  }

  @Test
  void processEmpty_processEmpty() {
    assertEquals("", new MissingValueProcessor("unknown", false).process(""));
  }

  @Test
  void process() {
    assertEquals("value", new MissingValueProcessor("unknown", true).process("value"));
  }

  @Test
  void process_processEmpty() {
    assertEquals("value", new MissingValueProcessor("unknown", true).process("value"));
  }

  @Test
  void equals() {
    assertEquals(
        new MissingValueProcessor("unknown", true), new MissingValueProcessor("unknown", true));
  }
}
