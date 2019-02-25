package org.molgenis.python;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertFalse;

import org.mockito.Mock;
import org.molgenis.script.core.Script;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PythonScriptRunnerTest extends AbstractMockitoTest {
  @Mock private PythonScriptExecutor pythonScriptExecutor;
  private PythonScriptRunner pythonScriptRunner;

  @BeforeMethod
  public void setUpBeforeMethod() {
    pythonScriptRunner = new PythonScriptRunner(pythonScriptExecutor);
  }

  @Test
  public void testHasFileOutput() {
    assertFalse(pythonScriptRunner.hasFileOutput(mock(Script.class)));
  }
}
