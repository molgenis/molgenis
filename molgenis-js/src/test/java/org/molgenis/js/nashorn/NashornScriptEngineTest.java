package org.molgenis.js.nashorn;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.script.ScriptException;
import java.time.LocalDate;
import java.time.ZoneId;

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
		assertEquals(nashornScriptEngine.eval("new Date(" + epoch + ")"), epoch);
	}

	@Test
	public void testInvokeDateDMY() throws Exception
	{
		LocalDate localDate = LocalDate.now();
		String script = String.format("new Date(%d,%d,%d)", localDate.getYear(), localDate.getMonth().getValue() - 1,
				localDate.getDayOfMonth());
		long epochMilli = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
		assertEquals(nashornScriptEngine.eval(script), epochMilli);
	}

	@Test(expectedExceptions = ScriptException.class, expectedExceptionsMessageRegExp = "ReferenceError: \"piet\" is not defined in <eval> at line number 1")
	public void doesntDirtyContext() throws ScriptException
	{
		nashornScriptEngine.eval("piet = 3");
		nashornScriptEngine.eval("piet");
	}

}