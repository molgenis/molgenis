package org.molgenis.script.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ScriptOutputHandlerTest {

  @Test
  void testHandler() {
    var handler = new ScriptOutputHandler();
    handler.append("test1");
    handler.getConsumer().accept("test2");
    assertEquals("test1test2", handler.toString());
  }
}
