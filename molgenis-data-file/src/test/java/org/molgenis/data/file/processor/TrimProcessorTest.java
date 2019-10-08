package org.molgenis.data.file.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TrimProcessorTest {
  @Test
  void process() {
    assertEquals("val", new TrimProcessor().process(" val "));
  }

  @Test
  void processNull() {
    assertNull(new TrimProcessor().process(null));
  }
}
