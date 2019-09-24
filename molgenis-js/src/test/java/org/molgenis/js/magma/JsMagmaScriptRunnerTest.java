package org.molgenis.js.magma;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.script.core.Script;
import org.molgenis.test.AbstractMockitoTest;

class JsMagmaScriptRunnerTest extends AbstractMockitoTest {
  @Mock private JsMagmaScriptExecutor jsMagmaScriptExecutor;
  private JsMagmaScriptRunner jsMagmaScriptRunner;

  @BeforeEach
  void setUpBeforeMethod() {
    jsMagmaScriptRunner = new JsMagmaScriptRunner(jsMagmaScriptExecutor);
  }

  @Test
  void testHasFileOutput() {
    assertFalse(jsMagmaScriptRunner.hasFileOutput(mock(Script.class)));
  }
}
