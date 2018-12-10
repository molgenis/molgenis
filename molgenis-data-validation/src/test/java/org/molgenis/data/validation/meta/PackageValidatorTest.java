package org.molgenis.data.validation.meta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.validation.MolgenisValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PackageValidatorTest {
  private PackageValidator packageValidator;
  private SystemPackageRegistry systemPackageRegistry;
  private Package systemPackage;
  private Package testPackage;

  @BeforeMethod
  public void setUpBeforeMethod() {
    systemPackageRegistry = mock(SystemPackageRegistry.class);
    packageValidator = new PackageValidator(systemPackageRegistry);
    systemPackage = when(mock(Package.class).getId()).thenReturn(PACKAGE_SYSTEM).getMock();
    testPackage = when(mock(Package.class).getId()).thenReturn("test").getMock();
  }

  @Test
  public void testValidateNonSystemPackage() {
    Package package_ = when(mock(Package.class).getId()).thenReturn("myPackage").getMock();
    when(systemPackageRegistry.containsPackage(package_)).thenReturn(false);
    packageValidator.validate(package_);
  }

  @Test
  public void testValidateSystemPackageInRegistry() {
    Package package_ =
        when(mock(Package.class).getId()).thenReturn(PACKAGE_SYSTEM + '_' + "myPackage").getMock();
    when(package_.getParent()).thenReturn(systemPackage);
    when(package_.getRootPackage()).thenReturn(systemPackage);
    when(systemPackageRegistry.containsPackage(package_)).thenReturn(true);
    packageValidator.validate(package_);
  }

  @SuppressWarnings("deprecation")
  @Test(
      expectedExceptions = MolgenisValidationException.class,
      expectedExceptionsMessageRegExp = "Modifying system packages is not allowed")
  public void testValidateSystemPackageNotInRegistry() {
    Package package_ =
        when(mock(Package.class).getId()).thenReturn(PACKAGE_SYSTEM + '_' + "myPackage").getMock();
    when(package_.getParent()).thenReturn(systemPackage);
    when(package_.getRootPackage()).thenReturn(systemPackage);
    when(systemPackageRegistry.containsPackage(package_)).thenReturn(false);
    packageValidator.validate(package_);
  }

  @SuppressWarnings("deprecation")
  @Test(
      expectedExceptions = MolgenisDataException.class,
      expectedExceptionsMessageRegExp =
          "Invalid name: \\[0package\\] Names must start with a letter.")
  public void testValidatePackageInvalidName() {
    Package package_ = when(mock(Package.class).getId()).thenReturn("0package").getMock();
    when(package_.getParent()).thenReturn(testPackage);
    when(package_.getRootPackage()).thenReturn(testPackage);
    packageValidator.validate(package_);
  }

  @Test
  public void testValidatePackageValidName() {
    Package package_ = when(mock(Package.class).getId()).thenReturn("test_myPackage").getMock();
    when(package_.getParent()).thenReturn(testPackage);
    when(package_.getRootPackage()).thenReturn(testPackage);
    packageValidator.validate(package_);
  }

  @Test
  public void testValidatePackageValidNameNoParent() {
    Package package_ = when(mock(Package.class).getId()).thenReturn("myPackage").getMock();
    when(package_.getParent()).thenReturn(null);
    packageValidator.validate(package_);
  }

  @SuppressWarnings("deprecation")
  @Test(
      expectedExceptions = MolgenisValidationException.class,
      expectedExceptionsMessageRegExp = "Package 'Child' with id 'child' parent contains cycles")
  public void testValidatePackageParentParentIsSelf() {
    Package aPackage = when(mock(Package.class).getId()).thenReturn("child").getMock();
    when(aPackage.getLabel()).thenReturn("Child");
    when(aPackage.getParent()).thenReturn(aPackage);
    packageValidator.validate(aPackage);
  }

  @SuppressWarnings("deprecation")
  @Test(
      expectedExceptions = MolgenisValidationException.class,
      expectedExceptionsMessageRegExp = "Package 'Child' with id 'child' parent contains cycles")
  public void testValidatePackageParentCycle() {
    Package packageParent = when(mock(Package.class).getId()).thenReturn("parent").getMock();
    Package aPackage = when(mock(Package.class).getId()).thenReturn("child").getMock();
    when(aPackage.getLabel()).thenReturn("Child");
    when(aPackage.getParent()).thenReturn(packageParent);
    when(packageParent.getParent()).thenReturn(aPackage);
    packageValidator.validate(aPackage);
  }

  @Test
  public void testValidatePackageParentOk() {
    Package packageParent = when(mock(Package.class).getId()).thenReturn("parent").getMock();
    Package aPackage = when(mock(Package.class).getId()).thenReturn("child").getMock();
    when(aPackage.getParent()).thenReturn(packageParent);
    packageValidator.validate(aPackage);
    // test passes if no exception occurs
  }
}
