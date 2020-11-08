package org.molgenis.js.graal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import javax.script.ScriptException;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GraalScriptEngineTest {
  private static GraalScriptEngine scriptEngine;

  @BeforeAll
  static void setUpBeforeClass() {
    scriptEngine = new GraalScriptEngine();
  }

  @Test
  void testInvokeFunction() throws Exception {
    long epoch = 1487342481434L;
    assertEquals(epoch, scriptEngine.eval("new Date(" + epoch + ")"));
  }

  @Test
  void testInvokeDateDMY() throws Exception {
    LocalDate localDate = LocalDate.now();
    String script =
        String.format(
            "new Date(%d,%d,%d)",
            localDate.getYear(), localDate.getMonth().getValue() - 1, localDate.getDayOfMonth());
    long epochMilli = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    assertEquals(epochMilli, scriptEngine.eval(script));
  }

  @Test
  void doesntDirtyContext() throws ScriptException, IOException {
    scriptEngine.eval("piet = 3");
    Exception exception = assertThrows(PolyglotException.class, () -> scriptEngine.eval("piet"));
    assertThat(exception.getMessage()).containsPattern("ReferenceError: piet is not defined");
  }
}
