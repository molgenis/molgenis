package org.molgenis.data.security;

import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.i18n.properties.AllPropertiesMessageSource;
import org.molgenis.i18n.test.exception.TestAllPropertiesMessageSource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;

import static org.testng.Assert.assertEquals;

@Test
public class PackagePermissionTest
{
	private PackagePermission addPackagePermission = PackagePermission.ADD_PACKAGE;
	private MessageFormatFactory messageFormatFactory = new MessageFormatFactory();
	private AllPropertiesMessageSource messageSource;

	@BeforeMethod
	public void exceptionMessageTestBeforeMethod()
	{
		messageSource = new TestAllPropertiesMessageSource(messageFormatFactory);
		messageSource.addMolgenisNamespaces("data-security");
		MessageSourceHolder.setMessageSource(messageSource);
	}

	@Test
	public void testNameEnglish()
	{
		assertEquals(messageSource.getMessage(addPackagePermission.getName(), Locale.ENGLISH), "Add package");
	}

	@Test
	public void testDescription()
	{
		assertEquals(messageSource.getMessage(addPackagePermission.getDescription(), Locale.ENGLISH),
				"Permission to add a child package to this package");
	}
}