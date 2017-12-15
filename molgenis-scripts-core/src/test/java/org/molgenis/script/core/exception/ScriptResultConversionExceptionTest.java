package org.molgenis.script.core.exception;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ScriptResultConversionExceptionTest extends ExceptionMessageTest
{
	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("scripts");
	}

	@Test
	public void testGetLocalizedMessage()
	{
		assertEquals(new ScriptResultConversionException(Long.MAX_VALUE).getLocalizedMessage(),
				"Script result '9,223,372,036,854,775,807' cannot be converted to the appropriate type");
	}
}