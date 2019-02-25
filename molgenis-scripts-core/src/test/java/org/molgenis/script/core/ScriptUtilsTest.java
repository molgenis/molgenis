package org.molgenis.script.core;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Map;
import org.testng.annotations.Test;

public class ScriptUtilsTest {
  @Test
  public void testGenerateScript() {
    Script script = mock(Script.class);
    when(script.getContent()).thenReturn("Hey ${name}");
    Map<String, Object> parameterValues = Collections.singletonMap("name", "Piet");
    String renderedScript = ScriptUtils.generateScript(script, parameterValues);
    assertEquals(renderedScript, "Hey Piet");
  }

  @Test
  public void testHasScriptVariableTrue() {
    Script script = when(mock(Script.class).getContent()).thenReturn("lorum ${x}").getMock();
    assertTrue(ScriptUtils.hasScriptVariable(script, "x"));
  }

  @Test
  public void testHasScriptVariableTrueHash() {
    Script script = when(mock(Script.class).getContent()).thenReturn("lorum ${x.y}").getMock();
    assertTrue(ScriptUtils.hasScriptVariable(script, "x"));
  }

  @Test
  public void testHasScriptVariableFalse() {
    Script script = when(mock(Script.class).getContent()).thenReturn("lorum ${x}").getMock();
    assertFalse(ScriptUtils.hasScriptVariable(script, "y"));
  }

  @Test
  public void testGetScriptVariables() {
    Script script =
        when(mock(Script.class).getContent()).thenReturn("lorum ${x} ipsum ${y.z}").getMock();
    assertEquals(ScriptUtils.getScriptVariables(script), Sets.newLinkedHashSet(asList("x", "y")));
  }
}
