package org.molgenis.data.validation.meta;

import org.molgenis.data.MolgenisDataException;
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
	private Package testPackage;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		systemPackageRegistry = mock(SystemPackageRegistry.class);
		packageValidator = new PackageValidator(systemPackageRegistry);
		systemPackage = when(mock(Package.class).getFullyQualifiedName()).thenReturn(PACKAGE_SYSTEM).getMock();
		testPackage = when(mock(Package.class).getFullyQualifiedName()).thenReturn("test").getMock();
	}

	@Test
	public void testValidateNonSystemPackage() throws Exception
	{
		Package package_ = when(mock(Package.class).getFullyQualifiedName()).thenReturn("myPackage").getMock();
		when(package_.getName()).thenReturn("myPackage");
		when(systemPackageRegistry.containsPackage(package_)).thenReturn(false);
		packageValidator.validate(package_);
	}

	@Test
	public void testValidateSystemPackageInRegistry() throws Exception
	{
		Package package_ = when(mock(Package.class).getFullyQualifiedName()).thenReturn(PACKAGE_SYSTEM + '_' + "myPackage").getMock();
		when(package_.getName()).thenReturn("myPackage");
		when(package_.getParent()).thenReturn(systemPackage);
		when(package_.getRootPackage()).thenReturn(systemPackage);
		when(systemPackageRegistry.containsPackage(package_)).thenReturn(true);
		packageValidator.validate(package_);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Modifying system packages is not allowed")
	public void testValidateSystemPackageNotInRegistry() throws Exception
	{
		Package package_ = when(mock(Package.class).getFullyQualifiedName()).thenReturn(PACKAGE_SYSTEM + '_' + "myPackage").getMock();
		when(package_.getName()).thenReturn("myPackage");
		when(package_.getParent()).thenReturn(systemPackage);
		when(package_.getRootPackage()).thenReturn(systemPackage);
		when(systemPackageRegistry.containsPackage(package_)).thenReturn(false);
		packageValidator.validate(package_);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Qualified package name \\[myPackage\\] not equal to parent package name \\[sys\\] underscore package name \\[myPackage\\]")
	public void testValidateNameInvalid() throws Exception
	{
		Package package_ = when(mock(Package.class).getFullyQualifiedName()).thenReturn("myPackage").getMock();
		when(package_.getName()).thenReturn("myPackage");
		when(package_.getParent()).thenReturn(systemPackage);
		when(systemPackageRegistry.containsPackage(package_)).thenReturn(false);
		packageValidator.validate(package_);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Invalid characters in: \\[my_Package\\] Only letters \\(a-z, A-Z\\), digits \\(0-9\\) and hashes \\(#\\) are allowed.")
	public void testValidatePackageInvalidName() throws Exception
	{
		Package package_ = when(mock(Package.class).getFullyQualifiedName()).thenReturn("test_my_Package").getMock();
		when(package_.getName()).thenReturn("my_Package");
		when(package_.getParent()).thenReturn(testPackage);
		when(package_.getRootPackage()).thenReturn(testPackage);
		packageValidator.validate(package_);
	}

	@Test
	public void testValidatePackageValidName() throws Exception
	{
		Package package_ = when(mock(Package.class).getFullyQualifiedName()).thenReturn("test_myPackage").getMock();
		when(package_.getName()).thenReturn("myPackage");
		when(package_.getParent()).thenReturn(testPackage);
		when(package_.getRootPackage()).thenReturn(testPackage);
		packageValidator.validate(package_);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Invalid characters in: \\[my_Package\\] Only letters \\(a-z, A-Z\\), digits \\(0-9\\) and hashes \\(#\\) are allowed.")
	public void testValidatePackageInvalidNameNoParent() throws Exception
	{
		Package package_ = when(mock(Package.class).getFullyQualifiedName()).thenReturn("my_Package").getMock();
		when(package_.getName()).thenReturn("my_Package");
		when(package_.getParent()).thenReturn(null);
		packageValidator.validate(package_);
	}

	@Test
	public void testValidatePackageValidNameNoParent() throws Exception
	{
		Package package_ = when(mock(Package.class).getFullyQualifiedName()).thenReturn("myPackage").getMock();
		when(package_.getName()).thenReturn("myPackage");
		when(package_.getParent()).thenReturn(null);
		packageValidator.validate(package_);
	}
}