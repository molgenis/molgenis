package org.molgenis.data.security.exception;

import org.mockito.Mock;
import org.molgenis.data.meta.model.Package;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.molgenis.data.security.PackagePermission.ADD_PACKAGE;

public class PackagePermissionExceptionTest extends ExceptionMessageTest
{
	@Mock
	private Package aPackage;
	private PackagePermissionDeniedException packagePermissionException;

	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("data-security");
		when(aPackage.getLabel()).thenReturn("System");
		packagePermissionException = new PackagePermissionDeniedException(ADD_PACKAGE, aPackage);
	}

	@Test(dataProvider = "languageMessageProvider")
	@Override
	public void testGetLocalizedMessage(String lang, String message)
	{
		assertExceptionMessageEquals(packagePermissionException, lang, message);
	}

	@DataProvider(name = "languageMessageProvider")
	@Override
	public Object[][] languageMessageProvider()
	{
		Object[] enParams = { "en", "No 'Add package' permission on package 'System'." };
		return new Object[][] { enParams };
	}
}