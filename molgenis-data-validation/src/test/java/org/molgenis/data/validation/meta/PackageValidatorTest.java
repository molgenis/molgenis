package org.molgenis.data.validation.meta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.validation.MolgenisValidationException;

class PackageValidatorTest {
  private PackageValidator packageValidator;
  private SystemPackageRegistry systemPackageRegistry;
  private Package systemPackage;
  private Package testPackage;

  @BeforeEach
  void setUpBeforeMethod() {
    systemPackageRegistry = mock(SystemPackageRegistry.class);
    packageValidator = new PackageValidator(systemPackageRegistry);
    systemPackage = when(mock(Package.class).getId()).thenReturn(PACKAGE_SYSTEM).getMock();
    testPackage = when(mock(Package.class).getId()).thenReturn("test").getMock();
  }

  @Test
  void testValidateNonSystemPackage() {
    Package package_ = when(mock(Package.class).getId()).thenReturn("myPackage").getMock();
    when(systemPackageRegistry.containsPackage(package_)).thenReturn(false);
    packageValidator.validate(package_);
  }

  @Test
  void testValidateSystemPackageInRegistry() {
    Package package_ =
        when(mock(Package.class).getId()).thenReturn(PACKAGE_SYSTEM + '_' + "myPackage").getMock();
    when(package_.getParent()).thenReturn(systemPackage);
    when(package_.getRootPackage()).thenReturn(systemPackage);
    when(systemPackageRegistry.containsPackage(package_)).thenReturn(true);
    packageValidator.validate(package_);
  }

  @SuppressWarnings("deprecation")
  @Test
  void testValidateSystemPackageNotInRegistry() {
    Package package_ =
        when(mock(Package.class).getId()).thenReturn(PACKAGE_SYSTEM + '_' + "myPackage").getMock();
    when(package_.getParent()).thenReturn(systemPackage);
    when(package_.getRootPackage()).thenReturn(systemPackage);
    when(systemPackageRegistry.containsPackage(package_)).thenReturn(false);
    Exception exception =
        assertThrows(MolgenisValidationException.class, () -> packageValidator.validate(package_));
    assertThat(exception.getMessage()).containsPattern("Modifying system packages is not allowed");
  }

  @SuppressWarnings("deprecation")
  @Test
  void testValidatePackageInvalidName() {
    Package package_ = when(mock(Package.class).getId()).thenReturn("0package").getMock();
    when(package_.getParent()).thenReturn(testPackage);
    when(package_.getRootPackage()).thenReturn(testPackage);
    Exception exception =
        assertThrows(MolgenisDataException.class, () -> packageValidator.validate(package_));
    assertThat(exception.getMessage())
        .containsPattern("Invalid name: \\[0package\\] Names must start with a letter.");
  }

  @Test
  void testValidatePackageValidName() {
    Package package_ = when(mock(Package.class).getId()).thenReturn("test_myPackage").getMock();
    when(package_.getParent()).thenReturn(testPackage);
    when(package_.getRootPackage()).thenReturn(testPackage);
    packageValidator.validate(package_);
  }

  @Test
  void testValidatePackageValidNameNoParent() {
    Package package_ = when(mock(Package.class).getId()).thenReturn("myPackage").getMock();
    when(package_.getParent()).thenReturn(null);
    packageValidator.validate(package_);
  }

  @SuppressWarnings("deprecation")
  @Test
  void testValidatePackageParentParentIsSelf() {
    Package aPackage = when(mock(Package.class).getId()).thenReturn("child").getMock();
    when(aPackage.getLabel()).thenReturn("Child");
    when(aPackage.getParent()).thenReturn(aPackage);
    Exception exception =
        assertThrows(MolgenisValidationException.class, () -> packageValidator.validate(aPackage));
    assertThat(exception.getMessage())
        .containsPattern("Package 'Child' with id 'child' parent contains cycles");
  }

  @SuppressWarnings("deprecation")
  @Test
  void testValidatePackageParentCycle() {
    Package packageParent = when(mock(Package.class).getId()).thenReturn("parent").getMock();
    Package aPackage = when(mock(Package.class).getId()).thenReturn("child").getMock();
    when(aPackage.getLabel()).thenReturn("Child");
    when(aPackage.getParent()).thenReturn(packageParent);
    when(packageParent.getParent()).thenReturn(aPackage);
    Exception exception =
        assertThrows(MolgenisValidationException.class, () -> packageValidator.validate(aPackage));
    assertThat(exception.getMessage())
        .containsPattern("Package 'Child' with id 'child' parent contains cycles");
  }

  @Test
  void testValidatePackageParentOk() {
    Package packageParent = when(mock(Package.class).getId()).thenReturn("parent").getMock();
    Package aPackage = when(mock(Package.class).getId()).thenReturn("child").getMock();
    when(aPackage.getParent()).thenReturn(packageParent);
    packageValidator.validate(aPackage);
    // test passes if no exception occurs
  }
}
