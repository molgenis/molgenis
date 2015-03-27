package org.molgenis.js;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.mozilla.javascript.EcmaError;
import org.testng.annotations.Test;

public class SandboxTest extends MolgenisJsTest
{
	@Test
	public void testAllowed()
	{
		ScriptEvaluator.eval("1 + 1", new MapEntity("firstName", "Piet"), new DefaultEntityMetaData("person"));
	}

	@Test(expectedExceptions = EcmaError.class)
	public void testCallingNonVisibleClass()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("firstName").setDataType(MolgenisFieldTypes.SCRIPT);

		ScriptEvaluator.eval("new java.lang.Integer(6).toString()", new MapEntity("firstName", "Piet"), emd);
	}

	@Test
	public void testGlobalMethod()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("firstName").setDataType(MolgenisFieldTypes.SCRIPT);

		ScriptEvaluator.eval("$('firstName')", new MapEntity("firstName", "Piet"), emd);
	}

	@Test(expectedExceptions = EcmaError.class)
	public void testCallingReflection()
	{
		try
		{
			DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
			emd.addAttribute("firstName").setDataType(MolgenisFieldTypes.SCRIPT);

			ScriptEvaluator.eval("java.lang.Class.forName('java.util.Date').newInstance()", new MapEntity("firstName",
					"Piet"), emd);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
}
