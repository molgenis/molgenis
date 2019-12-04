package org.molgenis.data.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.meta.model.Package;
import org.molgenis.util.exception.ExceptionMessageTest;

class GroupPackageDowngradeExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    Package aPackage = mock(Package.class);
    when(aPackage.getId()).thenReturn("test");
    ExceptionMessageTest.assertExceptionMessageEquals(
        new GroupPackageDowngradeException(aPackage), lang, message);
  }

  @Test
  void testGetMessage() {
    Package aPackage = mock(Package.class);
    when(aPackage.getId()).thenReturn("test");
    GroupPackageDowngradeException ex = new GroupPackageDowngradeException(aPackage);
    assertEquals("id:test", ex.getMessage());
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Moving package 'test' to anywhere other than the root is not allowed because it is a group package."
      }
    };
  }
}
