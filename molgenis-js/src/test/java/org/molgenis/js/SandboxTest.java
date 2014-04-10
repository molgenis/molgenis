package org.molgenis.js;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.support.MapEntity;
import org.mozilla.javascript.EcmaError;
import org.testng.annotations.Test;

public class SandboxTest extends MolgenisJsTest
{
	@Test
	public void testAllowed()
	{
		assertEquals(ScriptEvaluator.eval("1 + 1"), 2);
	}

	@Test(expectedExceptions = EcmaError.class)
	public void testCallingNonVisibleClass()
	{
		ScriptEvaluator.eval("new java.util.Date()");
	}

	@Test
	public void testGlobalMethod()
	{
		ScriptEvaluator.eval("$('firstName')", new MapEntity("firstName", "Piet"));
	}

	@Test(expectedExceptions = EcmaError.class)
	public void testCallingReflection()
	{
		ScriptEvaluator.eval("$('firstName').getClass().forName('java.util.Date').newInstance()", new MapEntity(
				"firstName", "Piet"));
	}
}
