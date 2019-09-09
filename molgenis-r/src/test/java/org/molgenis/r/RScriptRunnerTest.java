package org.molgenis.r;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.script.core.Script;
import org.molgenis.test.AbstractMockitoTest;

class RScriptRunnerTest extends AbstractMockitoTest {
  @Mock private RScriptExecutor rScriptExecutor;
  private RScriptRunner rScriptRunner;

  @BeforeEach
  void setUpBeforeMethod() {
    rScriptRunner = new RScriptRunner(rScriptExecutor);
  }

  @Test
  void testHasFileOutputTrue() {
    Script script =
        when(mock(Script.class).getContent()).thenReturn("lorum ${outputFile} ipsum").getMock();
    assertTrue(rScriptRunner.hasFileOutput(script));
  }

  @Test
  void testHasFileOutputFalse() {
    Script script = when(mock(Script.class).getContent()).thenReturn("lorum ${x} ipsum").getMock();
    assertFalse(rScriptRunner.hasFileOutput(script));
  }
}
