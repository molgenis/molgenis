package org.molgenis.script.core;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.script.core.ScriptUtils.getScriptVariables;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScriptUtilsTest {
  @Test
  void testGenerateScript() {
    Script script = mock(Script.class);
    when(script.getContent()).thenReturn("Hey ${name}");
    Map<String, Object> parameterValues = Collections.singletonMap("name", "Piet");
    String renderedScript = ScriptUtils.generateScript(script, parameterValues);
    assertEquals("Hey Piet", renderedScript);
  }

  @Test
  void testHasScriptVariableTrue() {
    Script script = when(mock(Script.class).getContent()).thenReturn("lorum ${x}").getMock();
    assertTrue(ScriptUtils.hasScriptVariable(script, "x"));
  }

  @Test
  void testHasScriptVariableTrueHash() {
    Script script = when(mock(Script.class).getContent()).thenReturn("lorum ${x.y}").getMock();
    assertTrue(ScriptUtils.hasScriptVariable(script, "x"));
  }

  @Test
  void testHasScriptVariableFalse() {
    Script script = when(mock(Script.class).getContent()).thenReturn("lorum ${x}").getMock();
    assertFalse(ScriptUtils.hasScriptVariable(script, "y"));
  }

  @Test
  void testGetScriptVariables() {
    Script script =
        when(mock(Script.class).getContent()).thenReturn("lorum ${x} ipsum ${y.z}").getMock();
    assertEquals(newLinkedHashSet(asList("x", "y")), getScriptVariables(script));
  }
}
