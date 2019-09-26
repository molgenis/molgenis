package org.molgenis.js;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.script.core.Script;
import org.molgenis.test.AbstractMockitoTest;

class JsScriptRunnerTest extends AbstractMockitoTest {
  @Mock private JsScriptExecutor jsScriptExecutor;
  private JsScriptRunner jsScriptRunner;

  @BeforeEach
  void setUpBeforeMethod() {
    jsScriptRunner = new JsScriptRunner(jsScriptExecutor);
  }

  @Test
  void testHasFileOutput() {
    assertFalse(jsScriptRunner.hasFileOutput(mock(Script.class)));
  }
}
