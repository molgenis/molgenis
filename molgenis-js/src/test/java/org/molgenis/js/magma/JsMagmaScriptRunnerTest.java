package org.molgenis.js.magma;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertFalse;

import org.mockito.Mock;
import org.molgenis.script.core.Script;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JsMagmaScriptRunnerTest extends AbstractMockitoTest {
  @Mock private JsMagmaScriptExecutor jsMagmaScriptExecutor;
  private JsMagmaScriptRunner jsMagmaScriptRunner;

  @BeforeMethod
  public void setUpBeforeMethod() {
    jsMagmaScriptRunner = new JsMagmaScriptRunner(jsMagmaScriptExecutor);
  }

  @Test
  public void testHasFileOutput() {
    assertFalse(jsMagmaScriptRunner.hasFileOutput(mock(Script.class)));
  }
}
