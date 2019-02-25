package org.molgenis.js;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertFalse;

import org.mockito.Mock;
import org.molgenis.script.core.Script;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JsScriptRunnerTest extends AbstractMockitoTest {
  @Mock private JsScriptExecutor jsScriptExecutor;
  private JsScriptRunner jsScriptRunner;

  @BeforeMethod
  public void setUpBeforeMethod() {
    jsScriptRunner = new JsScriptRunner(jsScriptExecutor);
  }

  @Test
  public void testHasFileOutput() {
    assertFalse(jsScriptRunner.hasFileOutput(mock(Script.class)));
  }
}
