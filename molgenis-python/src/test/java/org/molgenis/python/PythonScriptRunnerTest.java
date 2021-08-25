package org.molgenis.python;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.script.core.Script;
import org.molgenis.script.core.ScriptOutputHandler;
import org.molgenis.script.core.ScriptUtils;
import org.molgenis.test.AbstractMockitoTest;

class PythonScriptRunnerTest extends AbstractMockitoTest {
  @Mock private PythonScriptExecutor pythonScriptExecutor;
  private PythonScriptRunner pythonScriptRunner;

  @BeforeEach
  void setUpBeforeMethod() {
    pythonScriptRunner = new PythonScriptRunner(pythonScriptExecutor);
  }

  @Test
  void testHasFileOutput() {
    assertFalse(pythonScriptRunner.hasFileOutput(mock(Script.class)));
  }

  @Test
  void testRunScript() {
    var script = mock(Script.class);
    when(script.getContent()).thenReturn("test");
    Map<String, Object> parameters = emptyMap();
    var scriptString = "test";

    pythonScriptRunner.runScript(script, parameters);

    verify(pythonScriptExecutor).executeScript(eq(scriptString), any(ScriptOutputHandler.class));
  }

  @Test
  void testRunScriptHandler() {
    var script = mock(Script.class);
    when(script.getContent()).thenReturn("test");
    Map<String, Object> parameters = emptyMap();
    var scriptOutputHandler = mock(ScriptOutputHandler.class);
    var scriptString = "test";

    pythonScriptRunner.runScript(script, parameters, scriptOutputHandler);

    verify(pythonScriptExecutor).executeScript(scriptString, scriptOutputHandler);
  }

}
