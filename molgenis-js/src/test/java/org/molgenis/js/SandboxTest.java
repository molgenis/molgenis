package org.molgenis.js;

import org.molgenis.data.support.MapEntity;
import org.mozilla.javascript.EcmaError;
import org.testng.annotations.Test;

public class SandboxTest extends MolgenisJsTest
{
	@Test
	public void testAllowed()
	{
		ScriptEvaluator.eval("1 + 1", new MapEntity("firstName", "Piet"));
	}

	@Test(expectedExceptions = EcmaError.class)
	public void testCallingNonVisibleClass()
	{
		ScriptEvaluator.eval("new java.lang.Integer(6).toString()", new MapEntity("firstName", "Piet"));
	}

	@Test
	public void testGlobalMethod()
	{
		ScriptEvaluator.eval("$('firstName')", new MapEntity("firstName", "Piet"));
	}

	@Test(expectedExceptions = EcmaError.class)
	public void testCallingReflection()
	{
		try
		{
			ScriptEvaluator.eval("java.lang.Class.forName('java.util.Date').newInstance()", new MapEntity("firstName",
					"Piet"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
}
