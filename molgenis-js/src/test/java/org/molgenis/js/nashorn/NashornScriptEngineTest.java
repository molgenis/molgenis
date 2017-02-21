package org.molgenis.js.nashorn;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by Dennis on 2/17/2017.
 */
public class NashornScriptEngineTest
{
	private static NashornScriptEngine nashornScriptEngine;

	@BeforeClass
	public static void setUpBeforeClass()
	{
		nashornScriptEngine = new NashornScriptEngine();
	}

	@Test
	public void testInvokeFunction() throws Exception
	{
		long epoch = 1487342481434L;
		assertEquals(nashornScriptEngine.invokeFunction("evalScript", "new Date(" + epoch + ")"), epoch);
	}

}