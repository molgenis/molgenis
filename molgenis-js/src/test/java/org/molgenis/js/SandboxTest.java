package org.molgenis.js;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.mozilla.javascript.EcmaError;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static freemarker.template.utility.Collections12.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.SCRIPT;

public class SandboxTest extends ScriptEvaluatorTest
{
	private static EntityMetaData personFirstNameEntityMeta;

	@BeforeClass
	protected static void beforeClass()
	{
		ScriptEvaluatorTest.beforeClass();
		Attribute firstNameAttr = when(mock(Attribute.class).getName()).thenReturn("firstName")
				.getMock();
		when(firstNameAttr.getDataType()).thenReturn(SCRIPT);
		personFirstNameEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("person").getMock();
		when(personFirstNameEntityMeta.getAttribute("firstName")).thenReturn(firstNameAttr);
		when(personFirstNameEntityMeta.getAtomicAttributes()).thenReturn(singletonList(firstNameAttr));
	}

	@Test
	public void testAllowed()
	{
		Entity person = new DynamicEntity(personFirstNameEntityMeta);
		person.set("firstName", "Piet");
		ScriptEvaluator.eval("1 + 1", person, personFirstNameEntityMeta);
	}

	@Test(expectedExceptions = EcmaError.class)
	public void testCallingNonVisibleClass()
	{
		Entity person = new DynamicEntity(personFirstNameEntityMeta);
		person.set("firstName", "Piet");
		ScriptEvaluator.eval("new java.lang.Integer(6).toString()", person, personFirstNameEntityMeta);
	}

	@Test
	public void testGlobalMethod()
	{
		Entity person = new DynamicEntity(personFirstNameEntityMeta);
		person.set("firstName", "Piet");
		ScriptEvaluator.eval("$('firstName')", person, personFirstNameEntityMeta);
	}

	@Test(expectedExceptions = EcmaError.class)
	public void testCallingReflection()
	{
		try
		{
			Entity person = new DynamicEntity(personFirstNameEntityMeta);
			person.set("firstName", "Piet");

			ScriptEvaluator
					.eval("java.lang.Class.forName('java.util.Date').newInstance()", person, personFirstNameEntityMeta);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
}
