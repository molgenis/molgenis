package org.molgenis.script.core;

import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ScriptUtilsTest
{
	@Test
	public void testGenerateScript() throws Exception
	{
		Script script = mock(Script.class);
		when(script.getContent()).thenReturn("Hey ${name}");
		Map<String, Object> parameterValues = Collections.singletonMap("name", "Piet");
		String renderedScript = ScriptUtils.generateScript(script, parameterValues);
		assertEquals(renderedScript, "Hey Piet");
	}
}