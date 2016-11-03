package org.molgenis.js;

public class SandboxTest
{
	// FIXME enable tests
	//	private static EntityType personFirstNameEntityType;
	//
	//	@BeforeClass
	//	protected static void beforeClass()
	//	{
	//		ScriptEvaluatorTest.beforeClass();
	//		Attribute firstNameAttr = when(mock(Attribute.class).getName()).thenReturn("firstName").getMock();
	//		when(firstNameAttr.getDataType()).thenReturn(SCRIPT);
	//		personFirstNameEntityType = when(mock(EntityType.class).getName()).thenReturn("person").getMock();
	//		when(personFirstNameEntityType.getAttribute("firstName")).thenReturn(firstNameAttr);
	//		when(personFirstNameEntityType.getAtomicAttributes()).thenReturn(singletonList(firstNameAttr));
	//	}
	//
	//	@BeforeMethod
	//	public void beforeMethod()
	//	{
	//		new RhinoConfig().init();
	//	}
	//
	//	@Test
	//	public void testAllowed()
	//	{
	//		Entity person = new DynamicEntity(personFirstNameEntityType);
	//		person.set("firstName", "Piet");
	//		ScriptEvaluator.eval("1 + 1", person, personFirstNameEntityType);
	//	}
	//
	//	@Test(expectedExceptions = EcmaError.class)
	//	public void testCallingNonVisibleClass()
	//	{
	//		Entity person = new DynamicEntity(personFirstNameEntityType);
	//		person.set("firstName", "Piet");
	//		ScriptEvaluator.eval("new java.lang.Integer(6).toString()", person, personFirstNameEntityType);
	//	}
	//
	//	@Test
	//	public void testGlobalMethod()
	//	{
	//		Entity person = new DynamicEntity(personFirstNameEntityType);
	//		person.set("firstName", "Piet");
	//		ScriptEvaluator.eval("$('firstName')", person, personFirstNameEntityType);
	//	}
	//
	//	@Test(expectedExceptions = EcmaError.class)
	//	public void testCallingReflection()
	//	{
	//		try
	//		{
	//			Entity person = new DynamicEntity(personFirstNameEntityType);
	//			person.set("firstName", "Piet");
	//
	//			ScriptEvaluator
	//					.eval("java.lang.Class.forName('java.util.Date').newInstance()", person, personFirstNameEntityType);
	//		}
	//		catch (Exception e)
	//		{
	//			e.printStackTrace();
	//			throw e;
	//		}
	//	}
}
