package org.molgenis.r;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.mockito.Mock;
import org.molgenis.script.core.Script;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RScriptRunnerTest extends AbstractMockitoTest {
  @Mock private RScriptExecutor rScriptExecutor;
  private RScriptRunner rScriptRunner;

  @BeforeMethod
  public void setUpBeforeMethod() {
    rScriptRunner = new RScriptRunner(rScriptExecutor);
  }

  @Test
  public void testHasFileOutputTrue() {
    Script script =
        when(mock(Script.class).getContent()).thenReturn("lorum ${outputFile} ipsum").getMock();
    assertTrue(rScriptRunner.hasFileOutput(script));
  }

  @Test
  public void testHasFileOutputFalse() {
    Script script = when(mock(Script.class).getContent()).thenReturn("lorum ${x} ipsum").getMock();
    assertFalse(rScriptRunner.hasFileOutput(script));
  }
}
