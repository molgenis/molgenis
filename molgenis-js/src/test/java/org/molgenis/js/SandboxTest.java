package org.molgenis.js;

public class SandboxTest extends MolgenisJsTest
{
	//	@Test public void testAllowed() { ScriptEvaluator.eval("1 + 1", new MapEntity("firstName", "Piet"), new EntityMetaDataImpl("person")); } @Test(expectedExceptions = EcmaError.class) public void testCallingNonVisibleClass() { EntityMetaData emd = new EntityMetaDataImpl("person"); emd.addAttribute("firstName").setDataType(MolgenisFieldTypes.SCRIPT); ScriptEvaluator.eval("new java.lang.Integer(6).toString()", new MapEntity("firstName", "Piet"), emd); } @Test public void testGlobalMethod() { EntityMetaData emd = new EntityMetaDataImpl("person"); emd.addAttribute("firstName").setDataType(MolgenisFieldTypes.SCRIPT); ScriptEvaluator.eval("$('firstName')", new MapEntity("firstName", "Piet"), emd);
	//	}
	//
	//	@Test(expectedExceptions = EcmaError.class)
	//	public void testCallingReflection()
	//	{
	//		try
	//		{
	//			EntityMetaData emd = new EntityMetaDataImpl("person");
	//			emd.addAttribute("firstName").setDataType(MolgenisFieldTypes.SCRIPT);
	//
	//			ScriptEvaluator.eval("java.lang.Class.forName('java.util.Date').newInstance()", new MapEntity("firstName",
	//					"Piet"), emd);
	//		}
	//		catch (Exception e)
	//		{
	//			e.printStackTrace();
	//			throw e;
	//		}
	//	}
}
