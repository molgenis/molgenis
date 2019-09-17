package org.molgenis.python;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.script.core.Script;
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
}
