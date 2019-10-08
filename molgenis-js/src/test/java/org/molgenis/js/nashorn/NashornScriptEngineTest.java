package org.molgenis.js.nashorn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.ZoneId;
import javax.script.ScriptException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Created by Dennis on 2/17/2017. */
class NashornScriptEngineTest {
  private static NashornScriptEngine nashornScriptEngine;

  @BeforeAll
  static void setUpBeforeClass() {
    nashornScriptEngine = new NashornScriptEngine();
  }

  @Test
  void testInvokeFunction() throws Exception {
    long epoch = 1487342481434L;
    assertEquals(epoch, nashornScriptEngine.eval("new Date(" + epoch + ")"));
  }

  @Test
  void testInvokeDateDMY() throws Exception {
    LocalDate localDate = LocalDate.now();
    String script =
        String.format(
            "new Date(%d,%d,%d)",
            localDate.getYear(), localDate.getMonth().getValue() - 1, localDate.getDayOfMonth());
    long epochMilli = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    assertEquals(epochMilli, nashornScriptEngine.eval(script));
  }

  @Test
  void doesntDirtyContext() throws ScriptException {
    nashornScriptEngine.eval("piet = 3");
    Exception exception =
        assertThrows(ScriptException.class, () -> nashornScriptEngine.eval("piet"));
    assertThat(exception.getMessage())
        .containsPattern("ReferenceError: \"piet\" is not defined in <eval> at line number 1");
  }
}
