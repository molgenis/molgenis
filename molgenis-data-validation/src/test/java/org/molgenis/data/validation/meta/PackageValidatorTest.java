package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.validation.MolgenisValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

public class PackageValidatorTest
{
	private PackageValidator packageValidator;
	private SystemPackageRegistry systemPackageRegistry;
	private Package systemPackage;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		systemPackageRegistry = mock(SystemPackageRegistry.class);
		packageValidator = new PackageValidator(systemPackageRegistry);
		systemPackage = when(mock(Package.class).getName()).thenReturn(PACKAGE_SYSTEM).getMock();
	}

	@Test
	public void testValidateNonSystemPackage() throws Exception
	{
		Package package_ = when(mock(Package.class).getName()).thenReturn("myPackage").getMock();
		when(package_.getSimpleName()).thenReturn("myPackage");
		when(systemPackageRegistry.containsPackage(package_)).thenReturn(false);
		packageValidator.validate(package_);
	}

	@Test
	public void testValidateSystemPackageInRegistry() throws Exception
	{
		Package package_ = when(mock(Package.class).getName()).thenReturn(PACKAGE_SYSTEM + '_' + "myPackage").getMock();
		when(package_.getSimpleName()).thenReturn("myPackage");
		when(package_.getParent()).thenReturn(systemPackage);
		when(package_.getRootPackage()).thenReturn(systemPackage);
		when(systemPackageRegistry.containsPackage(package_)).thenReturn(true);
		packageValidator.validate(package_);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Modifying system packages is not allowed")
	public void testValidateSystemPackageNotInRegistry() throws Exception
	{
		Package package_ = when(mock(Package.class).getName()).thenReturn(PACKAGE_SYSTEM + '_' + "myPackage").getMock();
		when(package_.getSimpleName()).thenReturn("myPackage");
		when(package_.getParent()).thenReturn(systemPackage);
		when(package_.getRootPackage()).thenReturn(systemPackage);
		when(systemPackageRegistry.containsPackage(package_)).thenReturn(false);
		packageValidator.validate(package_);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Qualified package name \\[myPackage\\] not equal to parent package name \\[sys\\] underscore package name \\[myPackage\\]")
	public void testValidateNameInvalid() throws Exception
	{
		Package package_ = when(mock(Package.class).getName()).thenReturn("myPackage").getMock();
		when(package_.getSimpleName()).thenReturn("myPackage");
		when(package_.getParent()).thenReturn(systemPackage);
		when(systemPackageRegistry.containsPackage(package_)).thenReturn(false);
		packageValidator.validate(package_);
	}
}