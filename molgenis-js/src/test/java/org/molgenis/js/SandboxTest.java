package org.molgenis.js;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.mozilla.javascript.EcmaError;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.SCRIPT;

public class SandboxTest
{
	private static EntityType personFirstNameEntityType;

	@BeforeClass
	protected static void beforeClass()
	{
		ScriptEvaluatorTest.beforeClass();
		AttributeMetaData firstNameAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("firstName")
				.getMock();
		when(firstNameAttr.getDataType()).thenReturn(SCRIPT);
		personFirstNameEntityType = when(mock(EntityType.class).getName()).thenReturn("person").getMock();
		when(personFirstNameEntityType.getAttribute("firstName")).thenReturn(firstNameAttr);
		when(personFirstNameEntityType.getAtomicAttributes()).thenReturn(singletonList(firstNameAttr));
	}

	@BeforeMethod
	public void beforeMethod()
	{
		new RhinoConfig().init();
	}

	@Test
	public void testAllowed()
	{
		Entity person = new DynamicEntity(personFirstNameEntityType);
		person.set("firstName", "Piet");
		ScriptEvaluator.eval("1 + 1", person, personFirstNameEntityType);
	}

	@Test(expectedExceptions = EcmaError.class)
	public void testCallingNonVisibleClass()
	{
		Entity person = new DynamicEntity(personFirstNameEntityType);
		person.set("firstName", "Piet");
		ScriptEvaluator.eval("new java.lang.Integer(6).toString()", person, personFirstNameEntityType);
	}

	@Test
	public void testGlobalMethod()
	{
		Entity person = new DynamicEntity(personFirstNameEntityType);
		person.set("firstName", "Piet");
		ScriptEvaluator.eval("$('firstName')", person, personFirstNameEntityType);
	}

	@Test(expectedExceptions = EcmaError.class)
	public void testCallingReflection()
	{
		try
		{
			Entity person = new DynamicEntity(personFirstNameEntityType);
			person.set("firstName", "Piet");

			ScriptEvaluator
					.eval("java.lang.Class.forName('java.util.Date').newInstance()", person, personFirstNameEntityType);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
}
