package org.molgenis.script.core.exception;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ScriptExecutionExceptionTest extends ExceptionMessageTest
{
	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("scripts");
	}

	@Test
	public void testGetLocalizedMessage()
	{
		assertEquals(new ScriptExecutionException("Something went wrong").getLocalizedMessage(),
				"Error running script. Something went wrong");
	}

	@Test
	public void testGetLocalizedMessageWithCause()
	{
		Exception cause = new IllegalArgumentException();
		assertEquals(new ScriptExecutionException(cause).getLocalizedMessage(), "Error running script. ");
	}
}