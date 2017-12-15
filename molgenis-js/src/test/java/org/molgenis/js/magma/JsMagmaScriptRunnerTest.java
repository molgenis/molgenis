package org.molgenis.js.magma;

import org.mockito.Mock;
import org.molgenis.script.Script;
import org.molgenis.script.core.exception.ScriptExecutionException;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class JsMagmaScriptRunnerTest extends AbstractMockitoTest
{
	@Mock
	private JsMagmaScriptExecutor jsMagmaScriptExecutor;

	private JsMagmaScriptRunner jsMagmaScriptRunner;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		jsMagmaScriptRunner = new JsMagmaScriptRunner(jsMagmaScriptExecutor);
	}

	@Test
	public void testGetName()
	{
		assertEquals(jsMagmaScriptRunner.getName(), "JavaScript (Magma)");
	}

	@Test
	public void testRunScript()
	{
		Script script = mock(Script.class);
		when(script.getContent()).thenReturn("scriptContent");
		Map<String, Object> parameters = emptyMap();
		String scriptResult = "scriptResult";
		when(jsMagmaScriptExecutor.executeScript("scriptContent", parameters)).thenReturn(scriptResult);
		assertEquals(jsMagmaScriptRunner.runScript(script, parameters), scriptResult);
	}

	@Test(expectedExceptions = ScriptExecutionException.class)
	public void testRunScriptException()
	{
		Script script = mock(Script.class);
		when(script.getContent()).thenReturn("scriptContent");
		Map<String, Object> parameters = emptyMap();
		ScriptExecutionException exception = new ScriptExecutionException(new IllegalStateException("blah"));
		doThrow(exception).when(jsMagmaScriptExecutor).executeScript("scriptContent", parameters);
		jsMagmaScriptRunner.runScript(script, parameters);
	}
}