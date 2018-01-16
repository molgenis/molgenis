package org.molgenis.core.ui.style;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.molgenis.ui.style.CannotDeleteCurrentThemeException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CannotDeleteCurrentThemeExceptionTest extends ExceptionMessageTest
{
	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("core-ui");
	}

	@Test
	public void testGetMessage()
	{
		Assert.assertEquals(new CannotDeleteCurrentThemeException("theme0").getMessage(), "id:theme0");
	}

	@Test
	public void testGetLocalizedMessageArguments()
	{
		assertEquals(new CannotDeleteCurrentThemeException("theme0").getLocalizedMessage(),
				"Cannot delete the currently selected bootstrap theme 'theme0'.");
	}
}